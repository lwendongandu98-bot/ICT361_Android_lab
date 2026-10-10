package com.example.studentlabgroupmanagement.data.repository;

import android.content.Context;
import com.example.studentlabgroupmanagement.data.local.AppDatabase;
import com.example.studentlabgroupmanagement.data.local.entity.OperationEntity;
import com.example.studentlabgroupmanagement.data.local.entity.StudentEntity;
import java.util.UUID;

public class StudentRepository {
    private final AppDatabase db;

    public StudentRepository(Context context) {
        db = AppDatabase.getInstance(context);
    }

    // Save proposed edit and operation atomically in a Room transaction
    public void saveOfflineEdit(String studentId, String accountId, String type, String payload, int baseVersion) {
        db.runInTransaction(() -> {
            StudentEntity student = db.studentDao().getStudentById(studentId);
            if (student != null) {
                student.setSyncStatus("Pending");
                db.studentDao().updateStudent(student);
            }

            String operationId = UUID.randomUUID().toString();
            OperationEntity operation = new OperationEntity(
                    operationId, studentId, accountId, type, payload, baseVersion, System.currentTimeMillis()
            );
            db.operationDao().insertOperation(operation);
        });
    }
}