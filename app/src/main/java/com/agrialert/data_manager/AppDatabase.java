package com.agrialert.data_manager;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.sqlite.db.SupportSQLiteDatabase;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

//TODO : parlare dell'exportSchema nella prossima chiamata di gruppo

/**
 * Main database configuration for the AgriAlert application.
 * This class defines the Room database persistent state and serves as the main access point
 * for the underlying SQLite database.
 *
 * <p>It includes tables for fields, field groups, alerts, user alerts, and alert types.
 * The database is initialized with default data for field groups and various
 * agricultural alert types upon first creation.</p>
 *
 * @see RoomDatabase
 */
@Database(entities = {FieldsGroup.class, Field.class, AlertType.class, AlertTypeCrossRef.class, Alert.class}, version = 1, exportSchema = false)
@TypeConverters({Converters.class})
public abstract class AppDatabase extends RoomDatabase {
    abstract FieldsDao fieldsDao();
    abstract AlertDao alertDao();

    private static volatile AppDatabase INSTANCE;


    /**
     * Gets the singleton instance of the AppDatabase.
     * <p>
     * If the instance does not exist, it is created using the Room database builder.
     * This method includes an {@link RoomDatabase.Callback} to prepopulate the database
     * with a default group and several predefined alert types upon its initial creation.
     * </p>
     *
     * @param context The application context used to create or open the database.
     * @return The singleton instance of {@code AppDatabase}.
     */
    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, "FieldsDB")
                            .addCallback(new RoomDatabase.Callback() {
                                @Override
                                public void onCreate(@NonNull SupportSQLiteDatabase db) {
                                    super.onCreate(db);
                                    db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS idx_alerts_active_unique ON alerts(fieldId, typeId) WHERE resolved = 0");
                                    db.execSQL("INSERT INTO FieldsGroup VALUES ('Default', 'Gruppo di Default')");
                                    db.execSQL("INSERT INTO AlertType VALUES (NULL, 'Ondata di calore', 'Temperature elevate che possono causare stress termico', 35, NULL)");
                                    db.execSQL("INSERT INTO AlertType VALUES (NULL, 'Gelo / Brina', 'Rischio di danni da gelo su colture sensibili', 0, NULL)");
                                    db.execSQL("INSERT INTO AlertType VALUES (NULL, 'Pioggia intensa', 'Precipitazioni elevate che possono provocare ristagno o erosione', 30, NULL)");
                                    db.execSQL("INSERT INTO AlertType VALUES (NULL, 'Vento forte', 'Raffiche che possono piegare o danneggiare le piante', 60, NULL)");
                                    db.execSQL("INSERT INTO AlertType VALUES (NULL, 'Temporale / Grandine', 'Eventi violenti con rischio di danni ai raccolti', 12, NULL)");
                                    db.execSQL("INSERT INTO AlertType VALUES (NULL, 'Siccità prolungata', 'Carenza idrica dovuta a mancanza di piogge', 7, 30)");
                                    db.execSQL("INSERT INTO AlertType VALUES (NULL, 'Umidità elevata', 'Rischio di malattie fungine dovute a eccesso di umidità', 85, NULL)");
                                    db.execSQL("INSERT INTO AlertType VALUES (NULL, 'Escursione termica elevata', 'Rischio di stress termico tra giorno e notte', 20, NULL)");
                                    db.execSQL("INSERT INTO AlertType VALUES (NULL, 'Rischio incendio', 'Condizioni di vento secco e terreno arido', 40, NULL)");
                                    db.execSQL("INSERT INTO AlertType VALUES (NULL, 'Scarsa ventilazione', 'Stagnazione dell''aria con rischio muffe', 5, 80)");
                                }
                            })
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}