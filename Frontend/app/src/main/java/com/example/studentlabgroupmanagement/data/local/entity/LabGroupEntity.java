package com.example.studentlabgroupmanagement.data.local.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import com.google.gson.annotations.SerializedName;

@Entity(tableName = "lab_groups")
public class LabGroupEntity {
    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "group_name")
    @SerializedName("group_name") // Matches Node.js req.body.group_name
    private String groupName;

    @SerializedName("max_occupancy") // Matches Node.js req.body.max_occupancy
    private int maxOccupancy;

    // Default constructor required by Room
    public LabGroupEntity() {}

    public LabGroupEntity(String groupName, int maxOccupancy) {
        this.groupName = groupName;
        this.maxOccupancy = maxOccupancy;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getGroupName() { return groupName; }
    public void setGroupName(String groupName) { this.groupName = groupName; }

    public int getMaxOccupancy() { return maxOccupancy; }
    public void setMaxOccupancy(int maxOccupancy) { this.maxOccupancy = maxOccupancy; }
}