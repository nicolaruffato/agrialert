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

@Dao
public interface AlertDao {

    @Query("SELECT * FROM alerts WHERE resolved = :resolved ORDER BY createdAt DESC")
    Flowable<List<Alert>> observeByResolved(boolean resolved);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Single<Long> insert(Alert entity);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Single<List<Long>> insertAll(List<Alert> entities);

    @Query("UPDATE alerts SET resolved = :resolved, resolvedAt = :resolvedAt WHERE id = :id")
    Completable updateResolved(long id, boolean resolved, long resolvedAt);

    @Query("DELETE FROM alerts WHERE resolved = 0 AND (forecastAt + durationMs) < :now")
    Completable deleteExpiredActive(long now);

    @Query("DELETE FROM alerts WHERE resolved = 1 AND resolvedAt <= :resolvedBefore")
    Completable deleteResolvedBefore(long resolvedBefore);

    @Query("SELECT * FROM alerts WHERE typeId = :typeId AND fieldId = :fieldId ORDER BY createdAt DESC LIMIT 1")
    Maybe<Alert> findLatestByTypeAndField(int typeId, long fieldId);

    @Query("SELECT * FROM alerts WHERE typeId = :typeId AND fieldId = :fieldId AND resolved = 0 ORDER BY createdAt DESC LIMIT 1")
    Maybe<Alert> findLatestActiveByTypeAndField(int typeId, long fieldId);

    @Query("SELECT * FROM alerts WHERE typeId = :typeId AND fieldId = :fieldId AND resolved = 1 ORDER BY resolvedAt DESC LIMIT 1")
    Maybe<Alert> findLatestResolvedByTypeAndField(int typeId, long fieldId);
}
