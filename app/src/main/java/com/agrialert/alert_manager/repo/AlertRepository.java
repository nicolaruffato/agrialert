package com.agrialert.alert_manager.repo;

import android.content.Context;
import android.util.Log;

import com.agrialert.alert_manager.DataManagerConnector;
import com.agrialert.alert_manager.domain.AlertEvaluator;
import com.agrialert.api.OpenMeteoApiClient;
import com.agrialert.api.WeatherApiResponse;
import com.agrialert.data_manager.ActivatedAlerts;
import com.agrialert.data_manager.Alert;
import com.agrialert.data_manager.AlertWithThreshold;
import com.agrialert.data_manager.DataManager;
import com.agrialert.data_manager.Field;
import com.agrialert.data_manager.GroupWithFields;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Scheduler;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * Orchestrates weather fetch, threshold evaluation, persistence, and alert state updates.
 * <p>
 * This repository fetches forecasts via {@link OpenMeteoApiClient}, evaluates thresholds via
 * {@link AlertEvaluator}, and persists/updates alerts through the bound {@link DataManager} service
 * using {@link DataManagerConnector}.
 * </p>
 * <p>
 * When persisting candidates, duplicates are avoided by skipping insertion if an unresolved alert
 * of the same type already exists for the field, or if the most recently resolved alert is still
 * within its event duration for the candidate forecast time.
 * </p>
 */
public class AlertRepository {

    private static final String TAG = "AlertRepository";
    private static final long BIND_TIMEOUT_MS = 5_000L;
    private static final int NO_ALERT_ID = -1;
    private static final long DEFAULT_EVENT_DURATION_MS = TimeUnit.HOURS.toMillis(12);

    private final Context appContext;
    private final OpenMeteoApiClient apiClient;
    private final AlertEvaluator evaluator;
    private final Scheduler ioScheduler;
    private final Scheduler mainScheduler;

    /**
     * Creates a new repository with explicit dependencies and schedulers.
     *
     * @param context       any context used to derive the application context
     * @param apiClient     client used to fetch weather data
     * @param evaluator     evaluator used to turn weather into alerts
     * @param ioScheduler   scheduler for IO work; defaults to {@link Schedulers#io()} when null
     * @param mainScheduler scheduler for UI observation; falls back to IO when null
     */
    public AlertRepository(Context context,
                           OpenMeteoApiClient apiClient,
                           AlertEvaluator evaluator,
                           Scheduler ioScheduler,
                           Scheduler mainScheduler) {
        this.appContext = context.getApplicationContext();
        this.apiClient = apiClient;
        this.evaluator = evaluator;
        this.ioScheduler = ioScheduler != null ? ioScheduler : Schedulers.io();
        this.mainScheduler = mainScheduler;
    }

    /**
     * Returns the scheduler used for downstream observation.
     *
     * @return the main scheduler if available, otherwise the IO scheduler
     */
    private Scheduler observeScheduler() {
        return mainScheduler != null ? mainScheduler : ioScheduler;
    }

    // ------------------- MUTATE -------------------

    /**
     * Updates the resolved flag for an alert using reactive semantics.
     *
     * @param id       alert identifier
     * @param resolved resolved state to set
     * @return a {@link Completable} that completes when the update is applied
     */
    public Completable setResolvedRx(long id, boolean resolved) {
        return DataManagerConnector.withCompletable(appContext, dm ->
                dm.setResolved(id, resolved)
                        .subscribeOn(ioScheduler)
                        .observeOn(observeScheduler())
        );
    }

    // ------------------- SYNC METEO -------------------

    /**
     * Fetches all saved fields from {@link DataManager} and performs a full sync.
     *
     * @return a {@link Single} emitting the sync result
     */
    public Single<WeatherSyncResult> syncAllGroups() {
        return DataManagerConnector.withSingle(appContext, BIND_TIMEOUT_MS, dm ->
                dm.cleanupAlerts()
                        .andThen(dm.getAllGroups().first(Collections.emptyList()))
                        .subscribeOn(ioScheduler)
                        .observeOn(ioScheduler)
                        .flatMap(groups -> syncWithDataManager(dm, groups))
        );
    }

    // ------------------- MODEL -------------------

