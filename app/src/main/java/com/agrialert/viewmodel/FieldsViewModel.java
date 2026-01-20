package com.agrialert.viewmodel;

import io.reactivex.rxjava3.core.Single;
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

/**
 * ViewModel for managing agricultural fields and field groups.
 * Provides a high-level API for UI components to perform CRUD operations
 * and manage temporary field states during the creation process.
 */
public class FieldsViewModel {

    private static final boolean enable_traces = true;

    /** The data manager used for persistent storage operations. */
    private final DataManager dm;

    /** A temporary field object used during the multi-step creation process. */
    public Field pendingField = null;

    /** Flag indicating whether a field creation process is currently in progress. */
    public boolean isFieldPending = false;

    /**
     * Constructs a new FieldsViewModel.
     *
     * @param dm The DataManager to be used for data operations.
     */
    public FieldsViewModel(DataManager dm) {
        this.dm = dm;
    }

    // --------- TEMPORARY FIELD MANAGEMENT ---------

    /**
     * Sets the temporary field being created.
     *
     * @param field The field to set as pending.
     */
    public void setPendingField(Field field) {
        this.pendingField = field;
    }

    /**
     * Retrieves the current temporary field.
     *
     * @return The pending field, or null if none exists.
     */
    public Field getPendingField() {
        return pendingField;
    }

    /**
     * Clears the temporary field state.
     */
    public void clearPendingField() {
        pendingField = null;
    }

    // --------- READ OPERATIONS ---------

    /**
     * Retrieves all field groups along with their associated fields.
     *
     * @return A Flowable emitting a list of groups with fields.
     */
    // READ
    @AddTrace(name = "getAllGroupsTrace", enabled = enable_traces)
    public Flowable<List<GroupWithFields>> getAllGroups() {
        return dm.getAllGroups();
    }

    /**
     * Retrieves a specific group by its unique name.
     *
     * @param name The name of the group.
     * @return A Single emitting the group data.
     */
    @AddTrace(name = "getGroupByNameTrace", enabled = enable_traces)
    public Single<GroupWithFields> getGroupByName(String name) {
        return dm.getGroupByName(name);
    }

    /**
     * Inserts a new group into the database.
     *
     * @param group The group object to insert.
     * @return A Completable for the operation.
     */
    @AddTrace(name = "insertGroupTrace", enabled = enable_traces)
    public Completable insertGroup(FieldsGroup group) {
        return dm.insertGroup(group);
    }

    /**
     * Inserts a new field into the database.
     *
     * @param field The field object to insert.
     * @return A Completable for the operation.
     */
    @AddTrace(name = "insertFieldTrace", enabled = enable_traces)
    public Completable insertField(Field field) {
        return dm.insertField(field);
    }

    // --------- UPDATE OPERATIONS ---------

    /**
     * Updates an existing field's information.
     *
     * @param field The updated field object.
     * @return A Completable for the operation.
     */
    // UPDATE
    @AddTrace(name = "updateFieldTrace", enabled = enable_traces)
    public Completable updateField(Field field) {
        return dm.updateField(field);
    }

    /**
     * Updates an existing group's information.
     *
     * @param group The updated group object.
     * @return A Completable for the operation.
     */
    @AddTrace(name = "updateGroupTrace", enabled = enable_traces)
    public Completable updateGroup(FieldsGroup group) {
        return dm.updateGroup(group);
    }

    /**
     * Deletes a field from the database.
     *
     * @param field The field object to delete.
     * @return A Completable for the operation.
     */
    // DELETE
    @AddTrace(name = "deleteFieldTrace", enabled = enable_traces)
    public Completable deleteField(Field field) {
        return dm.deleteField(field);
    }

    /**
     * Deletes a group from the database.
     *
     * @param group The group object to delete.
     * @return A Completable for the operation.
     */
    @AddTrace(name = "deleteGroupTrace", enabled = enable_traces)
    public Completable deleteGroup(FieldsGroup group) {
        return dm.deleteGroup(group);
    }

    /**
     * Adds an alert configuration to a specific field.
     *
     * @param fieldId     The ID of the field.
     * @param alertTypeId The ID of the alert type.
     * @param threshold   The threshold values for the alert.
     * @return A Completable for the operation.
     */
    @AddTrace(name= "addAlertToFieldTrace", enabled = enable_traces)
    public Completable addAlertToField(int fieldId, int alertTypeId, Threshold threshold) {
        return dm.addAlertToField(fieldId, alertTypeId, threshold);
    }

    /**
     * Batch updates alert configurations for a specific field.
     *
     * @param fieldId                   The ID of the field.
     * @param alertsTypeWithThresholds  A list of alert types paired with their thresholds.
     * @return A Completable for the operation.
     */

    @AddTrace(name = "updateAlertToFieldTrace", enabled = enable_traces)
    public Completable updateAlertsToField(int fieldId, List<Pair<Integer, Threshold>> alertsTypeWithThresholds){
        return dm.updateAlertsToField(fieldId, alertsTypeWithThresholds);
    }

    /**
     * Retrieves the activated alerts configuration for a specific field.
     *
     * @param fieldId The unique identifier of the field.
     * @return A Flowable emitting the activated alerts.
     */
    @AddTrace(name = "getActivatedAlertsFromFieldTrace", enabled = enable_traces)
    public Flowable<ActivatedAlerts> getActivatedAlertsFromField(int fieldId) {
        return dm.getActivatedAlertsFromField(fieldId);
    }

    /**
     * Retrieves a specific field by its unique ID.
     *
     * @param fieldId The unique identifier of the field.
     * @return A Single emitting the field object.
     */
    @AddTrace(name = "getFieldByIdTrace", enabled = enable_traces)
    public Single<Field> getFieldById(int fieldId) {
        return  dm.getFieldById(fieldId);
    }


}
