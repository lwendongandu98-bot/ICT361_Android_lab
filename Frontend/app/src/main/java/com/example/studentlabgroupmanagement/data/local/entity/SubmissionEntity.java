package com.example.studentlabgroupmanagement.data.local.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.google.gson.annotations.SerializedName;

@Entity(tableName = "submissions")
public class SubmissionEntity {
    @PrimaryKey(autoGenerate = true)
    private Integer id; // Wrapper Integer allows nulls so MySQL auto-increment works on sync

    @SerializedName(value = "lab_id", alternate = {"labId"})
    private int labId;

    @SerializedName(value = "student_number", alternate = {"studentNumber", "studentId"})
    private String studentNumber;

    @SerializedName(value = "file_path", alternate = {"filePath", "file"})
    private String filePath;

    @SerializedName(value = "submitted_at", alternate = {"submittedAt", "createdAt"})
    private String submittedAt;

    @SerializedName(value = "grade", alternate = {"score", "marks"})
    private String grade;

    @SerializedName(value = "status", alternate = {"submissionStatus"})
    private String status;

    @ColumnInfo(name = "checked_at")
    @SerializedName(value = "checked_at", alternate = {"checkedAt", "gradedAt"})
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

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

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