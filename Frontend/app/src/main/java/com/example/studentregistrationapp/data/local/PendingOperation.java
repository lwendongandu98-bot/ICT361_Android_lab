package com.example.studentregistrationapp.data.local;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "pending_operations")
public class PendingOperation {

    @PrimaryKey
    private String operationId;

    private int studentId;
    private String accountId;
    private String type;
    private String payload;
    private int baseVersion;

    // Used to preserve the order operations were created
    private long createdAt;

    public PendingOperation(
            String operationId,
            int studentId,
            String accountId,
            String type,
            String payload,
            int baseVersion,
            long createdAt) {

        this.operationId = operationId;
        this.studentId = studentId;
        this.accountId = accountId;
        this.type = type;
        this.payload = payload;
        this.baseVersion = baseVersion;
        this.createdAt = createdAt;
    }

    public String getOperationId() {
        return operationId;
    }

    public int getStudentId() {
        return studentId;
    }

    public String getAccountId() {
        return accountId;
    }

    public String getType() {
        return type;
    }

    public String getPayload() {
        return payload;
    }

    public int getBaseVersion() {
        return baseVersion;
    }

    public long getCreatedAt() {
        return createdAt;
    }
}