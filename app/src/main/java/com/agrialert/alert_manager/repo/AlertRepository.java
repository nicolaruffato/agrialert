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
 * Orchestratore per fetch meteo, valutazione soglie, persistenza e toggling stato.
 * Tutte le operazioni passano dal servizio bound DataManager (tramite DataManagerConnector).
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

    private Scheduler observeScheduler() {
        return mainScheduler != null ? mainScheduler : ioScheduler;
    }

    // ------------------- OBSERVE -------------------

    public Flowable<List<Alert>> observeAlertsStream(boolean resolved) {
        return observeAlerts(resolved);
    }

    public Flowable<List<Alert>> observeAlertsModel(boolean resolved) {
        return observeAlerts(resolved);
    }

    public Flowable<List<Alert>> observeAlerts(boolean resolved) {
        return DataManagerConnector.withFlowable(appContext, dm ->
                dm.observeAlerts(resolved)
                        .subscribeOn(ioScheduler)
                        .observeOn(observeScheduler())
        );
    }

    // ------------------- MUTATE -------------------

    public Completable setResolvedRx(long id, boolean resolved) {
        return DataManagerConnector.withCompletable(appContext, dm ->
                dm.setResolved(id, resolved)
                        .subscribeOn(ioScheduler)
                        .observeOn(observeScheduler())
        );
    }

    public void setResolved(long id, boolean resolved) {
        setResolvedRx(id, resolved).subscribe();
    }

    // ------------------- SYNC METEO -------------------

    /**
     * Recupera i gruppi dal DataManager e lancia una sync completa.
     */
    public Single<WeatherSyncResult> syncAllGroups() {
        return DataManagerConnector.withSingle(appContext, BIND_TIMEOUT_MS, dm ->
                dm.getAllGroups()
                        .first(Collections.emptyList())
                        .subscribeOn(ioScheduler)
                        .observeOn(ioScheduler)
                        .flatMap(groups -> syncWithDataManager(dm, groups))
        );
    }

    /**
     * Esegue la sync partendo da una lista di gruppi gia' nota.
     */
    public Single<WeatherSyncResult> syncWeatherForGroups(List<GroupWithFields> groups) {
        if (groups == null || groups.isEmpty()) {
            return Single.just(new WeatherSyncResult(null, Collections.emptyList()));
        }
        return DataManagerConnector.withSingle(appContext, BIND_TIMEOUT_MS, dm -> syncWithDataManager(dm, groups));
    }

    /**
     * Compatibilita: sync a partire da una lista di campi (senza passare per i gruppi).
     */
    public Single<WeatherSyncResult> syncWeatherForFields(List<Field> fields) {
        if (fields == null || fields.isEmpty()) {
            return Single.just(new WeatherSyncResult(null, Collections.emptyList()));
        }
        return DataManagerConnector.withSingle(appContext, BIND_TIMEOUT_MS, dm -> syncFields(dm, fields));
    }

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

    public void syncWeatherAsync(List<Field> fields) {
        syncWeatherAsync(fields, null);
    }

    public void syncWeatherAsync(List<Field> fields, Consumer<String> infoCallback) {
        syncWeatherAsyncCompletable(fields, infoCallback).subscribe();
    }

    // Usa DataManager per recuperare i gruppi reali e lanciare la sync
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

    public void syncWeatherAsync() {
        syncAllGroups()
                .subscribeOn(ioScheduler)
                .observeOn(ioScheduler)
                .doOnError(error -> Log.w(TAG, "Sync meteo fallita", error))
                .ignoreElement()
                .subscribe();
    }

    // ------------------- MODEL -------------------

    public static class WeatherSyncResult {
        public final WeatherApiResponse response;
        public final List<Alert> created;

        public WeatherSyncResult(WeatherApiResponse response, List<Alert> created) {
            this.response = response;
            this.created = created;
        }
    }

    private boolean isRecent(long timestamp) {
        return System.currentTimeMillis() - timestamp < TimeUnit.HOURS.toMillis(12);
    }

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

    private String resolveGroupName(GroupWithFields group, List<Field> fields) {
        if (group != null && group.getGroup() != null && group.getGroup().getName() != null) {
            return group.getGroup().getName();
        }
        return deriveGroupName(fields);
    }

    private String deriveGroupName(List<Field> fields) {
        if (fields != null && !fields.isEmpty()) {
            String candidate = fields.get(0).getGroupName();
            if (candidate != null && !candidate.isEmpty()) {
                return candidate;
            }
        }
        return "Gruppo";
    }

    private int computeThreshold(List<Field> fields) {
        if (fields == null || fields.isEmpty()) {
            return 1;
        }
        return (fields.size() / 2) + 1;
    }

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

    private Maybe<Alert> latestByTypeAndGroup(DataManager dm, int typeId, String groupName) {
        return dm.findLatestByTypeAndGroup(typeId, groupName)
                .subscribeOn(ioScheduler)
                .observeOn(ioScheduler);
    }

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

    private String formatValue(double value, String suffix) {
        if (Double.isNaN(value)) return "-";
        return String.format(Locale.getDefault(), "%.1f %s", value, suffix);
    }

    private double firstOrNaN(List<Double> list) {
        if (list == null || list.isEmpty() || list.get(0) == null) {
            return Double.NaN;
        }
        return list.get(0);
    }
}
