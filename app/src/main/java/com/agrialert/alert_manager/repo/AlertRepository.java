package com.agrialert.alert_manager.repo;

import android.content.Context;
import android.util.Log;

import com.agrialert.alert_manager.DataManagerConnector;
import com.agrialert.alert_manager.domain.AlertEvaluator;
import com.agrialert.api.OpenMeteoApiClient;
import com.agrialert.api.WeatherApiResponse;
import com.agrialert.data_manager.ActivatedAlerts;
import com.agrialert.data_manager.Alert;
import com.agrialert.data_manager.DataManager;
import com.agrialert.data_manager.Field;
import com.agrialert.data_manager.GroupWithFields;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
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
 * All operations are executed through the bound {@link DataManager} service via
 * {@link DataManagerConnector}.
 */
public class AlertRepository {

    private static final String TAG = "AlertRepository";
    private static final long BIND_TIMEOUT_MS = 5_000L;
    private static final int NO_ALERT_ID = -1;

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

    // ------------------- OBSERVE -------------------

    /**
     * Observes alerts as a stream of lists.
     *
     * @param resolved whether to observe resolved alerts
     * @return a {@link Flowable} emitting alert lists
     */
    public Flowable<List<Alert>> observeAlertsStream(boolean resolved) {
        return observeAlerts(resolved);
    }

    /**
     * Observes alerts as a model list for UI consumption.
     *
     * @param resolved whether to observe resolved alerts
     * @return a {@link Flowable} emitting alert lists
     */
    public Flowable<List<Alert>> observeAlertsModel(boolean resolved) {
        return observeAlerts(resolved);
    }

