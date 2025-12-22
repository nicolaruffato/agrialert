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

    @Query("UPDATE alerts SET resolved = :resolved WHERE id = :id")
    Completable updateResolved(long id, boolean resolved);

    @Query("SELECT * FROM alerts WHERE typeId = :typeId AND groupName = :groupName ORDER BY createdAt DESC LIMIT 1")
    Maybe<Alert> findLatestByTypeAndGroup(int typeId, String groupName);
}
