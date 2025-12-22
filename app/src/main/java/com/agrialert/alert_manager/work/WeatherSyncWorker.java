package com.agrialert.alert_manager.work;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.agrialert.alert_manager.AlertManagerProvider;
import com.agrialert.alert_manager.notifications.AlertNotificationManager;
import com.agrialert.alert_manager.repo.AlertRepository;
import com.agrialert.data_manager.Alert;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Worker che ogni 15 minuti sincronizza il meteo e genera alert.
 */
public class WeatherSyncWorker extends Worker {

    private static final String TAG = "WeatherSyncWorker";

    public WeatherSyncWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        try {
            AlertRepository repository = AlertManagerProvider.getRepository(getApplicationContext());
            AlertRepository.WeatherSyncResult result = repository.syncAllGroups()
                    .timeout(45, TimeUnit.SECONDS)
                    .blockingGet();
            List<Alert> newAlerts = result != null ? result.created : null;

            if (newAlerts != null && !newAlerts.isEmpty()) {
                AlertNotificationManager.notifyNewAlerts(getApplicationContext(), newAlerts);
            }

            return Result.success();
        } catch (Exception ex) {
            Log.e(TAG, "Errore durante l'esecuzione del worker meteo", ex);
            return Result.retry();
        }
    }
}
