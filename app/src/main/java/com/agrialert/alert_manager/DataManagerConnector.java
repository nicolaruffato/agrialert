package com.agrialert.alert_manager;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

import com.agrialert.data_manager.DataManager;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;

/**
 * Utility per bind/unbind a DataManager e ottenere i suoi metodi in Rx.
 */
public final class DataManagerConnector {

    private static final String TAG = "DataManagerConnector";
    private static final long DEFAULT_BIND_TIMEOUT_MS = 5_000L;

    private DataManagerConnector() {}

    public static Single<BoundDataManager> bind(Context context) {
        return bind(context, DEFAULT_BIND_TIMEOUT_MS);
    }

    public static Single<BoundDataManager> bind(Context context, long timeoutMs) {
        return bindInternal(context, timeoutMs);
    }

    public static <T> Single<T> withSingle(Context context, Function<DataManager, Single<T>> fn) {
        return withSingle(context, DEFAULT_BIND_TIMEOUT_MS, fn);
    }

    public static <T> Single<T> withSingle(Context context,
                                           long timeoutMs,
                                           Function<DataManager, Single<T>> fn) {
        return bindInternal(context, timeoutMs).flatMap(bound ->
                Single.defer(() -> fn.apply(bound.service))
                        .doFinally(bound::release)
        );
    }

    public static Completable withCompletable(Context context, Function<DataManager, Completable> fn) {
        return withCompletable(context, DEFAULT_BIND_TIMEOUT_MS, fn);
    }

    public static Completable withCompletable(Context context,
                                              long timeoutMs,
                                              Function<DataManager, Completable> fn) {
        return bindInternal(context, timeoutMs).flatMapCompletable(bound ->
                Completable.defer(() -> fn.apply(bound.service))
                        .doFinally(bound::release)
        );
    }

    public static <T> Flowable<T> withFlowable(Context context, Function<DataManager, Flowable<T>> fn) {
        return withFlowable(context, DEFAULT_BIND_TIMEOUT_MS, fn);
    }

    public static <T> Flowable<T> withFlowable(Context context,
                                               long timeoutMs,
                                               Function<DataManager, Flowable<T>> fn) {
        return bindInternal(context, timeoutMs).flatMapPublisher(bound ->
                Flowable.defer(() -> fn.apply(bound.service))
                        .doFinally(bound::release)
        );
    }

    public static <T> Maybe<T> withMaybe(Context context, Function<DataManager, Maybe<T>> fn) {
        return withMaybe(context, DEFAULT_BIND_TIMEOUT_MS, fn);
    }

    public static <T> Maybe<T> withMaybe(Context context,
                                         long timeoutMs,
                                         Function<DataManager, Maybe<T>> fn) {
        return bindInternal(context, timeoutMs).flatMapMaybe(bound ->
                Maybe.defer(() -> fn.apply(bound.service))
                        .doFinally(bound::release)
        );
    }

    private static Single<BoundDataManager> bindInternal(Context context, long timeoutMs) {
        if (context == null) {
            return Single.error(new IllegalArgumentException("Context necessario per bind DataManager"));
        }
        final Context appContext = context.getApplicationContext();
        final long safeTimeout = Math.max(timeoutMs, DEFAULT_BIND_TIMEOUT_MS);

        return Single.<BoundDataManager>create(emitter -> {
            Intent intent = new Intent(appContext, DataManager.class);
            AtomicBoolean bound = new AtomicBoolean(false);

            ServiceConnection connection = new ServiceConnection() {
                @Override
                public void onServiceConnected(ComponentName name, IBinder service) {
                    if (emitter.isDisposed()) {
                        tryUnbind(appContext, this, bound);
                        return;
                    }
                    bound.set(true);
                    DataManager.LocalBinder binder = (DataManager.LocalBinder) service;
                    DataManager manager = binder.getService();
                    emitter.onSuccess(new BoundDataManager(appContext, this, manager, bound));
                }

                @Override
                public void onServiceDisconnected(ComponentName name) {
                    if (!emitter.isDisposed()) {
                        Log.w(TAG, "DataManager disconnected");
                        emitter.tryOnError(new IllegalStateException("DataManager disconnected"));
                    }
                    tryUnbind(appContext, this, bound);
                }
            };

            boolean ok = appContext.bindService(intent, connection, Context.BIND_AUTO_CREATE);
            if (!ok) {
                Log.w(TAG, "Impossibile bindare DataManager");
                emitter.onError(new IllegalStateException("Impossibile bindare DataManager"));
                return;
            }

            emitter.setCancellable(() -> tryUnbind(appContext, connection, bound));
        }).timeout(safeTimeout, TimeUnit.MILLISECONDS);
    }

    private static void tryUnbind(Context context, ServiceConnection connection, AtomicBoolean bound) {
        if (bound.compareAndSet(true, false)) {
            try {
                context.unbindService(connection);
            } catch (Exception ignored) {
            }
        }
    }

    public static final class BoundDataManager {
        private final Context appContext;
        private final ServiceConnection connection;
        private final DataManager service;
        private final AtomicBoolean bound;

        BoundDataManager(Context appContext,
                         ServiceConnection connection,
                         DataManager service,
                         AtomicBoolean bound) {
            this.appContext = appContext;
            this.connection = connection;
            this.service = service;
            this.bound = bound;
        }

        public DataManager getService() {
            return service;
        }

        void release() {
            tryUnbind(appContext, connection, bound);
        }
    }
}
