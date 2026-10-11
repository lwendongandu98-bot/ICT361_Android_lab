package com.example.studentlabgroupmanagement.data.local.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "submissions")
public class SubmissionEntity {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private int labId;
    private String studentNumber;
    private String filePath;
    private String submittedAt;
    private String grade;
    private String status;
    private String checkedAt;

    // Default constructor required by Room
    public SubmissionEntity() {}

    public SubmissionEntity(int labId, String studentNumber, String filePath, String submittedAt, String grade, String status, String checkedAt) {
        this.labId = labId;
        this.studentNumber = studentNumber;
        this.filePath = filePath;
        this.submittedAt = submittedAt;
        this.grade = grade;
        this.status = status;
        this.checkedAt = checkedAt;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getLabId() { return labId; }
    public void setLabId(int labId) { this.labId = labId; }

    public String getStudentNumber() { return studentNumber; }
    public void setStudentNumber(String studentNumber) { this.studentNumber = studentNumber; }

    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }

    public String getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(String submittedAt) { this.submittedAt = submittedAt; }

    public String getGrade() { return grade; }
    public void setGrade(String grade) { this.grade = grade; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getCheckedAt() { return checkedAt; }
    public void setCheckedAt(String checkedAt) { this.checkedAt = checkedAt; }
}