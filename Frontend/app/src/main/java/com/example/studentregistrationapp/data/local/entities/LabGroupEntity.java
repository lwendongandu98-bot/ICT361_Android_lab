package com.yourpackage.data.local.entities;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "lab_groups")
public class LabGroupEntity {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public String groupName;
    public int maxOccupancy;
}