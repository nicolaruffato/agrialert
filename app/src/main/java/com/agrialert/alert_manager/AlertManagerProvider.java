package com.agrialert.alert_manager;

import android.content.Context;

import com.agrialert.alert_manager.domain.AlertEvaluator;
import com.agrialert.alert_manager.repo.AlertRepository;
import com.agrialert.api.OpenMeteoApiClient;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * Fornisce singleton per repository/risorse senza dipendere da DI framework.
 */
public final class AlertManagerProvider {

    private static volatile AlertRepository repository;

    private AlertManagerProvider() {
    }

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
