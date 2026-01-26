package com.agrialert.data_manager;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;

/**
 * Room DAO for {@link Alert}.
 * <p>
 * Exposes RxJava3 queries to observe, insert, update, and delete alerts stored in the
 * {@code alerts} table.
 * </p>
 */
@Dao
public interface AlertDao {

    /**
     * Observes alerts filtered by resolved state, ordered by most recently created.
     *
     * @param resolved {@code true} for resolved alerts, {@code false} for active alerts
     * @return reactive stream emitting the updated list when the table changes
     */
    @Query("SELECT * FROM Alerts WHERE resolved = :resolved ORDER BY createdAt DESC")
    Flowable<List<Alert>> observeByResolved(boolean resolved);

    /**
     * Inserts an alert, skipping it on conflict.
     *
     * @param entity alert to insert
     * @return inserted row id
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    Single<Long> insert(Alert entity);

    /**
     * Inserts a list of alerts, skipping them on conflict.
     *
     * @param entities alerts to insert
     * @return list of inserted ids
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    Single<List<Long>> insertAll(List<Alert> entities);

    /**
     * Updates an alert resolved state and its corresponding timestamp.
     *
     * @param id         alert id
     * @param resolved   new resolved state
     * @param resolvedAt resolution timestamp (ms), or {@code 0} when not resolved
     */
    @Query("UPDATE alerts SET resolved = :resolved, resolvedAt = :resolvedAt WHERE id = :id")
    Completable updateResolved(long id, boolean resolved, long resolvedAt);

    /**
     * Deletes active alerts whose {@link Alert#getForecastAt()} is earlier than {@code now}.
     *
     * @param now reference timestamp (ms)
     */
    @Query("DELETE FROM alerts WHERE resolved = 0 AND forecastAt < :now")
    Completable deleteExpiredActive(long now);

    /**
     * Deletes resolved alerts whose {@link Alert#getResolvedAt()} is earlier than {@code resolvedBefore}.
     *
     * @param resolvedBefore retention threshold timestamp (ms)
     */
    @Query("DELETE FROM alerts WHERE resolved = 1 AND resolvedAt < :resolvedBefore")
    Completable deleteResolvedBefore(long resolvedBefore);

    /**
     * Deletes all alerts for a field.
     *
     * @param fieldId field id
     */
    @Query("DELETE FROM alerts WHERE fieldId = :fieldId")
    Completable deleteByFieldId(long fieldId);

    /**
     * Deletes active alerts for a field limited to a list of types.
     *
     * @param fieldId field id
     * @param typeIds type ids to delete
     */
    @Query("DELETE FROM alerts WHERE resolved = 0 AND fieldId = :fieldId AND typeId IN (:typeIds)")
    Completable deleteActiveByFieldAndTypes(long fieldId, List<Integer> typeIds);

    /**
     * Returns the most recent alert (active or resolved) for a type/field pair, ordered by creation time.
     */
    @Query("SELECT * FROM alerts WHERE typeId = :typeId AND fieldId = :fieldId ORDER BY createdAt DESC LIMIT 1")
    Maybe<Alert> findLatestByTypeAndField(int typeId, long fieldId);

    /**
     * Returns the most recent active alert for a type/field pair, ordered by creation time.
     */
    @Query("SELECT * FROM alerts WHERE typeId = :typeId AND fieldId = :fieldId AND resolved = 0 ORDER BY createdAt DESC LIMIT 1")
    Maybe<Alert> findLatestActiveByTypeAndField(int typeId, long fieldId);

    /**
     * Returns the most recent active alert for a field, ordered by creation time.
     */
    @Query("SELECT * FROM alerts WHERE fieldId = :fieldId AND resolved = 0 ORDER BY createdAt DESC LIMIT 1")
    Maybe<Alert> findLatestActiveByField(long fieldId);

    /**
     * Returns the most recent resolved alert for a type/field pair, ordered by resolution time.
     */
    @Query("SELECT * FROM alerts WHERE typeId = :typeId AND fieldId = :fieldId AND resolved = 1 ORDER BY resolvedAt DESC LIMIT 1")
    Maybe<Alert> findLatestResolvedByTypeAndField(int typeId, long fieldId);

    /**
     * Observes active alerts for a field, ordered by most recently created.
     */
    @Query("SELECT * FROM alerts WHERE fieldId = :fieldId AND resolved = 0 ORDER BY createdAt DESC")
    Flowable<List<Alert>> getActiveAlertsFromField(int fieldId);

    /**
     * Observes all alerts (active and resolved) for a field.
     */
    @Query("SELECT * FROM alerts WHERE fieldId = :fieldId")
    Flowable<List<Alert>> getAlertsFromField(int fieldId);

    /**
     * Deletes an alert by id.
     *
     * @param alertId alert id
     */
    @Query("DELETE FROM alerts WHERE id = :alertId")
    Completable deleteAlert(int alertId);
}
