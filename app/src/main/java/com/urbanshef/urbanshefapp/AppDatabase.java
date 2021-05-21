package com.urbanshef.urbanshefapp;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.urbanshef.urbanshefapp.objects.Basket;

@Database(entities = {Basket.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    private static  AppDatabase db;

    public abstract BasketDAO basketDao();

    public static AppDatabase getAppDatabase(Context context) {
        if (db == null) {
            db = Room.databaseBuilder(context.getApplicationContext(),
                    AppDatabase.class, "database-name").allowMainThreadQueries().build();
        }
        return db;
    }
}
