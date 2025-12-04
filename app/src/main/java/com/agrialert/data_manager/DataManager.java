package com.agrialert.data_manager;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.widget.Toast;

public class DataManager extends Service {

    private final IBinder binder = new LocalBinder();

    public DataManager() {
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