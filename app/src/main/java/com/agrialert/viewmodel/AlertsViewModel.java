package com.agrialert.viewmodel;

import com.agrialert.data_manager.ActivatedAlerts;
import com.agrialert.data_manager.Alert;
import com.agrialert.data_manager.AlertType;
import com.agrialert.data_manager.DataManager;

import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;

public class AlertsViewModel {

    private final DataManager dm;

    public AlertsViewModel(DataManager dm) {
        this.dm = dm;
    }

    public Flowable<List<AlertType>> getAllAlertTypes() {
        return dm.getAllAlertTypes();
    }

    public Flowable<ActivatedAlerts> getActivatedAlertsFromField(int fieldId) {
        return dm.getActivatedAlertsFromField(fieldId);
    }

    public Flowable<List<Alert>> getResolvedAlerts() {
        return dm.getResolvedAlerts();
    }

    public Flowable<List<Alert>> getActiveAlerts() {
        return dm.getActiveAlerts();
    }


    public Completable setAlertResolved(long id) {
        return dm.setAlertResolved(id);
    }

    public Completable setAlertActive(long id) {
        return dm.setAlertActive(id);
    }





}
