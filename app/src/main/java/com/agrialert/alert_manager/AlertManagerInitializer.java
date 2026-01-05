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
 * Coordinates registration of WorkManager jobs that synchronize weather data
 * and generate alerts for the alert manager subsystem.
 */
public final class AlertManagerInitializer {

    private static final String PERIODIC_WORK_NAME = "weather_sync_periodic";
    private static final String ONE_TIME_WORK_NAME = "weather_sync_now";
    private static final AtomicBoolean initialized = new AtomicBoolean(false);

    /**
     * Prevents instantiation; this is a static utility class.
     */
    private AlertManagerInitializer() {
    }

    /**
     * Initializes the alert manager background work for the current process.
     * This method is idempotent and will only schedule work once per process.
     *
     * @param context any context used to derive the application context
     */
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
     * Enqueues a one-off sync immediately, replacing any existing immediate sync work.
     *
     * @param context any context used to derive the application context
     */
    public static void triggerImmediateSync(Context context) {
        enqueueImmediate(context.getApplicationContext(), buildConstraints());
    }

    /**
     * Enqueues a unique one-time WeatherSyncWorker with the provided constraints.
     *
     * @param context     application context used to access WorkManager
     * @param constraints constraints that must be satisfied for execution
     */
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

    /**
     * Builds the default constraints for weather synchronization work.
     *
     * @return the constraints requiring a network connection
     */
    private static Constraints buildConstraints() {
        return new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();
    }
}
