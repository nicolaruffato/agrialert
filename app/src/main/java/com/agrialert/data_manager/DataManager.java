package com.agrialert.data_manager;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;


import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;
import kotlin.Pair;

public class DataManager extends Service {

    private final IBinder binder = new LocalBinder();
    private AppDatabase db;

    private FieldsDao fieldsDao;
    private AlertDao alertDao;

    public DataManager() {}

    @Override
    public void onCreate() {
        super.onCreate();
        db = AppDatabase.getDatabase(this);
        fieldsDao = db.fieldsDao();
        alertDao = db.alertDao();
    }

    /**
     * Retrieves all groups along with their associated fields from the database.
     *
     * @return A {@link Flowable} emitting a list of {@link GroupWithFields} objects.
     */
    public Flowable<List<GroupWithFields>> getAllGroups() {
        return fieldsDao.getGroups().subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    /**
     * Retrieves a specific group and its associated fields by the group's name.
     *
     * @param name The name of the group to retrieve.
     * @return A {@link Flowable} emitting the {@link GroupWithFields} object matching the specified name.
     */
    public Single<GroupWithFields> getGroupByName(String name) {
        return fieldsDao.getGroupByName(name).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    /**
     * Inserts a new group of fields into the database.
     *
     * @param group The {@link FieldsGroup} object to be inserted.
     * @return A {@link Completable} that represents the asynchronous operation.
     */
    public Completable insertGroup(FieldsGroup group) {
        return fieldsDao.insertGroup(group).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    /**
     * Inserts a new field into the database.
     *
     * @param field The {@link Field} object to be inserted.
     * @return A {@link Completable} that completes when the insertion is successful.
     */
    public Completable insertField(Field field) {
        return fieldsDao.insertField(field).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    /**
     * Retrieves a specific field by its unique identifier.
     *
     * @param fieldId The ID of the field to retrieve.
     * @return A {@link Single} emitting the {@link Field} object.
     */
    public Single<Field> getFieldById(int fieldId) {
        return fieldsDao.getFieldById(fieldId).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    /**
     * Associates a specific alert type with a field by inserting a relation with defined custom thresholds.
     * If treshold is null the default alertType treshold will be used.
     *
     * @param fieldId     The unique identifier of the field.
     * @param alertTypeId The unique identifier of the alert type to be added.
     * @param treshold    The {@link Threshold} object containing the limit values for the alert.
     * @return A {@link Completable} that represents the asynchronous operation.
     */
    public Completable addAlertToField(int fieldId, int alertTypeId, Threshold treshold) {
        if(treshold != null) {
            return fieldsDao.insertFieldAlertRelation(new AlertTypeCrossRef(alertTypeId, fieldId, treshold.getThreshold1(), treshold.getThreshold2()))
                    .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
        }
        else {
            return fieldsDao.insertFieldAlertRelation(new AlertTypeCrossRef(alertTypeId, fieldId, null, null))
                    .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
        }
    }


    /**
     * Updates the threshold values for a specific alert type associated with a field.
     * If treshold is null the default alertType treshold will be used.
     *
     * @param fieldId     The unique identifier of the field.
     * @param alertTypeId The unique identifier of the alert type.
     * @param treshold    The {@link Threshold} object containing the new threshold values.
     * @return A {@link Completable} that represents the asynchronous update operation.
     */
    public Completable updateAlertToField(int fieldId, int alertTypeId, Threshold treshold) {
        if(treshold != null) {
            return fieldsDao.updateFieldAlertRelation(new AlertTypeCrossRef(alertTypeId, fieldId, treshold.getThreshold1(), treshold.getThreshold2()))
                    .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
        }
        else {
            return fieldsDao.updateFieldAlertRelation(new AlertTypeCrossRef(alertTypeId, fieldId, null, null))
                    .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
        }
    }


    public Completable deleteAlertToField(int alertTypeId, int fieldId) {
        return fieldsDao.deleteFieldAlertRelation(new AlertTypeCrossRef(alertTypeId, fieldId, null, null));
    }

    public Completable deleteAlertsToField(List<Pair<Integer, Integer>> fieldAlertId) {
        List<AlertTypeCrossRef> crossRefs = new ArrayList<>();
        for(var pair : fieldAlertId) {
            crossRefs.add(new AlertTypeCrossRef(pair.getFirst(), pair.getSecond(), null, null));
        }
        return fieldsDao.deleteFieldAlertRelations(crossRefs);
    }

    /**
     * Updates multiple alert relations and their associated thresholds for a specific field.
     * If a threshold is null the default alertType treshold will be used.
     *
     * @param fieldId The unique identifier of the field to update.
     * @param alertsTypeWithThresholds A list of pairs, where each pair contains an alert type ID
     *                                 and its corresponding {@link Threshold} values.
     * @return A {@link Completable} that completes when the database update is successful.
     *
     */
    public Completable updateAlertsToField(int fieldId, List<Pair<Integer, Threshold>> alertsTypeWithThresholds) {
        List<AlertTypeCrossRef> crossRefs = new ArrayList<>();
        Map<Integer, Threshold> newThresholdsByType = new HashMap<>();

        List<Pair<Integer, Threshold>> safeInput = alertsTypeWithThresholds != null
                ? alertsTypeWithThresholds
                : Collections.emptyList();

        for (var pair : safeInput) {
            if (pair == null) continue;

            Integer typeId = pair.getFirst();
            Threshold threshold = pair.getSecond();
            if (typeId == null) continue;

            newThresholdsByType.put(typeId, threshold);

            if (threshold != null) {
                crossRefs.add(new AlertTypeCrossRef(typeId, fieldId, threshold.getThreshold1(), threshold.getThreshold2()));
            } else {
                crossRefs.add(new AlertTypeCrossRef(typeId, fieldId, null, null));
            }
        }

        return fieldsDao.getAlertsFromField(fieldId)
                .firstElement()
                .defaultIfEmpty(new ActivatedAlerts())
                .flatMapCompletable(current -> {
                    Set<Integer> toClearActive = new HashSet<>();

                    List<AlertWithThreshold> currentAlerts = current != null && current.getAlerts() != null
                            ? current.getAlerts()
                            : Collections.emptyList();

                    for (AlertWithThreshold oldAlert : currentAlerts) {
                        if (oldAlert == null || oldAlert.getAlertType() == null) continue;

                        int typeId = oldAlert.getAlertType().getId();
                        if (!newThresholdsByType.containsKey(typeId)) {
                            // Alert disabilitato -> rimuovi eventuale alert attivo in lista.
                            toClearActive.add(typeId);
                            continue;
                        }

                        Threshold oldEffective = oldAlert.getThreshold();
                        Threshold newEffective = newThresholdsByType.get(typeId) != null
                                ? newThresholdsByType.get(typeId)
                                : oldAlert.getAlertType().getDefaultThreshold();

                        if (!thresholdsEqual(oldEffective, newEffective)) {
                            // Threshold modificato -> invalida l'eventuale alert attivo e lascia che il sync rigeneri.
                            toClearActive.add(typeId);
                        }
                    }

                    Completable clearActive = Completable.complete();
                    if (!toClearActive.isEmpty()) {
                        clearActive = alertDao.deleteActiveByFieldAndTypes(fieldId, new ArrayList<>(toClearActive));
                    }

                    return clearActive
                            .andThen(fieldsDao.deleteAlertsForField(fieldId))
                            .andThen(fieldsDao.insertFieldAlertRelations(crossRefs));
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    private static boolean thresholdsEqual(Threshold a, Threshold b) {
        if (a == b) return true;
        if (a == null || b == null) return false;
        return Objects.equals(a.getThreshold1(), b.getThreshold1())
                && Objects.equals(a.getThreshold2(), b.getThreshold2());
    }

    /**
     * Inserts a new alert type into the database.
     *
     * @param alertType The {@link AlertType} object to be added.
     * @return A {@link Completable} that represents the asynchronous operation.
     */
    public Completable addAlertType(AlertType alertType) {
        return fieldsDao.insertAlertType(alertType).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    /**
     * Retrieves the user activated alerts associated with a specific field.
     *
     * @param fieldId The unique identifier of the field.
     * @return A {@link Flowable} emitting the {@link ActivatedAlerts} for the specified field.
     */
    public Flowable<ActivatedAlerts> getActivatedAlertsFromField(int fieldId) {
        return fieldsDao.getAlertsFromField(fieldId).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    /**
     * Deletes a specific field.
     *
     * @param field The {@link Field} object to be deleted.
     * @return A {@link Completable} that completes when the deletion is successful.
     */
    public Completable deleteField(Field field) {
        long fieldId = field != null ? field.getId() : 0L;

        Completable clearRelations = fieldId > 0L
                ? fieldsDao.deleteAlertsForField((int) fieldId)
                : Completable.complete();

        Completable clearAlerts = fieldId > 0L
                ? alertDao.deleteByFieldId(fieldId)
                : Completable.complete();

        return clearRelations
                .andThen(clearAlerts)
                .andThen(fieldsDao.deleteField(field))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    /**
     * Deletes a group of fields.
     * All the fields that were assigned to the deleted group will be assigned to the default group which cannot be
     * deleted.
     *
     * @param group The {@link FieldsGroup} object to be deleted.
     * @return A {@link Completable} that represents the asynchronous deletion operation.
     */
    public Completable deleteGroup(FieldsGroup group) {
        return fieldsDao.deleteGroup(group).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    /**
     * Updates an existing field's information.
     *
     * @param field The {@link Field} object containing the updated data.
     * @return A {@link Completable} that represents the asynchronous update operation.
     */
    public Completable updateField(Field field) {
        return fieldsDao.updateField(field).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    /**
     * Updates an existing group of fields.
     *
     * @param group The {@link FieldsGroup} object containing the updated data.
     * @return A {@link Completable} that represents the asynchronous update operation.
     */
    public Completable updateGroup(FieldsGroup group) {
        return fieldsDao.updateGroup(group).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }
    public Completable insertAlertType(AlertType alertType) {
        return fieldsDao.insertAlertType(alertType)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public Flowable<List<AlertType>> getAllAlertTypes() {
        return fieldsDao.getAllAlertTypes()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }


    // --- Alert ---
    public Flowable<List<Alert>> observeAlerts(boolean resolved) {
        return alertDao.observeByResolved(resolved)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public Flowable<List<Alert>> getResolvedAlerts() {
        return observeAlerts(true);
    }

    public Flowable<List<Alert>> getActiveAlerts() {
        return observeAlerts(false);
    }
    public Flowable<List<Alert>> getActiveAlertsFromField(int fieldId) {
        return alertDao.getActiveAlertsFromField(fieldId).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }
    public Flowable<List<Alert>> getAlertsFromField(int fieldId) {
        return alertDao.getAlertsFromField(fieldId).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    public Maybe<Alert> findLatestActiveByField(long fieldId) {
        return alertDao.findLatestActiveByField(fieldId).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    public Completable deleteAlert(int alertId) {
        return alertDao.deleteAlert(alertId).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    public Single<Long> insertAlert(Alert alert) {
        return alertDao.insert(alert)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public Single<List<Long>> insertAlerts(List<Alert> alerts) {
        return alertDao.insertAll(alerts)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public Completable setResolved(long id, boolean resolved) {
        long resolvedAt = resolved ? System.currentTimeMillis() : 0L;
        return alertDao.updateResolved(id, resolved, resolvedAt)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public Completable cleanupAlerts() {
        long now = System.currentTimeMillis();
        long resolvedBefore = now - TimeUnit.DAYS.toMillis(10);
        return alertDao.deleteExpiredActive(now)
                .andThen(alertDao.deleteResolvedBefore(resolvedBefore))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public Completable setAlertResolved(long id) {
        return setResolved(id, true);
    }

    public Completable setAlertActive(long id) {
        return setResolved(id, false);
    }

    public Maybe<Alert> findLatestActiveByTypeAndField(int typeId, long fieldId) {
        return alertDao.findLatestActiveByTypeAndField(typeId, fieldId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public Maybe<Alert> findLatestResolvedByTypeAndField(int typeId, long fieldId) {
        return alertDao.findLatestResolvedByTypeAndField(typeId, fieldId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    public class LocalBinder extends Binder {
        public DataManager getService() {
            // Return this instance of LocalService so clients can call public methods.
            return DataManager.this;
        }
    }



    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }
}
