package com.example.studentlabgroupmanagement.ui.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import com.example.studentlabgroupmanagement.data.local.entity.StudentEntity;
import com.example.studentlabgroupmanagement.data.repository.LecturerRepository;
import com.example.studentlabgroupmanagement.data.repository.GroupRepository;

public class LecturerProfileViewModel extends AndroidViewModel {
    private LecturerRepository repository;

    public LecturerProfileViewModel(@NonNull Application application) {
        super(application);
        repository = new LecturerRepository(application);
    }

    public LiveData<StudentEntity> getLecturerProfile(String username) {
        return repository.getLecturerByUsername(username);
    }

    public void updateLecturerProfile(StudentEntity lecturer, GroupRepository.OnOperationCallback callback) {
        repository.updateProfile(lecturer, callback);
    }
}