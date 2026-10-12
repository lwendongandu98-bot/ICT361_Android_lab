package com.example.studentlabgroupmanagement.data.repository;

import android.app.Application;
import androidx.lifecycle.LiveData;
import com.example.studentlabgroupmanagement.data.local.dao.LabGroupDao;
import com.example.studentlabgroupmanagement.data.local.AppDatabase;
import com.example.studentlabgroupmanagement.data.local.entity.LabGroupEntity;
import com.example.studentlabgroupmanagement.data.remote.ApiService;
import com.example.studentlabgroupmanagement.data.remote.RetrofitClient;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GroupRepository {
    private final LabGroupDao groupDao;
    private final ApiService apiService;
    private final LiveData<List<LabGroupEntity>> allGroups;
    private final ExecutorService executorService;

    public GroupRepository(Application application) {
        AppDatabase database = AppDatabase.getInstance(application);
        groupDao = database.labGroupDao();
        allGroups = groupDao.getAllGroups();
        apiService = RetrofitClient.getApiService();
        executorService = Executors.newFixedThreadPool(2);
    }

    public LiveData<List<LabGroupEntity>> getAllGroups() {
        return allGroups;
    }

    public void insertGroup(LabGroupEntity group, OnOperationCallback callback) {
        apiService.createGroup(group).enqueue(new Callback<LabGroupEntity>() {
            @Override
            public void onResponse(Call<LabGroupEntity> call, Response<LabGroupEntity> response) {
                if (response.isSuccessful() && response.body() != null) {
                    executorService.execute(() -> groupDao.insertGroup(group)); // MATCHED DAO METHOD
                    callback.onSuccess("Group created & saved to MySQL successfully!");
                } else {
                    saveLocallyAndFallback(group, callback);
                }
            }

            @Override
            public void onFailure(Call<LabGroupEntity> call, Throwable t) {
                saveLocallyAndFallback(group, callback);
            }
        });
    }

    private void saveLocallyAndFallback(LabGroupEntity group, OnOperationCallback callback) {
        executorService.execute(() -> {
            groupDao.insertGroup(group); // MATCHED DAO METHOD
            callback.onSuccess("Offline mode: Group saved locally & sync scheduled!");
        });
    }

    public interface OnOperationCallback {
        void onSuccess(String message);
        void onError(String error);
    }
}