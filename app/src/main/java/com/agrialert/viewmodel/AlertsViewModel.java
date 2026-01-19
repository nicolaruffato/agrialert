package com.agrialert.viewmodel;

import com.agrialert.data_manager.ActivatedAlerts;
import com.agrialert.data_manager.Alert;
import com.agrialert.data_manager.AlertType;
import com.agrialert.data_manager.DataManager;

import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * ViewModel for managing and interacting with alerts data.
 * Acts as a bridge between the DataManager and the UI components for alert-related operations.
 */
public class AlertsViewModel {

    /** The data manager responsible for low-level data operations. */
    private final DataManager dm;

    /**
     * Constructs a new AlertsViewModel.
     *
     * @param dm The DataManager to be used for data operations.
     */
    public AlertsViewModel(DataManager dm) {
        this.dm = dm;
    }

    /**
     * Retrieves all possible types of alerts.
     *
     * @return A Flowable emitting a list of AlertType objects.
     */
    public Flowable<List<AlertType>> getAllAlertTypes() {
        return dm.getAllAlertTypes();
    }

    /**
     * Retrieves all activated alerts configuration for a specific field.
     *
     * @param fieldId The unique identifier of the field.
     * @return A Flowable emitting the ActivatedAlerts configuration.
     */
    public Flowable<ActivatedAlerts> getActivatedAlertsFromField(int fieldId) {
        return dm.getActivatedAlertsFromField(fieldId);
    }

    /**
     * Retrieves the list of alerts that have been marked as resolved.
     *
     * @return A Flowable emitting a list of resolved Alert objects.
     */
    public Flowable<List<Alert>> getResolvedAlerts() {
        return dm.getResolvedAlerts();
    }

    /**
     * Retrieves the list of currently active (unresolved) alerts.
     *
     * @return A Flowable emitting a list of active Alert objects.
     */
    public Flowable<List<Alert>> getActiveAlerts() {
        return dm.getActiveAlerts();
    }

    /**
     * Marks an alert as resolved.
     *
     * @param id The unique identifier of the alert to resolve.
     * @return A Completable that completes when the operation is finished.
     */
    public Completable setAlertResolved(long id) {
        return dm.setAlertResolved(id);
    }

    /**
     * Marks a previously resolved alert as active again.
     *
     * @param id The unique identifier of the alert to activate.
     * @return A Completable that completes when the operation is finished.
     */
    public Completable setAlertActive(long id) {
        return dm.setAlertActive(id);
    }

    /**
     * Retrieves only the active alerts triggered for a specific field.
     *
     * @param fieldId The unique identifier of the field.
     * @return A Flowable emitting a list of active Alert objects for the field.
     */
    public Flowable<List<Alert>> getActiveAlertsFromField(int fieldId) {
        return dm.getActiveAlertsFromField(fieldId);
    }
}
