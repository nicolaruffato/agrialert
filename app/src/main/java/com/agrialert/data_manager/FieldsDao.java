package com.agrialert.data_manager;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

@Dao
public interface FieldsDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    public void insertField(Field field);

    @Insert(onConflict = OnConflictStrategy.ABORT)
    public void inserFieldsGroup(FieldsGroup fieldsGroup);

    @Query("SELECT * FROM Field WHERE groupId = :groupId")
    public Field[] loadAllFieldsFromGroup(int groupId);

    @Query("SELECT * FROM FieldsGroup")
    public FieldsGroup[] loadAllFieldsGroups();
}
