package com.example.studentlabgroupmanagement;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.studentlabgroupmanagement.data.local.AppDatabase;
import com.example.studentlabgroupmanagement.data.local.entity.SubmissionEntity;
import com.example.studentlabgroupmanagement.data.remote.ApiService;
import com.example.studentlabgroupmanagement.data.remote.RetrofitClient;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SubmitLabActivity extends AppCompatActivity {

    private static final String TAG = "SubmitLabActivity";

    private EditText etLabId, etStudentNumber, etFilePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_submit_lab);

        etLabId = findViewById(R.id.etLabId);
        etStudentNumber = findViewById(R.id.etStudentNumber);
        etFilePath = findViewById(R.id.etFilePath);
        Button btnSubmitLab = findViewById(R.id.btnSubmitLab);

        btnSubmitLab.setOnClickListener(v -> submitLabWork());
    }

    private void submitLabWork() {
        String labIdStr = etLabId.getText().toString().trim();
        String studentNumber = etStudentNumber.getText().toString().trim();
        String filePath = etFilePath.getText().toString().trim();

        if (labIdStr.isEmpty() || studentNumber.isEmpty()) {
            Toast.makeText(this, "Please enter Lab ID and Student Number", Toast.LENGTH_SHORT).show();
            return;
        }

        int labId = Integer.parseInt(labIdStr);
        String currentTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());

        SubmissionEntity submission = new SubmissionEntity(
                labId,
                studentNumber,
                filePath.isEmpty() ? "content://sample/path/lab_document.pdf" : filePath,
                currentTime,
                null,
                "PENDING",
                null
        );

        // 1. Save locally in Room database
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            AppDatabase.getInstance(getApplicationContext()).submissionDao().insertSubmission(submission);
            Log.d(TAG, "Submission saved successfully to local Room cache.");
        });

        // 2. Instantly push to Node.js / MySQL backend via Retrofit
        ApiService apiService = RetrofitClient.getApiService();
        apiService.createSubmission(submission).enqueue(new Callback<SubmissionEntity>() {
            @Override
            public void onResponse(Call<SubmissionEntity> call, Response<SubmissionEntity> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(SubmitLabActivity.this, "Submission uploaded to server successfully!", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "Submission successfully pushed to MySQL server.");
                    finish();
                } else {
                    Toast.makeText(SubmitLabActivity.this, "Saved locally. Server sync pending (Code: " + response.code() + ")", Toast.LENGTH_LONG).show();
                    Log.w(TAG, "Server rejected submission push, Code: " + response.code());
                    finish();
                }
            }

            @Override
            public void onFailure(Call<SubmissionEntity> call, Throwable t) {
                Toast.makeText(SubmitLabActivity.this, "Offline mode: Saved locally. Will sync when online.", Toast.LENGTH_LONG).show();
                Log.e(TAG, "Network failure during immediate submission push: " + t.getMessage());
                finish();
            }
        });
    }
}