package com.example.studentregistrationapp.data.local.entities;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.annotation.NonNull;

@Entity(tableName = "students")
public class StudentEntity {
    @PrimaryKey
    @NonNull
    private String studentId;
    private String username;
    private String password;
    private String fullName;

    public StudentEntity(@NonNull String studentId, String username, String password, String fullName) {
        this.studentId = studentId;
        this.username = username;
        this.password = password;
        this.fullName = fullName;
    }

    @NonNull
    public String getStudentId() { return studentId; }
    public void setStudentId(@NonNull String studentId) { this.studentId = studentId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
}