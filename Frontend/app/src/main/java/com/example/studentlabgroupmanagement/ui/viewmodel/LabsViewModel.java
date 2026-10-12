package com.example.studentlabgroupmanagement.ui.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import com.example.studentlabgroupmanagement.data.local.entity.LabEntity;
import com.example.studentlabgroupmanagement.data.repository.LabRepository;
import com.example.studentlabgroupmanagement.data.repository.GroupRepository;
import java.util.List;

public class LabsViewModel extends AndroidViewModel {
    private LabRepository repository;
    private LiveData<List<LabEntity>> allLabs;

    public LabsViewModel(@NonNull Application application) {
        super(application);
        repository = new LabRepository(application);
        allLabs = repository.getAllLabs();
    }

    public LiveData<List<LabEntity>> getAllLabs() {
        return allLabs;
    }

    public void createLab(String title, String description, String dueDate, String status, GroupRepository.OnOperationCallback callback) {
        LabEntity lab = new LabEntity();
        lab.setTitle(title);
        lab.setDescription(description);
        lab.setDueDate(dueDate);
        lab.setStatus(status);
        repository.insertLab(lab, callback);
    }
}