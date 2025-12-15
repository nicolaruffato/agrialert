package com.agrialert.AppDatabase;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;

import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;

@Dao
public interface FieldsDao {

    @Transaction
    @Query("SELECT * FROM FieldsGroup")
    Flowable<List<GroupWithFields>> getGroups();

    @Transaction
    @Query("SELECT * FROM FieldsGroup WHERE name = :name")
    Flowable<GroupWithFields> getGroup(String name);


    @Insert
    Completable insertGroup(FieldsGroup group);

    @Insert
    Completable insetField(Field field);

}
