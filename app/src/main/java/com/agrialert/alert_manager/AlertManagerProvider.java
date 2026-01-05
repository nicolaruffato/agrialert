package com.agrialert.alert_manager;

import android.content.Context;

import com.agrialert.alert_manager.domain.AlertEvaluator;
import com.agrialert.alert_manager.repo.AlertRepository;
import com.agrialert.api.OpenMeteoApiClient;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * Provides lazily initialized singletons for alert manager components without using
 * a dependency injection framework.
 */
public final class AlertManagerProvider {

    private static volatile AlertRepository repository;

    /**
     * Prevents instantiation; this is a static provider class.
     */
    private AlertManagerProvider() {
    }

    /**
     * Returns the singleton {@link AlertRepository}, initializing it on first use.
     * This method also triggers initialization of background work.
     *
     * @param context any context used to derive the application context
     * @return the shared {@link AlertRepository} instance
     */
    public static AlertRepository getRepository(Context context) {
        if (repository == null) {
            synchronized (AlertManagerProvider.class) {
                if (repository == null) {
                    AlertManagerInitializer.init(context.getApplicationContext());
                    repository = new AlertRepository(
                            context.getApplicationContext(),
                            new OpenMeteoApiClient(),
                            new AlertEvaluator(),
                            Schedulers.io(),
                            AndroidSchedulers.mainThread()
                    );
                }
            }
        }
        return repository;
    }
}
