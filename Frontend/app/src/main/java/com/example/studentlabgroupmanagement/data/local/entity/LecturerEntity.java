package com.example.studentlabgroupmanagement.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.google.gson.annotations.SerializedName;

@Entity(tableName = "lecturers")
public class LecturerEntity {

    @PrimaryKey
    @NonNull
    @SerializedName("employee_number")
    @ColumnInfo(name = "employee_number")
    private String employeeNumber;

    @SerializedName("full_name")
    @ColumnInfo(name = "full_name")
    private String fullName;

    @SerializedName("department")
    @ColumnInfo(name = "department")
    private String department;

    @SerializedName("email")
    @ColumnInfo(name = "email")
    private String email;

    @SerializedName("phone_number")
    @ColumnInfo(name = "phone_number")
    private String phoneNumber;

    @SerializedName("password")
    @ColumnInfo(name = "password")
    private String password;

    public LecturerEntity(@NonNull String employeeNumber, String fullName, String department, String email, String phoneNumber, String password) {
        this.employeeNumber = employeeNumber;
        this.fullName = fullName;
        this.department = department;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.password = password;
    }

    // Getters and Setters
    @NonNull
    public String getEmployeeNumber() { return employeeNumber; }
    public void setEmployeeNumber(@NonNull String employeeNumber) { this.employeeNumber = employeeNumber; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}