    /**
     * Holds the outcome of a weather sync.
     * <p>
     * A single {@link WeatherApiResponse} is not always meaningful when syncing multiple fields;
     * therefore {@link #response} may be {@code null}. Consumers should rely on {@link #created}.
     * </p>
     */
    public static class WeatherSyncResult {
        /** Optional weather response associated with the sync (may be {@code null}). */
        public final WeatherApiResponse response;

        /** Alerts created during the sync (may be empty). */
        public final List<Alert> created;

        /**
         * Creates a new sync result.
         *
         * @param response weather response used during the sync
         * @param created  alerts created during the sync
         */
        public WeatherSyncResult(WeatherApiResponse response, List<Alert> created) {
            this.response = response;
            this.created = created;
        }
    }

    /**
     * Syncs weather for all provided groups and persists any new alerts.
     *
     * @param dm     bound {@link DataManager} instance
     * @param groups groups with their fields
     * @return a {@link Single} emitting the sync result
     */
    private Single<WeatherSyncResult> syncWithDataManager(DataManager dm, List<GroupWithFields> groups) {
        if (groups == null || groups.isEmpty()) {
            return Single.just(new WeatherSyncResult(null, Collections.emptyList()));
        }

        List<Field> allFields = new ArrayList<>();
        for (GroupWithFields group : groups) {
            if (group == null || group.getFields() == null) {
                continue;
            }
            allFields.addAll(group.getFields());
        }
        return syncFields(dm, allFields);
    }

    /**
     * Syncs weather for a list of fields and persists any new alerts.
     *
     * @param dm     bound {@link DataManager} instance
     * @param fields fields to process
     * @return a {@link Single} emitting the sync result
     */
    private Single<WeatherSyncResult> syncFields(DataManager dm, List<Field> fields) {
        if (fields == null || fields.isEmpty()) {
            return Single.just(new WeatherSyncResult(null, Collections.emptyList()));
        }

        return Flowable.fromIterable(fields)
                .concatMapSingle(field -> evalFieldAlerts(dm, field))
                .flatMapIterable(list -> list)
                .toList()
                .flatMap(alerts -> persistCandidates(dm, alerts))
                .map(saved -> new WeatherSyncResult(null, saved))
                .subscribeOn(ioScheduler)
                .observeOn(ioScheduler);
    }

    /**
     * Evaluates alerts for a single field by fetching weather and active thresholds.
     *
     * @param dm        bound {@link DataManager} instance
     * @param field     field to evaluate
     * @return a {@link Single} emitting alert candidates for the field
     */
    private Single<List<Alert>> evalFieldAlerts(DataManager dm, Field field) {
        if (field == null) {
            return Single.just(Collections.emptyList());
        }
        Double lat = field.getLatitude();
        Double lon = field.getLongitude();
        if (lat == null || lon == null) {
            Log.w(TAG, "Coordinate mancanti per il campo id=" + field.getId());
            return Single.just(Collections.emptyList());
        }

        Single<ActivatedAlerts> activatedSingle = dm.getActivatedAlertsFromField(field.getId())
                .firstOrError()
                .subscribeOn(ioScheduler)
                .observeOn(ioScheduler)
                .onErrorReturnItem(new ActivatedAlerts());

        Single<WeatherApiResponse> weatherSingle = Single.fromCallable(() ->
                        apiClient.fetchWeather(lat, lon)
                )
                .subscribeOn(ioScheduler);

        return activatedSingle
                .flatMap(activated -> weatherSingle.map(response -> {
                    List<AlertWithThreshold> activeDefinitions =
                            activated != null && activated.getAlerts() != null
                                    ? activated.getAlerts()
                                    : Collections.emptyList();

                    List<Alert> evaluated = evaluator.evaluate(
                            response,
                            field,
                            null,
                            activeDefinitions
                    );
                    return evaluated;
                }))
                .onErrorReturn(error -> {
                    Log.w(TAG, "Errore durante la valutazione per il campo id=" + field.getId(), error);
                    return Collections.emptyList();
                });
    }

