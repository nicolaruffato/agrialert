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
import io.reactivex.rxjava3.core.Single;

/**
 * Data Access Object (DAO) for managing database operations related to fields, groups, and alerts.
 * This interface provides methods for CRUD operations on field groups, individual fields,
 * alert types, and the relationships between fields and alerts using Room and RxJava.
 * This interface should not be exposed outside the data_manager package.
 * All CRUD operation should be first performed using a {@link DataManager} instance.
 */
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

    @Insert
    Completable insetField(Field field);

    @Query("SELECT * FROM Field WHERE id = :fieldId")
    Single<Field> getFieldById(int fieldId);

    @Insert
    Completable insertAlertType(AlertType alertType);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Completable insertFieldAlertRelation(AlertTypeCrossRef crossRef);

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



}
