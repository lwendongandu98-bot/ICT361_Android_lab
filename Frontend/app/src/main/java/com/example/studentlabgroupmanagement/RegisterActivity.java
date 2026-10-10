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

import java.util.List;

public class RegisterActivity extends AppCompatActivity {

    private EditText etUsername, etName, etStudentNum, etEmail, etPassword;
    private Spinner spinnerProgram, spinnerGroup;
    private Button btnRegister;
    private TextView tvLogin;

    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        dbHelper = new DatabaseHelper(this);

        etUsername = findViewById(R.id.etUsername);
        etName = findViewById(R.id.etName);
        etStudentNum = findViewById(R.id.etStudentNum);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        spinnerProgram = findViewById(R.id.spinnerProgram);
        spinnerGroup = findViewById(R.id.spinnerGroup);
        btnRegister = findViewById(R.id.btnRegister);
        tvLogin = findViewById(R.id.tvLogin);

        // Populate Program/Course Spinner dynamically from DB
        loadCourseSpinner();

        // Populate Lab Group Spinner dynamically from DB
        loadLabGroupSpinner();

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

            boolean isInserted = dbHelper.insertUser(username, name, studentNum, email, password, program, labGroup, role);

            if (isInserted) {
                // Save session so StudentHomeActivity picks up the details immediately
                SessionManager sessionManager = new SessionManager(RegisterActivity.this);
                sessionManager.createLoginSession(username, role);

                SharedPreferences sharedPref = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putString("username", username);
                editor.apply();

                Toast.makeText(RegisterActivity.this, "Registration Successful!", Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(RegisterActivity.this, StudentHomeActivity.class);
                intent.putExtra("username", username);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(RegisterActivity.this, "Registration Failed. Username may already exist.", Toast.LENGTH_SHORT).show();
            }
        });

        if (tvLogin != null) {
            tvLogin.setOnClickListener(v -> finish());
        }
    }

    private void loadCourseSpinner() {
        List<String> courseList = dbHelper.getAllCourses();

        if (courseList.isEmpty()) {
            courseList.add("No courses available");
        }

        ArrayAdapter<String> courseAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                courseList
        );
        courseAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerProgram.setAdapter(courseAdapter);
    }

    private void loadLabGroupSpinner() {
        List<String> groupList = dbHelper.getAvailableLabGroups();

        if (groupList.isEmpty()) {
            groupList.add("Unassigned");
        }

        ArrayAdapter<String> groupAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                groupList
        );
        groupAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerGroup.setAdapter(groupAdapter);
    }
}