    /**
     * Observes alerts from {@link DataManager} with the configured schedulers.
     *
     * @param resolved whether to observe resolved alerts
     * @return a {@link Flowable} emitting alert lists
     */
    public Flowable<List<Alert>> observeAlerts(boolean resolved) {
        return DataManagerConnector.withFlowable(appContext, dm ->
                dm.cleanupAlerts()
                        .andThen(dm.observeAlerts(resolved))
                        .subscribeOn(ioScheduler)
                        .observeOn(observeScheduler())
        );
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

    /**
     * Updates the resolved flag for an alert in a fire-and-forget manner.
     *
     * @param id       alert identifier
     * @param resolved resolved state to set
     */
    public void setResolved(long id, boolean resolved) {
        setResolvedRx(id, resolved).subscribe();
    }

    // ------------------- SYNC METEO -------------------

    /**
     * Fetches groups from {@link DataManager} and performs a full sync.
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

    /**
     * Performs a sync for a known list of groups.
     *
     * @param groups groups with their fields
     * @return a {@link Single} emitting the sync result
     */
    public Single<WeatherSyncResult> syncWeatherForGroups(List<GroupWithFields> groups) {
        if (groups == null || groups.isEmpty()) {
            return Single.just(new WeatherSyncResult(null, Collections.emptyList()));
        }
        return DataManagerConnector.withSingle(appContext, BIND_TIMEOUT_MS, dm ->
                dm.cleanupAlerts().andThen(syncWithDataManager(dm, groups))
        );
    }

    /**
     * Performs a sync from a list of fields without requiring group objects.
     *
     * @param fields list of fields to process
     * @return a {@link Single} emitting the sync result
     */
    public Single<WeatherSyncResult> syncWeatherForFields(List<Field> fields) {
        if (fields == null || fields.isEmpty()) {
            return Single.just(new WeatherSyncResult(null, Collections.emptyList()));
        }
        return DataManagerConnector.withSingle(appContext, BIND_TIMEOUT_MS, dm ->
                dm.cleanupAlerts().andThen(syncFields(dm, fields))
        );
    }

    /**
     * Runs a sync for the provided fields and emits a summary to the callback.
     *
     * @param fields       list of fields to process
     * @param infoCallback optional callback to receive a textual summary
     * @return a {@link Completable} that completes when the sync finishes
     */
    public Completable syncWeatherAsyncCompletable(List<Field> fields, Consumer<String> infoCallback) {
        return syncWeatherForFields(fields)
                .map(result -> buildSummary(result.response, result.created))
                .subscribeOn(ioScheduler)
                .observeOn(observeScheduler())
                .doOnSuccess(summary -> {
                    if (infoCallback != null) {
                        infoCallback.accept(summary);
                    }
                })
                .doOnError(error -> Log.w(TAG, "Sync meteo (campi) fallita", error))
                .ignoreElement();
    }

    /**
     * Runs a sync for the provided fields without a callback.
     *
     * @param fields list of fields to process
     */
    public void syncWeatherAsync(List<Field> fields) {
        syncWeatherAsync(fields, null);
    }

    /**
     * Runs a sync for the provided fields and optionally notifies a callback.
     *
     * @param fields       list of fields to process
     * @param infoCallback optional callback to receive a textual summary
     */
    public void syncWeatherAsync(List<Field> fields, Consumer<String> infoCallback) {
        syncWeatherAsyncCompletable(fields, infoCallback).subscribe();
    }

    // Usa DataManager per recuperare i gruppi reali e lanciare la sync
    /**
     * Runs a full sync across all groups and optionally notifies a callback.
     *
     * @param infoCallback optional callback to receive a textual summary
     */
    public void syncWeatherAsync(Consumer<String> infoCallback) {
        syncAllGroups()
                .map(result -> buildSummary(result.response, result.created))
                .subscribeOn(ioScheduler)
                .observeOn(observeScheduler())
                .doOnSuccess(summary -> {
                    if (infoCallback != null) {
                        infoCallback.accept(summary);
                    }
                })
                .doOnError(error -> Log.w(TAG, "Sync meteo (gruppi) fallita", error))
                .ignoreElement()
                .subscribe();
    }

    /**
     * Runs a full sync across all groups without a callback.
     */
    public void syncWeatherAsync() {
        syncAllGroups()
                .subscribeOn(ioScheduler)
                .observeOn(ioScheduler)
                .doOnError(error -> Log.w(TAG, "Sync meteo fallita", error))
                .ignoreElement()
                .subscribe();
    }

    // ------------------- MODEL -------------------

    /**
     * Holds the outcome of a weather sync, including the response and created alerts.
     */
    public static class WeatherSyncResult {
        public final WeatherApiResponse response;
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
     * Returns {@code true} if a timestamp is within the last 12 hours.
     *
     * @param timestamp epoch milliseconds
     * @return {@code true} when recent, {@code false} otherwise
     */
    private boolean isRecent(long timestamp) {
        return System.currentTimeMillis() - timestamp < TimeUnit.HOURS.toMillis(12);
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

        return Flowable.fromIterable(groups)
                .concatMapSingle(group -> {
                    List<Field> fields = group != null ? group.getFields() : null;
                    String groupName = resolveGroupName(group, fields);
                    int threshold = computeThreshold(fields);
                    return processFields(dm, fields, groupName, threshold);
                })
                .flatMapIterable(list -> list)
                .toList()
                .flatMap(alerts -> persistCandidates(dm, alerts))
                .map(saved -> new WeatherSyncResult(null, saved))
                .subscribeOn(ioScheduler)
                .observeOn(ioScheduler);
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
        String groupName = deriveGroupName(fields);
        int threshold = computeThreshold(fields);

        return processFields(dm, fields, groupName, threshold)
                .flatMap(alerts -> persistCandidates(dm, alerts))
                .map(saved -> new WeatherSyncResult(null, saved))
                .subscribeOn(ioScheduler)
                .observeOn(ioScheduler);
    }

    /**
     * Resolves the group name from a {@link GroupWithFields} or falls back to the fields list.
     *
     * @param group  group wrapper that may include a name
     * @param fields fields used as a fallback
     * @return resolved group name
     */
    private String resolveGroupName(GroupWithFields group, List<Field> fields) {
        if (group != null && group.getGroup() != null && group.getGroup().getName() != null) {
            return group.getGroup().getName();
        }
        return deriveGroupName(fields);
    }

    /**
     * Derives a group name from the provided fields list.
     *
     * @param fields fields used as a source for group name
     * @return resolved group name, or a default label when missing
     */
    private String deriveGroupName(List<Field> fields) {
        if (fields != null && !fields.isEmpty()) {
            String candidate = fields.get(0).getGroupName();
            if (candidate != null && !candidate.isEmpty()) {
                return candidate;
            }
        }
        return "Gruppo";
    }

    /**
     * Computes the aggregation threshold for a group based on the number of fields.
     *
     * @param fields fields in the group
     * @return threshold used to aggregate alerts
     */
    private int computeThreshold(List<Field> fields) {
        if (fields == null || fields.isEmpty()) {
            return 1;
        }
        return (fields.size() / 2) + 1;
    }

    /**
     * Evaluates alerts for each field, then aggregates by group and threshold.
     *
     * @param dm         bound {@link DataManager} instance
     * @param fields     fields to process
     * @param groupName  group name to apply to aggregated alerts
     * @param threshold  minimum count required to aggregate by type
     * @return a {@link Single} emitting the aggregated list of alerts
     */
    private Single<List<Alert>> processFields(DataManager dm,
                                              List<Field> fields,
                                              String groupName,
                                              int threshold) {
        if (fields == null || fields.isEmpty()) {
            return Single.just(Collections.emptyList());
        }
        final String resolvedGroup = groupName != null ? groupName : deriveGroupName(fields);
        final int groupThreshold = Math.max(threshold, 1);

        return Flowable.fromIterable(fields)
                .concatMapSingle(field -> evalFieldAlerts(dm, field, resolvedGroup))
                .flatMapIterable(list -> list)
                .toList()
                .map(alerts -> aggregateByGroup(alerts, resolvedGroup, groupThreshold));
    }

    /**
     * Evaluates alerts for a single field by fetching weather and active thresholds.
     *
     * @param dm        bound {@link DataManager} instance
     * @param field     field to evaluate
     * @param groupName group name applied to created alerts
     * @return a {@link Single} emitting alert candidates for the field
     */
    private Single<List<Alert>> evalFieldAlerts(DataManager dm, Field field, String groupName) {
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
                .flatMap(activated -> weatherSingle.map(response ->
                        evaluator.evaluate(
                                response,
                                field,
                                groupName,
                                activated != null && activated.getAlerts() != null
                                        ? activated.getAlerts()
                                        : Collections.emptyList()
                        )
                ))
                .onErrorReturn(error -> {
                    Log.w(TAG, "Errore durante la valutazione per il campo id=" + field.getId(), error);
                    return Collections.emptyList();
                });
    }

    /**
     * Aggregates alerts by type and emits a group-level alert when the threshold is met.
     *
     * @param alerts    alert candidates
     * @param groupName group name to apply to aggregated alerts
     * @param threshold minimum count required to emit an aggregated alert
     * @return aggregated alerts for the group
     */
    private List<Alert> aggregateByGroup(List<Alert> alerts, String groupName, int threshold) {
        if (alerts == null || alerts.isEmpty()) return Collections.emptyList();
        Map<Integer, Integer> counts = new HashMap<>();
        Map<Integer, Alert> sample = new HashMap<>();

        for (Alert alert : alerts) {
            int key = alert.getTypeId();
            counts.put(key, counts.getOrDefault(key, 0) + 1);
            sample.putIfAbsent(key, alert);
        }

        List<Alert> result = new ArrayList<>();
        for (Map.Entry<Integer, Integer> entry : counts.entrySet()) {
            if (entry.getValue() >= threshold) {
                Alert base = sample.get(entry.getKey());
                if (base == null) continue;
                Alert aggregated = new Alert();
                aggregated.setTypeId(base.getTypeId());
                aggregated.setTitle(base.getTitle());
                aggregated.setDescription(base.getDescription());
                aggregated.setIconRes(base.getIconRes());
                aggregated.setFieldId(0);
                aggregated.setGroupName(groupName != null ? groupName : deriveGroupName(null));
                aggregated.setFieldAddress(groupName != null ? groupName : "Gruppo");
                aggregated.setCreatedAt(System.currentTimeMillis());
                aggregated.setResolved(false);
                result.add(aggregated);
            }
        }
        return result;
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
                .concatMapSingle(candidate -> insertIfNew(dm, candidate))
                .filter(entity -> entity != null)
                .toList()
                .subscribeOn(ioScheduler)
                .observeOn(ioScheduler);
    }

    /**
     * Inserts a candidate alert only if no recent unresolved alert of the same type exists.
     *
     * @param dm        bound {@link DataManager} instance
     * @param candidate alert candidate to insert
     * @return a {@link Single} emitting the inserted alert or {@code null} when skipped
     */
    private Single<Alert> insertIfNew(DataManager dm, Alert candidate) {
        Alert sentinel = new Alert();
        sentinel.setId(NO_ALERT_ID);

        return latestByTypeAndGroup(dm, candidate.getTypeId(), candidate.getGroupName())
                .switchIfEmpty(Single.just(sentinel))
                .flatMap(latest -> {
                    if (latest.getId() != NO_ALERT_ID
                            && !latest.isResolved()
                            && isRecent(latest.getCreatedAt())) {
                        return Single.just((Alert) null);
                    }
                    return dm.insertAlert(candidate)
                            .subscribeOn(ioScheduler)
                            .observeOn(observeScheduler())
                            .map(id -> {
                                candidate.setId(id);
                                return candidate;
                            });
                });
    }

    /**
     * Retrieves the most recent alert for a type and group.
     *
     * @param dm        bound {@link DataManager} instance
     * @param typeId    alert type identifier
     * @param groupName group name filter
     * @return a {@link Maybe} emitting the latest alert if present
     */
    private Maybe<Alert> latestByTypeAndGroup(DataManager dm, int typeId, String groupName) {
        return dm.findLatestByTypeAndGroup(typeId, groupName)
                .subscribeOn(ioScheduler)
                .observeOn(ioScheduler);
    }

    /**
     * Builds a human-readable summary for a sync execution.
     *
     * @param response weather response used during the sync
     * @param created  alerts created during the sync
     * @return formatted summary text
     */
    private String buildSummary(WeatherApiResponse response, List<Alert> created) {
        int newAlerts = created != null ? created.size() : 0;
        if (response == null || response.currentWeather == null) {
            return String.format(Locale.getDefault(),
                    "Ultima richiesta eseguita. Nuovi alert=%d",
                    newAlerts);
        }
        double temp = response.currentWeather.temperature;
        double wind = response.currentWeather.windspeed;
        double humidity = firstOrNaN(response.hourly != null ? response.hourly.relativeHumidity2m : null);
        double precipitation = firstOrNaN(response.hourly != null ? response.hourly.precipitation : null);

        String tempStr = formatValue(temp, "C");
        String windStr = formatValue(wind, "km/h");
        String humStr = formatValue(humidity, "%");
        String rainStr = formatValue(precipitation, "mm/h");

        return String.format(Locale.getDefault(),
                "Ultima richiesta: T=%s, vento=%s, umidita=%s, pioggia=%s, nuovi alert=%d",
                tempStr, windStr, humStr, rainStr, newAlerts);
    }

    /**
     * Formats a numeric value with one decimal and a suffix.
     *
     * @param value  numeric value
     * @param suffix suffix to append
     * @return formatted string, or "-" when the value is not a number
     */
    private String formatValue(double value, String suffix) {
        if (Double.isNaN(value)) return "-";
        return String.format(Locale.getDefault(), "%.1f %s", value, suffix);
    }

    /**
     * Returns the first value in the list or {@link Double#NaN} when unavailable.
     *
     * @param list list to read from
     * @return first value or {@link Double#NaN}
     */
    private double firstOrNaN(List<Double> list) {
        if (list == null || list.isEmpty() || list.get(0) == null) {
            return Double.NaN;
        }
        return list.get(0);
    }
}
