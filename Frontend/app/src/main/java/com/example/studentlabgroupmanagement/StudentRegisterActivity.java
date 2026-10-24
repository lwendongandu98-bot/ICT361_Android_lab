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

import com.example.studentlabgroupmanagement.data.local.AppDatabase;
import com.example.studentlabgroupmanagement.data.local.entity.CourseEntity;
import com.example.studentlabgroupmanagement.data.local.entity.LabGroupEntity;
import com.example.studentlabgroupmanagement.data.local.entity.OperationEntity;
import com.example.studentlabgroupmanagement.data.local.entity.StudentEntity;
import com.example.studentlabgroupmanagement.data.remote.RetrofitClient;
import com.example.studentlabgroupmanagement.data.worker.SyncHelper;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import retrofit2.Response;

public class StudentRegisterActivity extends AppCompatActivity {

    private EditText etRegName, etRegUsername, etRegStudentNum, etRegEmail, etRegPassword, etRegConfirmPassword;
    private Spinner spRegProgram, spRegGroup;
    private Button btnRegisterSubmit;
    private TextView tvBackToLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_register);

        etRegName = findViewById(R.id.etRegName);
        etRegUsername = findViewById(R.id.etRegUsername);
        etRegStudentNum = findViewById(R.id.etRegStudentNum);
        etRegEmail = findViewById(R.id.etRegEmail);
        etRegPassword = findViewById(R.id.etRegPassword);
        etRegConfirmPassword = findViewById(R.id.etRegConfirmPassword);
        spRegProgram = findViewById(R.id.spRegProgram);
        spRegGroup = findViewById(R.id.spRegGroup);
        btnRegisterSubmit = findViewById(R.id.btnRegisterSubmit);
        tvBackToLogin = findViewById(R.id.tvBackToLogin);

        loadSpinnersData();

        btnRegisterSubmit.setOnClickListener(v -> handleRegistration());

        if (tvBackToLogin != null) {
            tvBackToLogin.setOnClickListener(v -> finish());
        }
    }

    private void loadSpinnersData() {
        new Thread(() -> {
            AppDatabase db = AppDatabase.getInstance(getApplicationContext());

            try {
                Response<List<LabGroupEntity>> groupResponse = RetrofitClient.getApiService().getAllGroups().execute();
                if (groupResponse.isSuccessful() && groupResponse.body() != null) {
                    for (LabGroupEntity g : groupResponse.body()) {
                        try {
                            db.labGroupDao().insertGroup(g);
                        } catch (Exception ignored) {}
                    }
                }

                Response<List<CourseEntity>> courseResponse = RetrofitClient.getApiService().getAllCourses().execute();
                if (courseResponse.isSuccessful() && courseResponse.body() != null) {
                    for (CourseEntity c : courseResponse.body()) {
                        try {
                            db.courseDao().insertCourse(c);
                        } catch (Exception ignored) {}
                    }
                }
            } catch (Exception e) {
                Log.w("StudentRegister", "Network sync failed, falling back to local cache: " + e.getMessage());
            }

            List<CourseEntity> courses = db.courseDao().getAllCoursesSync();
            List<LabGroupEntity> groups = db.labGroupDao().getAllGroupsSync();

            List<String> courseList = new ArrayList<>();
            if (courses != null) {
                for (CourseEntity c : courses) {
                    if (c.getCourseName() != null) {
                        String displayStr = (c.getCourseCode() != null && !c.getCourseCode().isEmpty())
                                ? c.getCourseCode() + " - " + c.getCourseName()
                                : c.getCourseName();
                        courseList.add(displayStr);
                    }
                }
            }
            if (courseList.isEmpty()) {
                courseList.add("No courses added by lecturer yet");
            } else {
                courseList.add(0, "Select Program / Course");
            }

            List<String> groupList = new ArrayList<>();
            if (groups != null) {
                for (LabGroupEntity g : groups) {
                    if (g.getGroupName() != null) groupList.add(g.getGroupName());
                }
            }
            if (groupList.isEmpty()) {
                groupList.add("No lab groups available");
            } else {
                groupList.add(0, "Select Lab Group");
            }

            runOnUiThread(() -> {
                ArrayAdapter<String> courseAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, courseList);
                courseAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spRegProgram.setAdapter(courseAdapter);

                ArrayAdapter<String> groupAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, groupList);
                groupAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spRegGroup.setAdapter(groupAdapter);
            });
        }).start();
    }

    private void handleRegistration() {
        String name = etRegName.getText().toString().trim();
        String username = etRegUsername.getText().toString().trim();
        String studentNum = etRegStudentNum.getText().toString().trim();
        String email = etRegEmail.getText().toString().trim();
        String password = etRegPassword.getText().toString().trim();
        String confirmPassword = etRegConfirmPassword.getText().toString().trim();

        String selectedProgram = spRegProgram.getSelectedItem() != null ? spRegProgram.getSelectedItem().toString() : "";
        String selectedGroup = spRegGroup.getSelectedItem() != null ? spRegGroup.getSelectedItem().toString() : "";

        if (name.isEmpty() || username.isEmpty() || studentNum.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill in all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedProgram.equals("Select Program / Course") || selectedProgram.equals("No courses added by lecturer yet")) {
            Toast.makeText(this, "Please select a valid course added by the lecturer", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedGroup.equals("Select Lab Group") || selectedGroup.equals("No lab groups available")) {
            Toast.makeText(this, "Please select a valid lab group", Toast.LENGTH_SHORT).show();
            return;
        }

        new Thread(() -> {
            AppDatabase db = AppDatabase.getInstance(getApplicationContext());
            StudentEntity existing = db.studentDao().getStudentByIdentifier(username);

            if (existing != null) {
                runOnUiThread(() -> Toast.makeText(this, "Registration failed. Username or account already exists.", Toast.LENGTH_SHORT).show());
            } else {
                // Hash password with SHA-256 so local Room login matches LoginActivity
                String hashedPassword = hashPassword(password);

                StudentEntity newStudent = new StudentEntity(username, name, studentNum, email, hashedPassword, selectedProgram, selectedGroup, "Student");
                db.studentDao().insertStudent(newStudent);

                Gson gson = new Gson();
                String payload = gson.toJson(newStudent);
                String operationId = UUID.randomUUID().toString();

                OperationEntity operation = new OperationEntity(
                        operationId,
                        username,
                        username,
                        "REGISTER_USER",
                        payload,
                        1,
                        System.currentTimeMillis()
                );
                db.operationDao().insertOperation(operation);

                SyncHelper.triggerSync(this);

                runOnUiThread(() -> {
                    SessionManager sessionManager = new SessionManager(this);
                    // Populate comprehensive session data
                    sessionManager.createLoginSession(username, studentNum, email, name, "Student");

                    SharedPreferences sharedPref = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putString("username", username);
                    editor.apply();

                    Toast.makeText(this, "Registration saved locally! Will sync when online.", Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(StudentRegisterActivity.this, StudentHomeActivity.class);
                    intent.putExtra("username", username);
                    startActivity(intent);
                    finish();
                });
            }
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