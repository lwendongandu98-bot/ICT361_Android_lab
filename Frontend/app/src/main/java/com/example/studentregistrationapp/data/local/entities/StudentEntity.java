package com.yourpackage.data.local.entities;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "students")
public class StudentEntity {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public String fullName;
    public String username;
    public String studentNumber;
    public String email;
    public String password;
    public String program;
    public int labGroupId;
}