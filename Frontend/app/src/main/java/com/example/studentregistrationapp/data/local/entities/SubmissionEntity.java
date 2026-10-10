package com.yourpackage.data.local.entities;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "submissions")
public class SubmissionEntity {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public int labId;
    public int studentId;
    public String studentNumber;
    public String filePath;
    public String submittedAt;
    public String status; // 'PENDING' or 'CHECKED'
}