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
 * Utility for binding to {@link DataManager} and exposing its operations through RxJava types.
 * <p>
 * Binding is always performed against the application context. The {@code with*} helpers take care
 * of unbinding when the returned reactive type terminates; requested timeouts shorter than
 * {@link #DEFAULT_BIND_TIMEOUT_MS} are clamped to that minimum.
 * </p>
 */
public final class DataManagerConnector {

    private static final String TAG = "DataManagerConnector";
    private static final long DEFAULT_BIND_TIMEOUT_MS = 5_000L;

    /**
     * Prevents instantiation; this is a static utility class.
     */
    private DataManagerConnector() {}

    /**
     * Binds to {@link DataManager} using the default timeout.
     *
     * @param context any context used to derive the application context
     * @return a {@link Single} that emits a bound service wrapper
     */
    public static Single<BoundDataManager> bind(Context context) {
        return bind(context, DEFAULT_BIND_TIMEOUT_MS);
    }

    /**
     * Binds to {@link DataManager} using a custom timeout.
     *
     * @param context   any context used to derive the application context
     * @param timeoutMs timeout in milliseconds before emitting a timeout error; values lower than
     *                  {@link #DEFAULT_BIND_TIMEOUT_MS} are treated as {@link #DEFAULT_BIND_TIMEOUT_MS}
     * @return a {@link Single} that emits a bound service wrapper
     */
    public static Single<BoundDataManager> bind(Context context, long timeoutMs) {
        return bindInternal(context, timeoutMs);
    }

    /**
     * Binds to {@link DataManager}, executes a {@link Single} operation, and unbinds when done.
     *
     * @param context any context used to derive the application context
     * @param fn      function that uses the bound service to produce a {@link Single}
     * @param <T>     the item type emitted by the {@link Single}
     * @return a {@link Single} that mirrors {@code fn} and releases the service when finished
     */
    public static <T> Single<T> withSingle(Context context, Function<DataManager, Single<T>> fn) {
        return withSingle(context, DEFAULT_BIND_TIMEOUT_MS, fn);
    }

    /**
     * Binds to {@link DataManager}, executes a {@link Single} operation, and unbinds when done.
     *
     * @param context   any context used to derive the application context
     * @param timeoutMs timeout in milliseconds before emitting a timeout error
     * @param fn        function that uses the bound service to produce a {@link Single}
     * @param <T>       the item type emitted by the {@link Single}
     * @return a {@link Single} that mirrors {@code fn} and releases the service when finished
     */
    public static <T> Single<T> withSingle(Context context,
                                           long timeoutMs,
                                           Function<DataManager, Single<T>> fn) {
        return bindInternal(context, timeoutMs).flatMap(bound ->
                Single.defer(() -> fn.apply(bound.service))
                        .doFinally(bound::release)
        );
    }

    /**
     * Binds to {@link DataManager}, executes a {@link Completable}, and unbinds when done.
     *
     * @param context any context used to derive the application context
     * @param fn      function that uses the bound service to produce a {@link Completable}
     * @return a {@link Completable} that mirrors {@code fn} and releases the service when finished
     */
    public static Completable withCompletable(Context context, Function<DataManager, Completable> fn) {
        return withCompletable(context, DEFAULT_BIND_TIMEOUT_MS, fn);
    }

    /**
     * Binds to {@link DataManager}, executes a {@link Completable}, and unbinds when done.
     *
     * @param context   any context used to derive the application context
     * @param timeoutMs timeout in milliseconds before emitting a timeout error
     * @param fn        function that uses the bound service to produce a {@link Completable}
     * @return a {@link Completable} that mirrors {@code fn} and releases the service when finished
     */
    public static Completable withCompletable(Context context,
                                              long timeoutMs,
                                              Function<DataManager, Completable> fn) {
        return bindInternal(context, timeoutMs).flatMapCompletable(bound ->
                Completable.defer(() -> fn.apply(bound.service))
                        .doFinally(bound::release)
        );
    }

    /**
     * Binds to {@link DataManager}, executes a {@link Flowable}, and unbinds when done.
     *
     * @param context any context used to derive the application context
     * @param fn      function that uses the bound service to produce a {@link Flowable}
     * @param <T>     the item type emitted by the {@link Flowable}
     * @return a {@link Flowable} that mirrors {@code fn} and releases the service when finished
     */
    public static <T> Flowable<T> withFlowable(Context context, Function<DataManager, Flowable<T>> fn) {
        return withFlowable(context, DEFAULT_BIND_TIMEOUT_MS, fn);
    }

    /**
     * Binds to {@link DataManager}, executes a {@link Flowable}, and unbinds when done.
     *
     * @param context   any context used to derive the application context
     * @param timeoutMs timeout in milliseconds before emitting a timeout error
     * @param fn        function that uses the bound service to produce a {@link Flowable}
     * @param <T>       the item type emitted by the {@link Flowable}
     * @return a {@link Flowable} that mirrors {@code fn} and releases the service when finished
     */
    public static <T> Flowable<T> withFlowable(Context context,
                                               long timeoutMs,
                                               Function<DataManager, Flowable<T>> fn) {
        return bindInternal(context, timeoutMs).flatMapPublisher(bound ->
                Flowable.defer(() -> fn.apply(bound.service))
                        .doFinally(bound::release)
        );
    }

    /**
     * Binds to {@link DataManager}, executes a {@link Maybe}, and unbinds when done.
     *
     * @param context any context used to derive the application context
     * @param fn      function that uses the bound service to produce a {@link Maybe}
     * @param <T>     the item type emitted by the {@link Maybe}
     * @return a {@link Maybe} that mirrors {@code fn} and releases the service when finished
     */
    public static <T> Maybe<T> withMaybe(Context context, Function<DataManager, Maybe<T>> fn) {
        return withMaybe(context, DEFAULT_BIND_TIMEOUT_MS, fn);
    }

    /**
     * Binds to {@link DataManager}, executes a {@link Maybe}, and unbinds when done.
     *
     * @param context   any context used to derive the application context
     * @param timeoutMs timeout in milliseconds before emitting a timeout error
     * @param fn        function that uses the bound service to produce a {@link Maybe}
     * @param <T>       the item type emitted by the {@link Maybe}
     * @return a {@link Maybe} that mirrors {@code fn} and releases the service when finished
     */
    public static <T> Maybe<T> withMaybe(Context context,
                                         long timeoutMs,
                                         Function<DataManager, Maybe<T>> fn) {
        return bindInternal(context, timeoutMs).flatMapMaybe(bound ->
                Maybe.defer(() -> fn.apply(bound.service))
                        .doFinally(bound::release)
        );
    }

    /**
     * Performs the actual bind operation and wraps the bound service in a {@link BoundDataManager}.
     *
     * @param context   any context used to derive the application context
     * @param timeoutMs requested timeout in milliseconds before emitting a timeout error; values
     *                  lower than {@link #DEFAULT_BIND_TIMEOUT_MS} are clamped to that minimum
     * @return a {@link Single} that emits the bound service wrapper
     */
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

    /**
     * Attempts to unbind the service connection if it is currently bound.
     *
     * @param context    application context used to unbind
     * @param connection the service connection to unbind
     * @param bound      flag tracking the current bound state
     */
    private static void tryUnbind(Context context, ServiceConnection connection, AtomicBoolean bound) {
        if (bound.compareAndSet(true, false)) {
            try {
                context.unbindService(connection);
            } catch (Exception ignored) {
            }
        }
    }

    /**
     * Holds a bound {@link DataManager} instance and the connection used to release it.
     */
    public static final class BoundDataManager {
        private final Context appContext;
        private final ServiceConnection connection;
        private final DataManager service;
        private final AtomicBoolean bound;

        /**
         * Creates a bound wrapper for the {@link DataManager} service.
         *
         * @param appContext application context used for unbinding
         * @param connection service connection used for unbinding
         * @param service    bound {@link DataManager} instance
         * @param bound      flag tracking the current bound state
         */
        BoundDataManager(Context appContext,
                         ServiceConnection connection,
                         DataManager service,
                         AtomicBoolean bound) {
            this.appContext = appContext;
            this.connection = connection;
            this.service = service;
            this.bound = bound;
        }

        /**
         * Returns the bound {@link DataManager} instance.
         *
         * @return the bound service
         */
        public DataManager getService() {
            return service;
        }

        /**
         * Releases the service connection if it is still bound.
         */
        void release() {
            tryUnbind(appContext, connection, bound);
        }
    }
}
