package com.agrialert.data_manager.AppDatabase;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(indices = {@Index(value = "groupId", unique = true)},
        foreignKeys = {@ForeignKey(entity = FieldsGroupDB.class,
        parentColumns = "id",
        childColumns = "groupId",
        onDelete =ForeignKey.SET_DEFAULT)})
public class FieldDB {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public String address;
    public Double latitude;
    public Double longitude;

    @ColumnInfo(defaultValue = "0")
    public int groupId;
}
