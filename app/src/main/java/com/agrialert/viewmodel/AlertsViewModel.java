package com.agrialert.viewmodel;

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




}
