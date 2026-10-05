package com.example.studentregistrationapp.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.studentregistrationapp.data.model.LabGroup;
import java.util.ArrayList;
import java.util.List;

public class LabGroupViewModel extends ViewModel {

    private final MutableLiveData<List<LabGroup>> labGroups = new MutableLiveData<>();
    private final MutableLiveData<String> selectionStatus = new MutableLiveData<>();

    public LabGroupViewModel() {
        loadDefaultLabGroups();
    }

    // Populate initial dummy data for testing
    private void loadDefaultLabGroups() {
        List<LabGroup> defaultGroups = new ArrayList<>();
        defaultGroups.add(new LabGroup("L1", "Lab Group 1 (Mon 08:00 - 10:00)", 30, 28));
        defaultGroups.add(new LabGroup("L2", "Lab Group 2 (Tue 10:00 - 12:00)", 30, 30)); // Full group
        defaultGroups.add(new LabGroup("L3", "Lab Group 3 (Thu 14:00 - 16:00)", 30, 15));

        labGroups.setValue(defaultGroups);
    }

    public LiveData<List<LabGroup>> getLabGroups() {
        return labGroups;
    }

    public LiveData<String> getSelectionStatus() {
        return selectionStatus;
    }

    public void selectLabGroup(LabGroup group) {
        if (group == null) {
            selectionStatus.setValue("Please select a valid lab group.");
            return;
        }

        if (group.isFull()) {
            selectionStatus.setValue("Selected group (" + group.getGroupName() + ") is full! Choose another.");
            return;
        }

        selectionStatus.setValue("Successfully joined " + group.getGroupName());
    }
}
