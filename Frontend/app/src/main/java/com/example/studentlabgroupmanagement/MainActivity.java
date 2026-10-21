package com.example.studentlabgroupmanagement;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.example.studentlabgroupmanagement.data.local.AppDatabase;
import com.example.studentlabgroupmanagement.data.local.entity.StudentEntity;
import com.example.studentlabgroupmanagement.data.remote.RetrofitClient;

import java.util.List;

import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        session = new SessionManager(this);

        // 1. Automatically fetch and sync all backend students into local Room on startup
        syncStudentsFromServer();

        // 2. Navigate to Login or Home after the 1.5-second splash delay
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (session.isLoggedIn()) {
                String role = session.getUserRole();
                if ("Student".equalsIgnoreCase(role)) {
                    startActivity(new Intent(MainActivity.this, StudentHomeActivity.class));
                } else {
                    startActivity(new Intent(MainActivity.this, LecturerHomeActivity.class));
                }
            } else {
                startActivity(new Intent(MainActivity.this, LoginActivity.class));
            }
            finish();
        }, 1500); // 1.5-second splash delay
    }

    private void syncStudentsFromServer() {
        new Thread(() -> {
            try {
                // Fetch all registered students from your Node.js/MySQL backend server
                Response<List<StudentEntity>> response = RetrofitClient.getApiService().getAllStudents().execute();

                if (response.isSuccessful() && response.body() != null) {
                    AppDatabase db = AppDatabase.getInstance(getApplicationContext());

                    for (StudentEntity student : response.body()) {
                        try {
                            // Log incoming student data to verify what the server returned
                            Log.d("MainActivitySync", "Fetched -> Username: " + student.getUsername()
                                    + ", Name: " + student.getName()
                                    + ", Email: " + student.getEmail()
                                    + ", StudentNum: " + student.getStudentNum()
                                    + ", Role: " + student.getRole());

                            // Apply safety defaults if any field is missing from the server payload
                            if (student.getRole() == null || student.getRole().isEmpty()) {
                                student.setRole("Student");
                            }
                            if (student.getLabGroup() == null || student.getLabGroup().isEmpty()) {
                                student.setLabGroup("Unassigned");
                            }

                            // Upsert/insert each backend student into local Room database
                            db.studentDao().insertStudent(student);
                        } catch (Exception e) {
                            Log.e("MainActivitySync", "Failed to insert student: " + student.getUsername(), e);
                        }
                    }
                    Log.d("MainActivitySync", "Successfully synced all backend students into local Room database.");
                } else {
                    Log.e("MainActivitySync", "Server returned error code: " + response.code());
                }
            } catch (Exception e) {
                Log.w("MainActivitySync", "Offline mode: Could not fetch students from server: " + e.getMessage());
            }
        }).start();
    }
}