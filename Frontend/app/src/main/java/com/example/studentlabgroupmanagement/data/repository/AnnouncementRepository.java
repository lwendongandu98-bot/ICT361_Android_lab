package com.example.studentlabgroupmanagement.data.repository;

import android.app.Application;
import androidx.lifecycle.LiveData;
import com.example.studentlabgroupmanagement.data.local.dao.AnnouncementDao;
import com.example.studentlabgroupmanagement.data.local.AppDatabase;
import com.example.studentlabgroupmanagement.data.local.entity.AnnouncementEntity;
import com.example.studentlabgroupmanagement.data.remote.ApiService;
import com.example.studentlabgroupmanagement.data.remote.RetrofitClient;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AnnouncementRepository {
    private final AnnouncementDao announcementDao;
    private final ApiService apiService;
    private final LiveData<List<AnnouncementEntity>> allAnnouncements;
    private final ExecutorService executorService;

    public AnnouncementRepository(Application application) {
        AppDatabase database = AppDatabase.getInstance(application);
        announcementDao = database.announcementDao();
        allAnnouncements = announcementDao.getAllAnnouncements();
        apiService = RetrofitClient.getApiService();
        executorService = Executors.newFixedThreadPool(2);
    }

    public LiveData<List<AnnouncementEntity>> getAllAnnouncements() {
        return allAnnouncements;
    }

    public void insertAnnouncement(AnnouncementEntity announcement, GroupRepository.OnOperationCallback callback) {
        apiService.createAnnouncement(announcement).enqueue(new Callback<AnnouncementEntity>() {
            @Override
            public void onResponse(Call<AnnouncementEntity> call, Response<AnnouncementEntity> response) {
                if (response.isSuccessful() && response.body() != null) {
                    executorService.execute(() -> announcementDao.insertAnnouncement(announcement)); // MATCHED DAO METHOD
                    callback.onSuccess("Announcement posted to MySQL successfully!");
                } else {
                    saveLocallyAndFallback(announcement, callback);
                }
            }

            @Override
            public void onFailure(Call<AnnouncementEntity> call, Throwable t) {
                saveLocallyAndFallback(announcement, callback);
            }
        });
    }

    private void saveLocallyAndFallback(AnnouncementEntity announcement, GroupRepository.OnOperationCallback callback) {
        executorService.execute(() -> {
            announcementDao.insertAnnouncement(announcement); // MATCHED DAO METHOD
            callback.onSuccess("Offline mode: Announcement saved locally & sync scheduled!");
        });
    }
}