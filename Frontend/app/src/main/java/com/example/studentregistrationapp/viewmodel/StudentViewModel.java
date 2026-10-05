package com.example.studentregistrationapp.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class StudentViewModel extends ViewModel {

    // LiveData holds state that the UI observes
    private final MutableLiveData<String> studentName = new MutableLiveData<>("");
    private final MutableLiveData<String> registrationStatus = new MutableLiveData<>("");

    public StudentViewModel() {
        // Initialization logic if needed
    }

    // Getters for UI observation
    public LiveData<String> getStudentName() {
        return studentName;
    }

    public LiveData<String> getRegistrationStatus() {
        return registrationStatus;
    }

    // Business logic method to process registration
    public void registerStudent(String name) {
        studentName.setValue(name);
        if (name == null || name.trim().isEmpty()) {
            registrationStatus.setValue("Registration Failed: Name cannot be empty");
        } else {
            registrationStatus.setValue("Student " + name + " registered successfully!");
        }
    }
}
