package com.agrialert.viewmodel;

import com.agrialert.data_manager.ActivatedAlerts;
import com.agrialert.data_manager.Alert;
import com.agrialert.data_manager.AlertType;
import com.agrialert.data_manager.DataManager;
import com.google.firebase.perf.metrics.AddTrace;

import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class AlertsViewModel {

    private static final boolean enable_traces = true;

    private final DataManager dm;

    public AlertsViewModel(DataManager dm) {
        this.dm = dm;
    }

    @AddTrace(name = "getAllGroupsTrace", enabled = enable_traces)
    public Flowable<List<AlertType>> getAllAlertTypes() {
        return dm.getAllAlertTypes();
    }

    @AddTrace(name = "getActivatedAlertsFromFieldTrace", enabled = enable_traces)
    public Flowable<ActivatedAlerts> getActivatedAlertsFromField(int fieldId) {
        return dm.getActivatedAlertsFromField(fieldId);
    }

    @AddTrace(name = "getResolvedAlertsTrace", enabled = enable_traces)
    public Flowable<List<Alert>> getResolvedAlerts() {
        return dm.getResolvedAlerts();
    }

    @AddTrace(name = "getActiveAlertsTrace", enabled = enable_traces)
    public Flowable<List<Alert>> getActiveAlerts() {
        return dm.getActiveAlerts();
    }

    @AddTrace(name = "setAlertResolvedTrace", enabled = enable_traces)
    public Completable setAlertResolved(long id) {
        return dm.setAlertResolved(id);
    }

    @AddTrace(name = "setAlertActiveTrace", enabled = enable_traces)
    public Completable setAlertActive(long id) {
        return dm.setAlertActive(id);
    }

    public Flowable<List<Alert>> getActiveAlertsFromField(int fieldId) {
        return dm.getActiveAlertsFromField(fieldId);
    }

    public Flowable<List<Alert>> getAlertsFromField(int fieldId) {
        return dm.getAlertsFromField(fieldId);
    }

    public Maybe<Alert> findLatestActiveByField(long fieldId) {
        return dm.findLatestActiveByField(fieldId);
    }

    public Completable deleteAlert(int alertId) {
        return dm.deleteAlert(alertId);
    }

}
