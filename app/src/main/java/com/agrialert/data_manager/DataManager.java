package com.agrialert.data_manager;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import com.agrialert.AppDatabase.AppDatabase;
import com.agrialert.AppDatabase.Field;
import com.agrialert.AppDatabase.FieldsDao;
import com.agrialert.AppDatabase.GroupWithFields;
import com.agrialert.AppDatabase.FieldsGroup;


import org.reactivestreams.Publisher;

import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class DataManager extends Service {

    // Thread pool used for implementing the asynchronous operations on data
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

    public Completable insertGroup(FieldsGroup group) {
        return fieldsDao.insertGroup(group).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    public Completable insertField(Field field) {
        return fieldsDao.insetField(field).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
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