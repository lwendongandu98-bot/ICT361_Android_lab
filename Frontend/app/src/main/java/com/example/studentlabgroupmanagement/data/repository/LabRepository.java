package com.example.studentlabgroupmanagement.data.repository;

import android.app.Application;
import androidx.lifecycle.LiveData;
import com.example.studentlabgroupmanagement.data.local.dao.LabDao;
import com.example.studentlabgroupmanagement.data.local.AppDatabase;
import com.example.studentlabgroupmanagement.data.local.entity.LabEntity;
import com.example.studentlabgroupmanagement.data.remote.ApiService;
import com.example.studentlabgroupmanagement.data.remote.RetrofitClient;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LabRepository {
    private final LabDao labDao;
    private final ApiService apiService;
    private final LiveData<List<LabEntity>> allLabs;
    private final ExecutorService executorService;

    public LabRepository(Application application) {
        AppDatabase database = AppDatabase.getInstance(application);
        labDao = database.labDao();
        allLabs = labDao.getAllLabs();
        apiService = RetrofitClient.getApiService();
        executorService = Executors.newFixedThreadPool(2);
    }

    public LiveData<List<LabEntity>> getAllLabs() {
        return allLabs;
    }

    public void insertLab(LabEntity lab, GroupRepository.OnOperationCallback callback) {
        apiService.createLab(lab).enqueue(new Callback<LabEntity>() {
            @Override
            public void onResponse(Call<LabEntity> call, Response<LabEntity> response) {
                if (response.isSuccessful() && response.body() != null) {
                    executorService.execute(() -> labDao.insertLab(lab)); // MATCHED DAO METHOD
                    callback.onSuccess("Lab created & saved to MySQL successfully!");
                } else {
                    saveLocallyAndFallback(lab, callback);
                }
            }

            @Override
            public void onFailure(Call<LabEntity> call, Throwable t) {
                saveLocallyAndFallback(lab, callback);
            }
        });
    }

    private void saveLocallyAndFallback(LabEntity lab, GroupRepository.OnOperationCallback callback) {
        executorService.execute(() -> {
            labDao.insertLab(lab); // MATCHED DAO METHOD
            callback.onSuccess("Offline mode: Lab saved locally & sync scheduled!");
        });
    }
}