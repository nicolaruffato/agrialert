package com.agrialert.data_manager;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import com.agrialert.AppDatabase.ActivatedAlerts;
import com.agrialert.AppDatabase.AlertType;
import com.agrialert.AppDatabase.AlertTypeCrossRef;
import com.agrialert.AppDatabase.AppDatabase;
import com.agrialert.AppDatabase.Field;
import com.agrialert.AppDatabase.FieldsDao;
import com.agrialert.AppDatabase.GroupWithFields;
import com.agrialert.AppDatabase.FieldsGroup;


import java.util.ArrayList;
import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import kotlin.Pair;

public class DataManager extends Service {
/*
    COSE DA FARE:
    - Creare un unico package dove ci sono tutte le classi di AppDatabase e data_manager insieme
    - questo perche' risco a mettere pubbliche solo ed esclusivamente le classi che devono restare pubbliche
    - In questo momento AlertTypeCrossRef e' pubblico con tutti campi protected, sarebbe meglio spostarlo a
    package private ma per farlo DataManager deve risiedere nella stessa cartella

    - Per il resto tutto e' stato implementato
*/

    private final IBinder binder = new LocalBinder();
    private AppDatabase db;

    private FieldsDao fieldsDao;

    public DataManager() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        db = AppDatabase.getDatabase(this);
        fieldsDao = db.fieldsDao();
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
        return fieldsDao.insetField(field).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    public Completable addAlertToField(int fieldId, int alertTypeId, Double treshold) {
        return fieldsDao.insertFieldAlertRelation(new AlertTypeCrossRef(alertTypeId, fieldId, treshold))
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    // creare modifica alert associati al campo
    public Completable updateAlertToField(int fieldId, int alertTypeId, Double treshold) {
        return fieldsDao.updateFieldAlertRelation(new AlertTypeCrossRef(alertTypeId, fieldId, treshold))
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    public Completable updateAlertsToField(int fieldId, List<Pair<Integer, Double>> alertsTypeWithThresholds) {
        List<AlertTypeCrossRef> crossRefs = new ArrayList<>();
        for(var pair : alertsTypeWithThresholds) {
            crossRefs.add(new AlertTypeCrossRef(pair.getFirst(), fieldId, pair.getSecond()));
        }
        return fieldsDao.updateFieldAlertRelations(crossRefs)
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
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