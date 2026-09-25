package com.example.studentregistrationapp.data.model;

public class Student {

    private long studentId;
    private String studentNumber;
    private String studentName;
    private String programme;
    private String group;

    public Student(long studentId, String studentNumber, String studentName,
                   String programme, String group) {
        this.studentId = studentId;
        this.studentNumber = studentNumber;
        this.studentName = studentName;
        this.programme = programme;
        this.group = group;
    }

    public long getStudentId() {
        return studentId;
    }

    public String getStudentNumber() {
        return studentNumber;
    }

    public String getStudentName() {
        return studentName;
    }

    public String getProgramme() {
        return programme;
    }

    public String getGroup() {
        return group;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public void setProgramme(String programme) {
        this.programme = programme;
    }

    public void setGroup(String group) {
        this.group = group;
    }
}