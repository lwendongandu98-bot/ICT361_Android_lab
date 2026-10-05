package com.example.studentregistrationapp.data.local;

import android.content.Context;

import androidx.room.Room;

public class StudentLocalDataSource {

    private final AppDatabase database;

    public StudentLocalDataSource(Context context) {
        database = Room.databaseBuilder(
                        context.getApplicationContext(),
                        AppDatabase.class,
                        "student_database"
                )
                .addMigrations(AppDatabase.MIGRATION_1_2)
                .build();
    }

    public StudentDao getStudentDao() {
        return database.studentDao();
    }

    public PendingOperationDao getPendingOperationDao() {
        return database.pendingOperationDao();
    }
}