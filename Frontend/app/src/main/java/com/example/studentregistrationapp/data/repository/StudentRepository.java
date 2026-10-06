package com.example.studentregistrationapp.data.repository;

import com.example.studentregistrationapp.data.model.Student;
import java.util.ArrayList;
import java.util.List;

public class StudentRepository {

    private final List<Student> studentList = new ArrayList<>();

    public StudentRepository() {
        Student s1 = new Student();
        s1.setName("Victor");
        s1.setUsername("victor123");
        s1.setStudentNumber("STU001");
        s1.setEmail("victor@example.com");
        s1.setPassword("pass123");
        studentList.add(s1);
    }

    public List<Student> getStudents() {
        return studentList;
    }

    public Student getStudentById(long studentId) {
        return studentList.isEmpty() ? null : studentList.get(0);
    }

    public void registerStudent(Student student) {
        studentList.add(student);
    }

    public void updateStudent(Student student) {
        // Mock update logic
    }

    public void deleteStudent(long studentId) {
        // Mock delete logic
    }
}