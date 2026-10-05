package com.example.studentregistrationapp.data.local;

import androidx.room.Database;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

@Database(
        entities = {
                Student.class,
                PendingOperation.class
        },
        version = 2,
        exportSchema = false
)
public abstract class AppDatabase extends RoomDatabase {

    public abstract StudentDao studentDao();

    public abstract PendingOperationDao pendingOperationDao();

    public void saveStudentAndOperation(
            Student student,
            PendingOperation operation) {

        runInTransaction(() -> {
            studentDao().insert(student);
            pendingOperationDao().insert(operation);
        });
    }

    public static final Migration MIGRATION_1_2 =
            new Migration(1, 2) {
                @Override
                public void migrate(SupportSQLiteDatabase database) {
                    database.execSQL(
                            "ALTER TABLE pending_operations " +
                                    "ADD COLUMN createdAt INTEGER NOT NULL DEFAULT 0"
                    );
                }
            };
}