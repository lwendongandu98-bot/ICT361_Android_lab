package com.example.studentregistrationapp.data.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.studentregistrationapp.data.model.LabGroup;
import java.util.ArrayList;
import java.util.List;

public class LabGroupRepository {

    private final MutableLiveData<List<LabGroup>> labGroupsLiveData = new MutableLiveData<>();

    public LabGroupRepository() {
        List<LabGroup> initialGroups = new ArrayList<>();
        initialGroups.add(new LabGroup("L1", "Lab Group 1 (Mon 08:00 - 10:00)", 30, 28));
        labGroupsLiveData.setValue(initialGroups);
    }

    // Matches getObservedLabGroups called in LabGroupViewModel
    public LiveData<List<LabGroup>> getObservedLabGroups() {
        return labGroupsLiveData;
    }

    public LiveData<List<LabGroup>> getLabGroups() {
        return labGroupsLiveData;
    }

    public void addLabGroup(LabGroup group) {
        List<LabGroup> current = labGroupsLiveData.getValue();
        if (current != null) {
            current.add(group);
            labGroupsLiveData.setValue(current);
        }
    }
}