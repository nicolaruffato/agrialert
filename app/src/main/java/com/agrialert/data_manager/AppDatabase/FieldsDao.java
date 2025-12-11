package com.agrialert.data_manager.AppDatabase;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;

import java.util.List;

@Dao
public interface FieldsDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    public void insertField(FieldDB fieldDB);

    @Insert(onConflict = OnConflictStrategy.ABORT)
    public void inserFieldsGroup(FieldsGroupDB fieldsGroupDB);

    @Query("SELECT * FROM FieldDB WHERE groupId = :groupId")
    public LiveData<List<FieldDB>> loadAllFieldsFromGroup(int groupId);

    @Query("SELECT * FROM FieldsGroupDB")
    public LiveData<List<FieldsGroupDB>> loadAllFieldsGroups();
    @Transaction
    @Query("SELECT * FROM FieldsGroupDB")
    public LiveData<List<GroupWithFieldsDB>> loadAllGroupsWithFields();

    @Transaction
    @Query("SELECT * FROM FieldsGroupDB WHERE name = :name")
    public LiveData<GroupWithFieldsDB> loadGroupWithFields(String name);
}
