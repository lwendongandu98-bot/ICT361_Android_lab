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
    private String operationType;
    private String payloadJson;
    private int baseVersion;
    private long timestamp;

    // Default constructor required by Room
    public OperationEntity() {}

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
    public void setOperationId(@NonNull String operationId) { this.operationId = operationId; }

    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }

    public String getAccountId() { return accountId; }
    public void setAccountId(String accountId) { this.accountId = accountId; }

    public String getOperationType() { return operationType; }
    public void setOperationType(String operationType) { this.operationType = operationType; }

    public String getPayloadJson() { return payloadJson; }
    public void setPayloadJson(String payloadJson) { this.payloadJson = payloadJson; }

    public int getBaseVersion() { return baseVersion; }
    public void setBaseVersion(int baseVersion) { this.baseVersion = baseVersion; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}