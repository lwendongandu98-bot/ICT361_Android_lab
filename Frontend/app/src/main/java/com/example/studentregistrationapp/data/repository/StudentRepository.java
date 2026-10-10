package com.example.studentregistrationapp.data.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.studentregistrationapp.data.model.Student;
import java.util.ArrayList;
import java.util.List;

public class StudentRepository {

    private final MutableLiveData<List<Student>> studentsLiveData = new MutableLiveData<>();
    private final List<Student> studentList = new ArrayList<>();

    public StudentRepository() {
        Student s1 = new Student();
        s1.setName("Victor");
        s1.setUsername("victor123");
        s1.setStudentNumber("STU001");
        s1.setEmail("victor@example.com");
        s1.setPassword("pass123");
        studentList.add(s1);
        studentsLiveData.setValue(studentList);
    }

    // Matches getObservedStudents called in StudentViewModel
    public LiveData<List<Student>> getObservedStudents() {
        return studentsLiveData;
    }

    public List<Student> getStudents() {
        return studentList;
    }

    public Student getStudentById(long studentId) {
        return studentList.isEmpty() ? null : studentList.get(0);
    }

    public void registerStudent(Student student) {
        studentList.add(student);
        studentsLiveData.setValue(studentList); // Notify observers
        // TODO: Add local Room insert and Volley sync to Node.js backend here
    }

    public void updateStudent(Student student) {
        // Mock update logic
    }

    public void deleteStudent(long studentId) {
        // Mock delete logic
    }
}