    /**
     * Persists candidate alerts, filtering out duplicates based on recent history.
     *
     * @param dm         bound {@link DataManager} instance
     * @param candidates alert candidates to persist
     * @return a {@link Single} emitting the saved alerts
     */
    private Single<List<Alert>> persistCandidates(DataManager dm, List<Alert> candidates) {
        if (candidates == null || candidates.isEmpty()) {
            return Single.just(Collections.emptyList());
        }

        return Flowable.fromIterable(candidates)
                .concatMapMaybe(candidate -> insertIfNew(dm, candidate))
                .toList()
                .subscribeOn(ioScheduler)
                .observeOn(ioScheduler);
    }

    /**
     * Inserts a candidate alert only if no unresolved alert of the same type already exists for the
     * same field, and the most recently resolved alert (if any) is not still within its event
     * duration.
     *
     * @param dm        bound {@link DataManager} instance
     * @param candidate alert candidate to insert
     * @return a {@link Maybe} emitting the inserted alert, or empty when skipped
     */
    private Maybe<Alert> insertIfNew(DataManager dm, Alert candidate) {
        if (candidate == null) {
            return Maybe.empty();
        }
        Alert sentinel = new Alert();
        sentinel.setId(NO_ALERT_ID);

        long now = System.currentTimeMillis();

        int typeId = candidate.getTypeId();
        long fieldId = candidate.getFieldId();

        return latestActiveByTypeAndField(dm, typeId, fieldId)
                .defaultIfEmpty(sentinel)
                .flatMapMaybe(active -> {
                    if (active.getId() != NO_ALERT_ID) {
                        return Maybe.empty();
                    }
                    return latestResolvedByTypeAndField(dm, typeId, fieldId)
                            .defaultIfEmpty(sentinel)
                            .flatMapMaybe(resolved -> {
                                if (resolved.getId() != NO_ALERT_ID && isSuppressed(resolved, candidate, now)) {
                                    return Maybe.empty();
                                }
                                return dm.insertAlert(candidate)
                                        .subscribeOn(ioScheduler)
                                        .observeOn(observeScheduler())
                                        .map(id -> {
                                            candidate.setId(id);
                                            return candidate;
                                        })
                                        .toMaybe();
                            });
                });
    }

    /**
     * Returns {@code true} when a newly evaluated candidate should be suppressed because the last
     * resolved alert has not yet "expired" for the candidate forecast time.
     * <p>
     * The end of the resolved event is computed as {@code baseAt + durationMs}, where {@code baseAt}
     * is taken from (in order) the resolved alert forecast time, resolved time, or creation time,
     * and {@code durationMs} is taken from (in order) the resolved alert duration, the candidate
     * duration, or a default value.
     * </p>
     *
     * @param latestResolved the most recent resolved alert for the same type/field
     * @param candidate      the newly evaluated candidate
     * @param nowMs          current time used as fallback when the candidate has no forecast time
     * @return {@code true} to skip inserting the candidate, {@code false} otherwise
     */
    private boolean isSuppressed(Alert latestResolved, Alert candidate, long nowMs) {
        long candidateAt = candidate != null && candidate.getForecastAt() > 0L
                ? candidate.getForecastAt()
                : nowMs;

        long baseAt = latestResolved.getForecastAt();
        if (baseAt <= 0L) {
            long resolvedAt = latestResolved.getResolvedAt();
            baseAt = resolvedAt > 0L ? resolvedAt : latestResolved.getCreatedAt();
        }
        if (baseAt <= 0L) {
            return false;
        }

        long durationMs = latestResolved.getDurationMs();
        if (durationMs <= 0L && candidate != null) {
            durationMs = candidate.getDurationMs();
        }
        if (durationMs <= 0L) {
            durationMs = DEFAULT_EVENT_DURATION_MS;
        }

        long endAt = baseAt + durationMs;
        return candidateAt < endAt;
    }

    /**
     * Retrieves the most recent unresolved alert for a type and field.
     */
    private Maybe<Alert> latestActiveByTypeAndField(DataManager dm, int typeId, long fieldId) {
        return dm.findLatestActiveByTypeAndField(typeId, fieldId)
                .subscribeOn(ioScheduler)
                .observeOn(ioScheduler);
    }

    /**
     * Retrieves the most recent resolved alert for a type and field.
     */
    private Maybe<Alert> latestResolvedByTypeAndField(DataManager dm, int typeId, long fieldId) {
        return dm.findLatestResolvedByTypeAndField(typeId, fieldId)
                .subscribeOn(ioScheduler)
                .observeOn(ioScheduler);
    }
}
