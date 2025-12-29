package com.agrialert.data_manager;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;

@Dao
interface FieldsDao {

    @Transaction
    @Query("SELECT * FROM FieldsGroup")
    Flowable<List<GroupWithFields>> getGroups();

    @Transaction
    @Query("SELECT * FROM FieldsGroup WHERE name = :name")
    Flowable<GroupWithFields> getGroupByName(String name);


    @Insert(onConflict = OnConflictStrategy.ABORT)
    Completable insertGroup(FieldsGroup group);

    @Query("DELETE FROM AlertTypeCrossRef WHERE fieldId = :fieldId")
    Completable deleteAlertsForField(int fieldId);
    @Insert
    Completable insertField(Field field);

    @Insert
    Completable insertAlertType(AlertType alertType);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Completable insertFieldAlertRelation(AlertTypeCrossRef crossRef);

    // Utile se devi inserire più relazioni contemporaneamente
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Completable insertFieldAlertRelations(List<AlertTypeCrossRef> crossRefs);

    @Update
    Completable updateFieldAlertRelation(AlertTypeCrossRef crossRef);

    @Update
    Completable updateFieldAlertRelations(List<AlertTypeCrossRef> crossRefs);

    @Delete
    Completable deleteGroup(FieldsGroup group);

    @Delete
    Completable deleteField(Field field);

    @Update
    Completable updateGroup(FieldsGroup group);

    @Update
    Completable updateField(Field field);


    @Transaction
    @Query("SELECT * FROM Field WHERE id = :fieldId")
    Flowable<ActivatedAlerts> getAlertsFromField(int fieldId);

    @Query("SELECT * FROM AlertType")
    Flowable<List<AlertType>> getAllAlertTypes();





}
