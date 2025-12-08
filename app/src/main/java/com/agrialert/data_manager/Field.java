package com.agrialert.data_manager;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

@Entity(foreignKeys = {@ForeignKey(entity = FieldsGroup.class,
        parentColumns = "id",
        childColumns = "groupId",
        onDelete =ForeignKey.SET_DEFAULT)})
public class Field {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public String address;
    public Double latitude;
    public Double longitude;

    @ColumnInfo(defaultValue = "0")
    public int groupId;
}
