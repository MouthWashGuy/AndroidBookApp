package com.example.lab5.provider;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(entities = {Book.class}, version = 1)
public abstract class BookDatabase extends RoomDatabase {

    public static final String BOOK_DATABASE_NAME = "book_database";

    public abstract BookDao bookDao();

    // marking the instance as volatile to ensure atomic access to the variable
    private static volatile BookDatabase INSTANCE;
    private static final int NUMBER_OF_THREADS = 4;
    static final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    static BookDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (BookDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    BookDatabase.class, BOOK_DATABASE_NAME)
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
