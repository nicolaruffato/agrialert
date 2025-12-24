package com.agrialert.data_manager;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

@Database(entities = {FieldsGroup.class, Field.class, AlertType.class, AlertTypeCrossRef.class, Alert.class}, version = 1)
@TypeConverters({Converters.class})
public abstract class AppDatabase extends RoomDatabase {
    abstract FieldsDao fieldsDao();
    abstract AlertDao alertDao();

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