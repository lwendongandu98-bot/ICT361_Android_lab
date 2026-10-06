package com.example.studentregistrationapp.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.studentregistrationapp.data.model.Lecturer;
import java.util.ArrayList;
import java.util.List;

public class LecturerViewModel extends ViewModel {

    private final MutableLiveData<List<Lecturer>> lecturers = new MutableLiveData<>();
    private final MutableLiveData<Lecturer> selectedLecturer = new MutableLiveData<>();

    public LecturerViewModel() {
        loadLecturers();
    }

    private void loadLecturers() {
        List<Lecturer> lecturerList = new ArrayList<>();
        lecturerList.add(new Lecturer("LEC01", "Dr. Smith", "smith@university.ac.zm", "Computer Science"));
        lecturerList.add(new Lecturer("LEC02", "Prof. Banda", "banda@university.ac.zm", "Information Technology"));
        lecturerList.add(new Lecturer("LEC03", "Dr. Mwewa", "mwewa@university.ac.zm", "Cybersecurity"));

        lecturers.setValue(lecturerList);
    }

    public LiveData<List<Lecturer>> getLecturers() {
        return lecturers;
    }

    public LiveData<Lecturer> getSelectedLecturer() {
        return selectedLecturer;
    }

    public void selectLecturer(Lecturer lecturer) {
        selectedLecturer.setValue(lecturer);
    }
}