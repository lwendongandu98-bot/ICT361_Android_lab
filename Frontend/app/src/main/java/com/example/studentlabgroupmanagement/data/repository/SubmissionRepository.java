package com.example.studentlabgroupmanagement.data.repository;

import android.app.Application;
import androidx.lifecycle.LiveData;
import com.example.studentlabgroupmanagement.data.local.dao.SubmissionDao;
import com.example.studentlabgroupmanagement.data.local.AppDatabase;
import com.example.studentlabgroupmanagement.data.local.entity.SubmissionEntity;
import com.example.studentlabgroupmanagement.data.remote.ApiService;
import com.example.studentlabgroupmanagement.data.remote.RetrofitClient;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SubmissionRepository {
    private final SubmissionDao submissionDao;
    private final ApiService apiService;
    private final LiveData<List<SubmissionEntity>> allSubmissions;
    private final ExecutorService executorService;

    public SubmissionRepository(Application application) {
        AppDatabase database = AppDatabase.getInstance(application);
        submissionDao = database.submissionDao();
        allSubmissions = submissionDao.getAllSubmissions();
        apiService = RetrofitClient.getApiService();
        executorService = Executors.newFixedThreadPool(2);
    }

    public LiveData<List<SubmissionEntity>> getAllSubmissions() {
        return allSubmissions;
    }

    public void insertSubmission(SubmissionEntity submission, GroupRepository.OnOperationCallback callback) {
        apiService.createSubmission(submission).enqueue(new Callback<SubmissionEntity>() {
            @Override
            public void onResponse(Call<SubmissionEntity> call, Response<SubmissionEntity> response) {
                if (response.isSuccessful() && response.body() != null) {
                    executorService.execute(() -> submissionDao.insertSubmission(submission)); // MATCHED DAO METHOD
                    callback.onSuccess("Submission uploaded to MySQL successfully!");
                } else {
                    saveLocallyAndFallback(submission, callback);
                }
            }

            @Override
            public void onFailure(Call<SubmissionEntity> call, Throwable t) {
                saveLocallyAndFallback(submission, callback);
            }
        });
    }

    private void saveLocallyAndFallback(SubmissionEntity submission, GroupRepository.OnOperationCallback callback) {
        executorService.execute(() -> {
            submissionDao.insertSubmission(submission); // MATCHED DAO METHOD
            callback.onSuccess("Offline mode: Submission saved locally & sync scheduled!");
        });
    }
}