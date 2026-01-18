package com.agrialert.viewmodel;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;
import kotlin.Pair;


import com.agrialert.data_manager.ActivatedAlerts;
import com.agrialert.data_manager.DataManager;
import com.agrialert.data_manager.Field;
import com.agrialert.data_manager.FieldsGroup;
import com.agrialert.data_manager.GroupWithFields;
import com.agrialert.data_manager.Threshold;
import com.google.firebase.perf.metrics.AddTrace;

import io.reactivex.rxjava3.core.Flowable;


import java.util.List;

import io.reactivex.rxjava3.core.Completable;

public class FieldsViewModel {

    private static final boolean enable_traces = true;

    private final DataManager dm;
    // FIELD TEMPORANEO (non salvato)
    public Field pendingField = null;
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
    @AddTrace(name = "getAllGroupsTrace", enabled = enable_traces)
    public Flowable<List<GroupWithFields>> getAllGroups() {
        return dm.getAllGroups();
    }

    @AddTrace(name = "getGroupByNameTrace", enabled = enable_traces)
    public Single<GroupWithFields> getGroupByName(String name) {
        return dm.getGroupByName(name);
    }

    // CREATE
    @AddTrace(name = "insertGroupTrace", enabled = enable_traces)
    public Completable insertGroup(FieldsGroup group) {
        return dm.insertGroup(group);
    }

    @AddTrace(name = "insertFieldTrace", enabled = enable_traces)
    public Completable insertField(Field field) {
        return dm.insertField(field);
    }

    // UPDATE
    @AddTrace(name = "updateFieldTrace", enabled = enable_traces)
    public Completable updateField(Field field) {
        return dm.updateField(field);
    }

    @AddTrace(name = "updateGroupTrace", enabled = enable_traces)
    public Completable updateGroup(FieldsGroup group) {
        return dm.updateGroup(group);
    }

    // DELETE
    @AddTrace(name = "deleteFieldTrace", enabled = enable_traces)
    public Completable deleteField(Field field) {
        return dm.deleteField(field);
    }

    @AddTrace(name = "deleteGroupTrace", enabled = enable_traces)
    public Completable deleteGroup(FieldsGroup group) {
        return dm.deleteGroup(group);
    }

    @AddTrace(name= "addAlertToFieldTrace", enabled = enable_traces)
    public Completable addAlertToField(int fieldId, int alertTypeId, Threshold threshold) {
        return dm.addAlertToField(fieldId, alertTypeId, threshold);
    }

    @AddTrace(name = "updateAlertToFieldTrace", enabled = enable_traces)
    public Completable updateAlertsToField(int fieldId, List<Pair<Integer, Threshold>> alertsTypeWithThresholds){
        return dm.updateAlertsToField(fieldId, alertsTypeWithThresholds);
    }

    @AddTrace(name = "getActivatedAlertsFromFieldTrace", enabled = enable_traces)
    public Flowable<ActivatedAlerts> getActivatedAlertsFromField(int fieldId) {
        return dm.getActivatedAlertsFromField(fieldId);
    }

    @AddTrace(name = "getFieldByIdTrace", enabled = enable_traces)
    public Single<Field> getFieldById(int fieldId) {
        return  dm.getFieldById(fieldId);
    }


}
