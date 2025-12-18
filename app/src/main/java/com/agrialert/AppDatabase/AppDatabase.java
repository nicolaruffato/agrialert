package com.agrialert.AppDatabase;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {FieldsGroup.class, Field.class, AlertType.class, AlertTypeCrossRef.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    public abstract FieldsDao fieldsDao();

    private static volatile AppDatabase INSTANCE;

    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, "FieldsDB")
                            .allowMainThreadQueries() // DA USARE SOLO PER TEST
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}