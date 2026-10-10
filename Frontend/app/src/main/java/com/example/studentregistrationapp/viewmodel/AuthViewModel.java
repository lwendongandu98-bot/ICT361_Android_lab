package com.example.studentregistrationapp.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import com.example.studentregistrationapp.data.model.Student;
import com.example.studentregistrationapp.data.repository.StudentRepository;
import java.util.List;

public class StudentViewModel extends ViewModel {

    private final StudentRepository repository = new StudentRepository();

    // Expose LiveData so UI Activities can observe database/server updates
    public LiveData<List<Student>> getStudents() {
        return repository.getObservedStudents();
    }

    public void registerStudent(Student student) {
        repository.registerStudent(student);
    }
}