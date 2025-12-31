package com.agrialert.data_manager;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;


import java.util.ArrayList;
import java.util.List;

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

    public DataManager() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        db = AppDatabase.getDatabase(this);
        fieldsDao = db.fieldsDao();
        alertDao = db.alertDao();
    }

    public Flowable<List<GroupWithFields>> getAllGroups() {
        return fieldsDao.getGroups().subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    public Flowable<GroupWithFields> getGroupByName(String name) {
        return fieldsDao.getGroupByName(name).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    public Completable insertGroup(FieldsGroup group) {
        return fieldsDao.insertGroup(group).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    public Completable insertField(Field field) {
        return fieldsDao.insertField(field).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    public Single<Field> getFieldById(int fieldId) {
        return fieldsDao.getFieldById(fieldId).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    public Completable addAlertToField(int fieldId, int alertTypeId, Threshold treshold) {
        return fieldsDao.insertFieldAlertRelation(new AlertTypeCrossRef(alertTypeId, fieldId, treshold.getThreshold1(), treshold.getThreshold2()))
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    // creare modifica alert associati al campo
    public Completable updateAlertToField(int fieldId, int alertTypeId, Threshold treshold) {
        return fieldsDao.updateFieldAlertRelation(new AlertTypeCrossRef(alertTypeId, fieldId, treshold.getThreshold1(), treshold.getThreshold2()))
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    public Completable updateAlertsToField(int fieldId, List<Pair<Integer, Threshold>> alertsTypeWithThresholds) {
        List<AlertTypeCrossRef> crossRefs = new ArrayList<>();
        for(var pair : alertsTypeWithThresholds) {
            crossRefs.add(new AlertTypeCrossRef(pair.getFirst(), fieldId, pair.getSecond().getThreshold1(), pair.getSecond().getThreshold2()));
        }

        return fieldsDao.deleteAlertsForField(fieldId)
                .andThen(fieldsDao.insertFieldAlertRelations(crossRefs))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public Completable addAlertType(AlertType alertType) {
        return fieldsDao.insertAlertType(alertType).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    public Flowable<ActivatedAlerts> getActivatedAlertsFromField(int fieldId) {
        return fieldsDao.getAlertsFromField(fieldId).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    public Completable deleteField(Field field) {
        return fieldsDao.deleteField(field).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    public Completable deleteGroup(FieldsGroup group) {
        return fieldsDao.deleteGroup(group).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    public Completable updateField(Field field) {
        return fieldsDao.updateField(field).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

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
        return alertDao.updateResolved(id, resolved)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public Completable setAlertResolved(long id) {
        return setResolved(id, true);
    }

    public Completable setAlertActive(long id) {
        return setResolved(id, false);
    }

    public Maybe<Alert> findLatestByTypeAndGroup(int typeId, String groupName) {
        return alertDao.findLatestByTypeAndGroup(typeId, groupName)
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