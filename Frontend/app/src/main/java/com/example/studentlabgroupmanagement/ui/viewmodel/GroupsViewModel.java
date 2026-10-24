package com.example.studentlabgroupmanagement.ui.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import com.example.studentlabgroupmanagement.data.local.entity.LabGroupEntity;
import com.example.studentlabgroupmanagement.data.repository.GroupRepository;
import java.util.List;

public class GroupsViewModel extends AndroidViewModel {
    private GroupRepository repository;
    private LiveData<List<LabGroupEntity>> allGroups;

    public GroupsViewModel(@NonNull Application application) {
        super(application);
        repository = new GroupRepository(application);
        allGroups = repository.getAllGroups();
    }

    public LiveData<List<LabGroupEntity>> getAllGroups() {
        return allGroups;
    }

    public void createGroup(String groupName, int maxOccupancy, GroupRepository.OnOperationCallback callback) {
        LabGroupEntity group = new LabGroupEntity();
        group.setGroupName(groupName);
        group.setMaxOccupancy(maxOccupancy);
        repository.insertGroup(group, callback);
    }
}