package com.example.studentregistrationapp.data.model;

public class Student {
    private String name;
    private String username;
    private String studentNumber;
    private String email;
    private String password;

    // Default Constructor
    public Student() {}

    // Main Constructor
    public Student(String name, String username, String studentNumber, String email, String password) {
        this.name = name;
        this.username = username;
        this.studentNumber = studentNumber;
        this.email = email;
        this.password = password;
    }

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getStudentNumber() {
        return studentNumber;
    }

    public void setStudentNumber(String studentNumber) {
        this.studentNumber = studentNumber;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
