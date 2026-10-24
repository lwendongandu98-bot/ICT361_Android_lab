package com.example.studentlabgroupmanagement.data.repository;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.LiveData;

import com.example.studentlabgroupmanagement.data.local.AppDatabase;
import com.example.studentlabgroupmanagement.data.local.dao.LabDao;
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

    private static final String TAG = "LAB_DEBUG";

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

    public void insertLab(LabEntity lab,
                          GroupRepository.OnOperationCallback callback) {

        Log.d(TAG, "insertLab() called");

        apiService.createLab(lab).enqueue(new Callback<LabEntity>() {

            @Override
            public void onResponse(Call<LabEntity> call,
                                   Response<LabEntity> response) {

                Log.d(TAG, "Response Code: " + response.code());

                if (response.isSuccessful()) {

                    Log.d(TAG, "Lab successfully saved to backend");

                    executorService.execute(() -> {
                        try {
                            labDao.insertLab(lab);
                        } catch (Exception e) {
                            Log.e(TAG,
                                    "Room insert error: "
                                            + e.getMessage());
                        }
                    });

                    callback.onSuccess(
                            "Lab created and saved successfully!"
                    );

                } else {

                    Log.e(TAG,
                            "Server returned error: "
                                    + response.code());

                    saveLocallyAndFallback(lab, callback);
                }
            }

            @Override
            public void onFailure(Call<LabEntity> call, Throwable t) {

                Log.e(TAG,
                        "API Failure: "
                                + t.getMessage());

                saveLocallyAndFallback(lab, callback);
            }
        });
    }

    private void saveLocallyAndFallback(
            LabEntity lab,
            GroupRepository.OnOperationCallback callback) {

        executorService.execute(() -> {

            try {
                labDao.insertLab(lab);

                callback.onSuccess(
                        "Offline mode: Lab saved locally."
                );

            } catch (Exception e) {

                callback.onError(
                        "Failed to save lab: "
                                + e.getMessage()
                );
            }
        });
    }
}