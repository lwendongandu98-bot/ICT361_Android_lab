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

import com.example.studentlabgroupmanagement.data.local.AppDatabase;
import com.example.studentlabgroupmanagement.data.local.entity.CourseEntity;
import com.example.studentlabgroupmanagement.data.local.entity.LabGroupEntity;
import com.example.studentlabgroupmanagement.data.local.entity.StudentEntity;

import java.util.ArrayList;
import java.util.List;

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
            List<CourseEntity> courses = db.courseDao().getAllCourses();
            List<LabGroupEntity> groups = db.labGroupDao().getAllGroups();

            List<String> courseList = new ArrayList<>();
            if (courses != null) {
                for (CourseEntity c : courses) {
                    if (c.getCourseName() != null) courseList.add(c.getCourseName());
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
            StudentEntity existing = db.studentDao().getStudentById(username);

            if (existing != null) {
                runOnUiThread(() -> Toast.makeText(this, "Registration failed. Username may already be taken.", Toast.LENGTH_SHORT).show());
            } else {
                StudentEntity newStudent = new StudentEntity(username, name, studentNum, email, password, selectedProgram, selectedGroup, "Student");
                db.studentDao().insertStudent(newStudent);

                runOnUiThread(() -> {
                    SessionManager sessionManager = new SessionManager(this);
                    sessionManager.createLoginSession(username, "Student");

                    SharedPreferences sharedPref = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putString("username", username);
                    editor.apply();

                    Toast.makeText(this, "Registration Successful!", Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(StudentRegisterActivity.this, StudentHomeActivity.class);
                    intent.putExtra("username", username);
                    startActivity(intent);
                    finish();
                });
            }
        }).start();
    }
}