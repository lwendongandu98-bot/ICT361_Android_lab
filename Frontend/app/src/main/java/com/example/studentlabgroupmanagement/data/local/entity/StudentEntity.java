package com.example.studentlabgroupmanagement.data.local.entity;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.google.gson.annotations.SerializedName;

@Keep
@Entity(tableName = "users")
public class StudentEntity {

    @PrimaryKey
    @NonNull
    @Keep
    @SerializedName(value = "username", alternate = {"user_name"})
    private String username;

    @Keep
    @SerializedName(value = "name", alternate = {"full_name", "fullname"})
    private String name;

    @Keep
    @SerializedName(value = "studentNum", alternate = {"student_number", "studentnumber", "student_no", "studentNo"})
    private String studentNum;

    @Keep
    @SerializedName(value = "email", alternate = {"email_address", "mail"})
    private String email;

    @Keep
    @SerializedName(value = "password", alternate = {"pass", "pwd", "password_hash"})
    private String password;

    @Keep
    @SerializedName(value = "program", alternate = {"course", "course_name"})
    private String program;

    @Keep
    @SerializedName(value = "labGroup", alternate = {"lab_group", "labgroup", "group_name"})
    private String labGroup;

    @Keep
    @SerializedName(value = "role", alternate = {"user_role", "user_type"})
    private String role;

    // Default constructor required by Room and Gson
    public StudentEntity() {}

    public StudentEntity(@NonNull String username, String name, String studentNum, String email,
                         String password, String program, String labGroup, String role) {
        this.username = username;
        this.name = name;
        this.studentNum = studentNum;
        this.email = email;
        this.password = password;
        this.program = program;
        this.labGroup = labGroup;
        this.role = role;
    }

    @NonNull
    public String getUsername() { return username; }
    public void setUsername(@NonNull String username) { this.username = username; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getStudentNum() { return studentNum; }
    public void setStudentNum(String studentNum) { this.studentNum = studentNum; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getProgram() { return program; }
    public void setProgram(String program) { this.program = program; }

    public String getLabGroup() { return labGroup; }
    public void setLabGroup(String labGroup) { this.labGroup = labGroup; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}