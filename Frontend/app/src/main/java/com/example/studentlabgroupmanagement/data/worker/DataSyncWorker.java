package com.example.studentlabgroupmanagement.data.worker;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.studentlabgroupmanagement.data.local.AppDatabase;
import com.example.studentlabgroupmanagement.data.local.entity.SubmissionEntity;
import com.example.studentlabgroupmanagement.data.remote.ApiService;
import com.example.studentlabgroupmanagement.data.remote.RetrofitClient;

import java.io.File;
import java.util.List;

import retrofit2.Response;

public class DataSyncWorker extends Worker {

    private static final String TAG = "DataSyncWorker";

    public DataSyncWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        Log.d(TAG, "Background sync worker started...");

        AppDatabase db = AppDatabase.getInstance(getApplicationContext());
        ApiService apiService = RetrofitClient.getApiService();

        try {
            // Focus background sync primarily on pushing pending local student submissions to MySQL
            List<SubmissionEntity> pendingSubmissions = db.submissionDao().getAllSubmissionsSync();

            if (pendingSubmissions != null && !pendingSubmissions.isEmpty()) {
                for (SubmissionEntity sub : pendingSubmissions) {
                    try {
                        // Skip if file is missing
                        if (sub.getFilePath() != null && !sub.getFilePath().isEmpty()) {
                            File file = new File(sub.getFilePath());
                            if (!file.exists()) {
                                Log.w(TAG, "Submission file path points to non-existent local file: " + sub.getFilePath());
                            }
                        }

                        // Execute network call synchronously inside worker thread
                        Response<SubmissionEntity> response =
                                apiService.createSubmission(sub).execute();

                        Log.d(TAG, "========== SUBMISSION SYNC ==========");
                        Log.d(TAG, "Submission ID: " + sub.getId());
                        Log.d(TAG, "Lab ID: " + sub.getLabId());
                        Log.d(TAG, "Student Number: " + sub.getStudentNumber());
                        Log.d(TAG, "File Path: " + sub.getFilePath());
                        Log.d(TAG, "Response Code: " + response.code());

                        if (response.isSuccessful()) {

                            Log.d(TAG, "SUCCESS: Submission uploaded to MySQL");

                        } else {

                            String errorBody = "";

                            try {
                                errorBody = response.errorBody() != null
                                        ? response.errorBody().string()
                                        : "No error body returned";
                            } catch (Exception ex) {
                                errorBody = ex.getMessage();
                            }

                            Log.e(TAG, "FAILED TO UPLOAD");
                            Log.e(TAG, "Response Code: " + response.code());
                            Log.e(TAG, "Server Error: " + errorBody);
                        }

                        // Brief pause to release SQLite lock contention and keep UI responsive
                        Thread.sleep(50);

                    } catch (Exception e) {
                        Log.e(TAG, "Error syncing individual submission: " + e.getMessage());
                    }
                }
            }

            Log.d(TAG, "Background sync completed successfully.");
            return Result.success();

        } catch (Exception e) {
            Log.e(TAG, "Fatal error during background sync: " + e.getMessage(), e);
            return Result.retry(); // Retry later if network drops
        }
    }
}