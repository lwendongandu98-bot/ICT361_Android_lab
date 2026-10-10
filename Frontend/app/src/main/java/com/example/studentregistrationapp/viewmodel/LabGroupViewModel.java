package com.example.studentregistrationapp.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.studentregistrationapp.data.model.LabGroup;
import com.example.studentregistrationapp.data.repository.LabGroupRepository;
import java.util.List;

public class LabGroupViewModel extends ViewModel {

    private final LabGroupRepository repository = new LabGroupRepository();
    private final MutableLiveData<String> selectionStatus = new MutableLiveData<>();

    // Fetch live lab groups from repository (Room / Node.js backend)
    public LiveData<List<LabGroup>> getLabGroups() {
        return repository.getObservedLabGroups();
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