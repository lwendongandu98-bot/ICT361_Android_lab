package com.example.studentregistrationapp.data.repository;

import com.example.studentregistrationapp.data.model.Student;

import java.util.List;

public interface StudentRepository {

    List<Student> getStudents();

    Student getStudentById(long studentId);

    void registerStudent(Student student);

    void updateStudent(Student student);

    void deleteStudent(long studentId);
}