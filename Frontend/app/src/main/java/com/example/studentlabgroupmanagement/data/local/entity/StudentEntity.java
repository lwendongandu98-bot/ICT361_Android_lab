package com.example.studentlabgroupmanagement.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "students")
public class StudentEntity {
    @PrimaryKey
    @NonNull
    private String studentId;
    private String username;
    private String name;
    private String studentNum;
    private String email;
    private String program;
    private String labGroup;
    private int baseVersion;
    private String syncStatus; // "Saved locally", "Pending", "Synced", "Action required"

    public StudentEntity(@NonNull String studentId, String username, String name, String studentNum,
                         String email, String program, String labGroup, int baseVersion, String syncStatus) {
        this.studentId = studentId;
        this.username = username;
        this.name = name;
        this.studentNum = studentNum;
        this.email = email;
        this.program = program;
        this.labGroup = labGroup;
        this.baseVersion = baseVersion;
        this.syncStatus = syncStatus;
    }

    @NonNull
    public String getStudentId() { return studentId; }
    public String getUsername() { return username; }
    public String getName() { return name; }
    public String getStudentNum() { return studentNum; }
    public String getEmail() { return email; }
    public String getProgram() { return program; }
    public String getLabGroup() { return labGroup; }
    public int getBaseVersion() { return baseVersion; }
    public String getSyncStatus() { return syncStatus; }

    public void setSyncStatus(String syncStatus) { this.syncStatus = syncStatus; }
    public void setBaseVersion(int baseVersion) { this.baseVersion = baseVersion; }
    public void setLabGroup(String labGroup) { this.labGroup = labGroup; }
}