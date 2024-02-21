package com.example.youdo;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {ToDo.class, Type.class, User.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    public abstract ToDoDao toDoDao();
    public abstract TypeDao typeDao();
    public abstract UserDao userDao();

    // Singleton sample for database instance
    private static volatile AppDatabase INSTANCE;

    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, "app_database")
                            .fallbackToDestructiveMigration() // delete and rebuilds the database if the pattern changes
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
