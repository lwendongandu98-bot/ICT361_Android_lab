package com.example.studentregistrationapp.data.local.entities;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "lab_groups")
public class LabGroupEntity {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private String groupName;
    private String courseCode;

    public LabGroupEntity(String groupName, String courseCode) {
        this.groupName = groupName;
        this.courseCode = courseCode;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getGroupName() { return groupName; }
    public void setGroupName(String groupName) { this.groupName = groupName; }

    public String getCourseCode() { return courseCode; }
    public void setCourseCode(String courseCode) { this.courseCode = courseCode; }
}