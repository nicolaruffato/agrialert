package com.agrialert.data_manager;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {FieldsGroup.class, Field.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    public abstract FieldsDao fieldsDao();
}