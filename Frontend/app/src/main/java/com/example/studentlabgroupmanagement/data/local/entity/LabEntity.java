package com.example.studentlabgroupmanagement.data.local.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "labs")
public class LabEntity {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private String title;
    private String description;
    private String dueDate;
    private String filePath;
    private String status;

    // Default constructor required by Room
    public LabEntity() {}

    public LabEntity(String title, String description, String dueDate, String filePath, String status) {
        this.title = title;
        this.description = description;
        this.dueDate = dueDate;
        this.filePath = filePath;
        this.status = status;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getDueDate() { return dueDate; }
    public void setDueDate(String dueDate) { this.dueDate = dueDate; }

    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}