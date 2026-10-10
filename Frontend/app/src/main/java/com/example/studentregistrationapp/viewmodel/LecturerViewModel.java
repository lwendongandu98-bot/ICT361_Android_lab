package com.example.studentregistrationapp.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.studentregistrationapp.data.model.Lecturer;
import com.example.studentregistrationapp.data.repository.LecturerRepository;
import java.util.List;

public class LecturerViewModel extends ViewModel {

    private final LecturerRepository repository = new LecturerRepository();
    private final MutableLiveData<Lecturer> selectedLecturer = new MutableLiveData<>();

    // Expose observed list from repository
    public LiveData<List<Lecturer>> getLecturers() {
        return repository.getObservedLecturers();
    }

    public LiveData<Lecturer> getSelectedLecturer() {
        return selectedLecturer;
    }

    public void selectLecturer(Lecturer lecturer) {
        selectedLecturer.setValue(lecturer);
    }
}