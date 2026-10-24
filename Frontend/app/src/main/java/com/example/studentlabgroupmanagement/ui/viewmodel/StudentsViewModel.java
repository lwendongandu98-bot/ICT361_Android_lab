package com.example.studentlabgroupmanagement.ui.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import com.example.studentlabgroupmanagement.data.local.entity.StudentEntity;
import com.example.studentlabgroupmanagement.data.repository.StudentRepository;
import com.example.studentlabgroupmanagement.data.repository.GroupRepository;
import java.util.List;

public class StudentsViewModel extends AndroidViewModel {
    private StudentRepository repository;
    private LiveData<List<StudentEntity>> allStudents;

    public StudentsViewModel(@NonNull Application application) {
        super(application);
        repository = new StudentRepository(application);
        allStudents = repository.getAllStudents();
    }

    public LiveData<List<StudentEntity>> getAllStudents() {
        return allStudents;
    }

    public void createStudent(StudentEntity student, GroupRepository.OnOperationCallback callback) {
        repository.insertStudent(student, callback);
    }
}