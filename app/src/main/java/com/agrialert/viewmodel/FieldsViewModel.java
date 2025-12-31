package com.agrialert.viewmodel;

import kotlin.Pair;


import com.agrialert.data_manager.ActivatedAlerts;
import com.agrialert.data_manager.DataManager;
import com.agrialert.data_manager.Field;
import com.agrialert.data_manager.FieldsGroup;
import com.agrialert.data_manager.GroupWithFields;

import io.reactivex.rxjava3.core.Flowable;


import java.util.List;

import io.reactivex.rxjava3.core.Completable;

public class FieldsViewModel {

    private final DataManager dm;
    // FIELD TEMPORANEO (non salvato)
    private Field pendingField;
    public boolean isFieldPending = false;

    public FieldsViewModel(DataManager dm) {
        this.dm = dm;
    }

    // --------- TEMP FIELD ---------
    public void setPendingField(Field field) {
        this.pendingField = field;
    }

    public Field getPendingField() {
        return pendingField;
    }

    public void clearPendingField() {
        pendingField = null;
    }

    // READ
    public Flowable<List<GroupWithFields>> getAllGroups() {
        return dm.getAllGroups();
    }

    public Flowable<GroupWithFields> getGroupByName(String name) {
        return dm.getGroupByName(name);
    }

    // CREATE
    public Completable insertGroup(FieldsGroup group) {
        return dm.insertGroup(group);
    }

    public Completable insertField(Field field) {
        return dm.insertField(field);
    }

    // UPDATE
    public Completable updateField(Field field) {
        return dm.updateField(field);
    }

    public Completable updateGroup(FieldsGroup group) {
        return dm.updateGroup(group);
    }

    // DELETE
    public Completable deleteField(Field field) {
        return dm.deleteField(field);
    }

    public Completable deleteGroup(FieldsGroup group) {
        return dm.deleteGroup(group);
    }

    public Completable addAlertToField(int fieldId, int alertTypeId, double threshold) {
        return dm.addAlertToField(fieldId, alertTypeId, threshold);
    }
    public Completable updateAlertsToField(int fieldId, List<Pair<Integer, Double>> alertsTypeWithThresholds){
        return dm.updateAlertsToField(fieldId, alertsTypeWithThresholds);
    }

    public Flowable<ActivatedAlerts> getActivatedAlertsFromField(int fieldId) {
        return dm.getActivatedAlertsFromField(fieldId);
    }


}
