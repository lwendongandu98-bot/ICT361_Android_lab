package com.example.studentlabgroupmanagement.ui.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import com.example.studentlabgroupmanagement.data.local.entity.SubmissionEntity;
import com.example.studentlabgroupmanagement.data.repository.SubmissionRepository;
import com.example.studentlabgroupmanagement.data.repository.GroupRepository;
import java.util.List;

public class SubmissionsViewModel extends AndroidViewModel {
    private SubmissionRepository repository;
    private LiveData<List<SubmissionEntity>> allSubmissions;

    public SubmissionsViewModel(@NonNull Application application) {
        super(application);
        repository = new SubmissionRepository(application);
        allSubmissions = repository.getAllSubmissions();
    }

    public LiveData<List<SubmissionEntity>> getAllSubmissions() {
        return allSubmissions;
    }

    public void createSubmission(SubmissionEntity submission, GroupRepository.OnOperationCallback callback) {
        repository.insertSubmission(submission, callback);
    }
}