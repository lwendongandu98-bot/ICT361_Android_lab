package com.example.studentregistrationapp.viewmodel;

import androidx.lifecycle.ViewModel;

import com.example.studentregistrationapp.data.model.Student;
import com.example.studentregistrationapp.data.repository.StudentRepository;

import java.util.List;

public class StudentViewModel extends ViewModel {

    private final StudentRepository repository = new StudentRepository();

    public List<Student> getStudents() {
        return repository.getStudents();
    }

    public Student getStudentById(long studentId) {
        return repository.getStudentById(studentId);
    }

    public void registerStudent(Student student) {
        repository.registerStudent(student);
    }
}