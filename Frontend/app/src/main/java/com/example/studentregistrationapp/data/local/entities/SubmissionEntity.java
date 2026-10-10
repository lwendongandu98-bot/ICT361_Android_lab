package com.example.studentregistrationapp.data.local.entities;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "submissions")
public class SubmissionEntity {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private String studentId;
    private int labId;
    private String status; // e.g., "PENDING", "CHECKED"

    public SubmissionEntity(String studentId, int labId, String status) {
        this.studentId = studentId;
        this.labId = labId;
        this.status = status;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }

    public int getLabId() { return labId; }
    public void setLabId(int labId) { this.labId = labId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}