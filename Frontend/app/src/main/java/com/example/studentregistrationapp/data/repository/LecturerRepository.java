package com.example.studentregistrationapp.data.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.studentregistrationapp.data.model.Lecturer;
import java.util.ArrayList;
import java.util.List;

public class LecturerRepository {

    private final MutableLiveData<List<Lecturer>> lecturersLiveData = new MutableLiveData<>();

    public LecturerRepository() {
        List<Lecturer> initialLecturers = new ArrayList<>();
        initialLecturers.add(new Lecturer());
        lecturersLiveData.setValue(initialLecturers);
    }

    public LiveData<List<Lecturer>> getLecturers() {
        return lecturersLiveData;
    }

    public void addLecturer(Lecturer lecturer) {
        List<Lecturer> current = lecturersLiveData.getValue();
        if (current != null) {
            current.add(lecturer);
            lecturersLiveData.setValue(current);
        }
    }
}
