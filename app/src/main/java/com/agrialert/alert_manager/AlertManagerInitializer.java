package com.agrialert.alert_manager;

import android.content.Context;

import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.ExistingWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.agrialert.alert_manager.work.WeatherSyncWorker;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Centralizza la registrazione dei worker periodici per sincronizzare meteo/alert.
 */
public final class AlertManagerInitializer {

    private static final String PERIODIC_WORK_NAME = "weather_sync_periodic";
    private static final String ONE_TIME_WORK_NAME = "weather_sync_now";
    private static final AtomicBoolean initialized = new AtomicBoolean(false);

    private AlertManagerInitializer() {
    }

    public static void init(Context context) {
        if (!initialized.compareAndSet(false, true)) {
            return;
        }

        Context appContext = context.getApplicationContext();
        Constraints constraints = buildConstraints();

        PeriodicWorkRequest periodicRequest = new PeriodicWorkRequest.Builder(
                WeatherSyncWorker.class,
                15,
                TimeUnit.MINUTES
        ).setConstraints(constraints).build();

        WorkManager.getInstance(appContext).enqueueUniquePeriodicWork(
                PERIODIC_WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                periodicRequest
        );

        enqueueImmediate(appContext, constraints);
    }

    /**
     * Permette di forzare una sync immediata (es. dopo l'inserimento di un nuovo campo).
     */
    public static void triggerImmediateSync(Context context) {
        enqueueImmediate(context.getApplicationContext(), buildConstraints());
    }

    private static void enqueueImmediate(Context context, Constraints constraints) {
        OneTimeWorkRequest immediateSync = new OneTimeWorkRequest.Builder(WeatherSyncWorker.class)
                .setConstraints(constraints)
                .build();

        WorkManager.getInstance(context.getApplicationContext()).enqueueUniqueWork(
                ONE_TIME_WORK_NAME,
                ExistingWorkPolicy.REPLACE,
                immediateSync
        );
    }

    private static Constraints buildConstraints() {
        return new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();
    }
}
