package com.example.studentlabgroupmanagement;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
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
import com.example.studentlabgroupmanagement.data.worker.DataSyncWorker;

import java.util.ArrayList;
import java.util.List;

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

            if (username.isEmpty() || name.isEmpty() || password.isEmpty()) {
                Toast.makeText(RegisterActivity.this, "Please fill in all required fields", Toast.LENGTH_SHORT).show();
                return;
            }

            if (program.equals("No courses available")) {
                Toast.makeText(RegisterActivity.this, "Cannot register: No courses available yet.", Toast.LENGTH_SHORT).show();
                return;
            }

            new Thread(() -> {
                AppDatabase db = AppDatabase.getInstance(getApplicationContext());
                StudentEntity existing = db.studentDao().getStudentById(username);

                if (existing != null) {
                    runOnUiThread(() -> Toast.makeText(RegisterActivity.this, "Registration Failed. Username may already exist.", Toast.LENGTH_SHORT).show());
                } else {
                    StudentEntity newStudent = new StudentEntity(username, name, studentNum, email, password, program, labGroup, role);
                    db.studentDao().insertStudent(newStudent);

                    // --- TRIGGER WORKMANAGER BACKGROUND SYNC ---
                    Constraints constraints = new Constraints.Builder()
                            .setRequiredNetworkType(NetworkType.CONNECTED)
                            .build();

                    OneTimeWorkRequest syncRequest = new OneTimeWorkRequest.Builder(DataSyncWorker.class)
                            .setConstraints(constraints)
                            .build();

                    WorkManager.getInstance(getApplicationContext()).enqueueUniqueWork(
                            "FullAppSyncWork",
                            ExistingWorkPolicy.KEEP,
                            syncRequest
                    );
                    // ------------------------------------------

                    runOnUiThread(() -> {
                        SessionManager sessionManager = new SessionManager(RegisterActivity.this);
                        sessionManager.createLoginSession(username, role);

                        SharedPreferences sharedPref = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
                        sharedPref.edit().putString("username", username).apply();

                        Toast.makeText(RegisterActivity.this, "Registration saved locally! Will sync when online.", Toast.LENGTH_SHORT).show();

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
            // FIXED: Using synchronous query methods for background worker threads
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
}