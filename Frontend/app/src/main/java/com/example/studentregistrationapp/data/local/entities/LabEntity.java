package com.yourpackage.data.local.entities;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "labs")
public class LabEntity {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public String title;
    public String description;
    public String dueDate;
    public String filePath;
    public String status; // 'DRAFT' or 'PUBLISHED'
}