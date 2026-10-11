package com.example.studentlabgroupmanagement.data.local.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "lab_groups")
public class LabGroupEntity {
    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "group_name")
    private String groupName;

    private String description;

    // Default constructor required by Room
    public LabGroupEntity() {}

    public LabGroupEntity(String groupName, String description) {
        this.groupName = groupName;
        this.description = description;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getGroupName() { return groupName; }
    public void setGroupName(String groupName) { this.groupName = groupName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}