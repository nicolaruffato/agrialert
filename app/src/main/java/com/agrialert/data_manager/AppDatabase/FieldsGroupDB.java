package com.agrialert.data_manager.AppDatabase;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.Relation;

import java.util.List;

@Entity
public class FieldsGroupDB {

    @PrimaryKey(autoGenerate = true)
    public int id;

    public String name;
    public String description;

}
