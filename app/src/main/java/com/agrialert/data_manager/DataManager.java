package com.agrialert.data_manager;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;

import com.agrialert.AppDatabase.AppDatabase;
import com.agrialert.AppDatabase.FieldsDao;

import java.util.List;
import java.util.stream.Collectors;

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

    public LiveData<List<FieldsGroup>> getAllGroups() {
        return Transformations.map(fieldsDao.loadAllGroupsWithFields(), dbList -> {
            return dbList.stream()
                    .map(groupwfields -> {
                        FieldsGroup group = new FieldsGroup(groupwfields.group.id, groupwfields.group.name);

                        List<Field> domainFields = groupwfields.fields.stream()
                                .map(dbfield -> new Field(dbfield.id, dbfield.address, dbfield.latitude, dbfield.longitude)) // Tuo costruttore di conversione
                                .collect(Collectors.toList());

                        group.addFields(domainFields);
                        return group;
                    })
                    .collect(Collectors.toList());
        });
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