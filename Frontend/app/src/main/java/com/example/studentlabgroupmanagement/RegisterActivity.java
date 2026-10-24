package com.example.studentlabgroupmanagement;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.work.Constraints;
import androidx.work.ExistingWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.example.studentlabgroupmanagement.data.local.AppDatabase;
import com.example.studentlabgroupmanagement.data.local.entity.CourseEntity;
import com.example.studentlabgroupmanagement.data.local.entity.LabGroupEntity;
import com.example.studentlabgroupmanagement.data.local.entity.StudentEntity;
import com.example.studentlabgroupmanagement.data.remote.RetrofitClient;
import com.example.studentlabgroupmanagement.data.worker.DataSyncWorker;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends AppCompatActivity {

    private EditText etUsername, etName, etStudentNum, etEmail, etPassword;
    private Spinner spinnerProgram, spinnerGroup;
    private Button btnRegister;
    private TextView tvLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        etUsername = findViewById(R.id.etUsername);
        etName = findViewById(R.id.etName);
        etStudentNum = findViewById(R.id.etStudentNum);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        spinnerProgram = findViewById(R.id.spinnerProgram);
        spinnerGroup = findViewById(R.id.spinnerGroup);
        btnRegister = findViewById(R.id.btnRegister);
        tvLogin = findViewById(R.id.tvLogin);

        loadSpinnersData();

        btnRegister.setOnClickListener(v -> {
            String username = etUsername.getText().toString().trim();
            String name = etName.getText().toString().trim();
            String studentNum = etStudentNum.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            String program = spinnerProgram.getSelectedItem() != null ? spinnerProgram.getSelectedItem().toString() : "";
            String labGroup = spinnerGroup.getSelectedItem() != null ? spinnerGroup.getSelectedItem().toString() : "";
            String role = "Student";

            if (username.isEmpty() || name.isEmpty() || password.isEmpty() || studentNum.isEmpty() || email.isEmpty()) {
                Toast.makeText(RegisterActivity.this, "Please fill in all required fields", Toast.LENGTH_SHORT).show();
                return;
            }

            if (program.equals("No courses available")) {
                Toast.makeText(RegisterActivity.this, "Cannot register: No courses available yet.", Toast.LENGTH_SHORT).show();
                return;
            }

            btnRegister.setEnabled(false); // Prevent duplicate taps

            new Thread(() -> {
                AppDatabase db = AppDatabase.getInstance(getApplicationContext());
                StudentEntity existing = db.studentDao().getStudentByIdentifier(username);

                if (existing != null) {
                    runOnUiThread(() -> {
                        Toast.makeText(RegisterActivity.this, "Registration Failed. Username or account may already exist.", Toast.LENGTH_LONG).show();
                        btnRegister.setEnabled(true);
                    });
                } else {
                    // Hash password with SHA-256 for local Room compatibility
                    String hashedPassword = hashPassword(password);
                    StudentEntity newStudent = new StudentEntity(username, name, studentNum, email, hashedPassword, program, labGroup, role);

                    // 1. Save locally into Room database immediately so UI/Database Inspector shows it right away
                    db.studentDao().insertStudent(newStudent);

                    // 2. Push asynchronously to MySQL server via Retrofit
                    RetrofitClient.getApiService().createStudent(newStudent).enqueue(new Callback<StudentEntity>() {
                        @Override
                        public void onResponse(Call<StudentEntity> call, Response<StudentEntity> response) {
                            if (response.isSuccessful()) {
                                Log.d("RegisterSync", "Student successfully pushed to MySQL server!");
                            } else {
                                Log.e("RegisterSync", "MySQL server rejected registration code: " + response.code());
                            }
                        }

                        @Override
                        public void onFailure(Call<StudentEntity> call, Throwable t) {
                            Log.w("RegisterSync", "Offline mode or server unreachable: " + t.getMessage() + ". WorkManager backup queue will handle sync.");
                        }
                    });

                    // 3. Trigger WorkManager backup sync task with REPLACE policy
                    Constraints constraints = new Constraints.Builder()
                            .setRequiredNetworkType(NetworkType.CONNECTED)
                            .build();

                    OneTimeWorkRequest syncRequest = new OneTimeWorkRequest.Builder(DataSyncWorker.class)
                            .setConstraints(constraints)
                            .build();

                    WorkManager.getInstance(getApplicationContext()).enqueueUniqueWork(
                            "FullAppSyncWork",
                            ExistingWorkPolicy.REPLACE,
                            syncRequest
                    );

                    // 4. Finalize session and navigate to home screen on Main Thread
                    runOnUiThread(() -> {
                        SessionManager sessionManager = new SessionManager(RegisterActivity.this);
                        sessionManager.createLoginSession(username, studentNum, email, name, role);

                        SharedPreferences sharedPref = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
                        sharedPref.edit().putString("username", username).apply();

                        Toast.makeText(RegisterActivity.this, "Registration successful!", Toast.LENGTH_SHORT).show();

                        Intent intent = new Intent(RegisterActivity.this, StudentHomeActivity.class);
                        intent.putExtra("username", username);
                        startActivity(intent);
                        finish();
                    });
                }
            }).start();
        });

        if (tvLogin != null) {
            tvLogin.setOnClickListener(v -> finish());
        }
    }

    private void loadSpinnersData() {
        new Thread(() -> {
            AppDatabase db = AppDatabase.getInstance(getApplicationContext());
            List<CourseEntity> courses = db.courseDao().getAllCoursesSync();
            List<LabGroupEntity> groups = db.labGroupDao().getAllGroupsSync();

            List<String> courseList = new ArrayList<>();
            if (courses != null) {
                for (CourseEntity c : courses) {
                    if (c.getCourseName() != null) courseList.add(c.getCourseName());
                }
            }
            if (courseList.isEmpty()) {
                courseList.add("No courses available");
            }

            List<String> groupList = new ArrayList<>();
            if (groups != null) {
                for (LabGroupEntity g : groups) {
                    if (g.getGroupName() != null) groupList.add(g.getGroupName());
                }
            }
            if (groupList.isEmpty()) {
                groupList.add("Unassigned");
            }

            runOnUiThread(() -> {
                ArrayAdapter<String> courseAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, courseList);
                courseAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerProgram.setAdapter(courseAdapter);

                ArrayAdapter<String> groupAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, groupList);
                groupAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerGroup.setAdapter(groupAdapter);
            });
        }).start();
    }

    // Helper method to hash passwords using SHA-256 for local Room compatibility
    private String hashPassword(String password) {
        try {
            java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes("UTF-8"));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}