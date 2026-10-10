package com.yourpackage.data.local;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.yourpackage.data.local.dao.*;
import com.yourpackage.data.local.entities.*;

@Database(entities = {
        StudentEntity.class,
        CourseEntity.class,
        LabGroupEntity.class,
        LabEntity.class,
        SubmissionEntity.class
}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    private static volatile AppDatabase INSTANCE;

    public abstract StudentDao studentDao();
    public abstract CourseDao courseDao();
    public abstract LabGroupDao labGroupDao();
    public abstract LabDao labDao();
    public abstract SubmissionDao submissionDao();

    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                            context.getApplicationContext(),
                            AppDatabase.class,
                            "student_lab_local_db"
                    ).fallbackToDestructiveMigration().build();
                }
            }
        }
        return INSTANCE;
    }
}