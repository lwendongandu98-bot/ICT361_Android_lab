package com.example.studentlabgroupmanagement.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "operation_queue")
public class OperationEntity {
    @PrimaryKey
    @NonNull
    private String operationId;
    private String studentId;
    private String accountId;
    private String operationType; // "UPDATE", "GROUP_TRANSFER", "DELETE"
    private String payloadJson;
    private int baseVersion;
    private long timestamp;

    public OperationEntity(@NonNull String operationId, String studentId, String accountId,
                           String operationType, String payloadJson, int baseVersion, long timestamp) {
        this.operationId = operationId;
        this.studentId = studentId;
        this.accountId = accountId;
        this.operationType = operationType;
        this.payloadJson = payloadJson;
        this.baseVersion = baseVersion;
        this.timestamp = timestamp;
    }

    @NonNull
    public String getOperationId() { return operationId; }
    public String getStudentId() { return studentId; }
    public String getAccountId() { return accountId; }
    public String getOperationType() { return operationType; }
    public String getPayloadJson() { return payloadJson; }
    public int getBaseVersion() { return baseVersion; }
    public long getTimestamp() { return timestamp; }
}