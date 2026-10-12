package com.example.studentlabgroupmanagement.ui.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import com.example.studentlabgroupmanagement.data.local.entity.AnnouncementEntity;
import com.example.studentlabgroupmanagement.data.repository.AnnouncementRepository;
import com.example.studentlabgroupmanagement.data.repository.GroupRepository;
import java.util.List;

public class AnnouncementsViewModel extends AndroidViewModel {
    private AnnouncementRepository repository;
    private LiveData<List<AnnouncementEntity>> allAnnouncements;

    public AnnouncementsViewModel(@NonNull Application application) {
        super(application);
        repository = new AnnouncementRepository(application);
        allAnnouncements = repository.getAllAnnouncements();
    }

    public LiveData<List<AnnouncementEntity>> getAllAnnouncements() {
        return allAnnouncements;
    }

    public void insertAnnouncement(AnnouncementEntity announcement, GroupRepository.OnOperationCallback callback) {
        repository.insertAnnouncement(announcement, callback);
    }
}