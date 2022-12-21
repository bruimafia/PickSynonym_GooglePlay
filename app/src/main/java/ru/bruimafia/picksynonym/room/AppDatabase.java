package ru.bruimafia.picksynonym.room;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {Level.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    public abstract LevelDao levelDao();

    public static AppDatabase instance;

    public static synchronized AppDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context, AppDatabase.class, "PickSynonymDB-database")
                    .allowMainThreadQueries()
                    .createFromAsset("PickSynonymDB.db")
                    .build();
        }
        return instance;
    }


}
