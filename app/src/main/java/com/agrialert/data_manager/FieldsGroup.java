package com.agrialert.data_manager;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class FieldsGroup {

    @PrimaryKey(autoGenerate = true)
    public int id;

    public String name;
    public String description;


}
