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

public class StudentRegisterActivity extends AppCompatActivity {

    private EditText etRegName, etRegUsername, etRegStudentNum, etRegEmail, etRegPassword, etRegConfirmPassword;
    private Spinner spRegProgram, spRegGroup;
    private Button btnRegisterSubmit;
    private TextView tvBackToLogin;

    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_register);

        dbHelper = new DatabaseHelper(this);

        // Bind exact layout XML IDs
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

        // Fetch lecturer-created courses and groups dynamically
        loadCoursesFromDatabase();
        loadGroupsFromDatabase();

        // Registration button handler
        btnRegisterSubmit.setOnClickListener(v -> handleRegistration());

        // Navigation back to login screen
        if (tvBackToLogin != null) {
            tvBackToLogin.setOnClickListener(v -> finish());
        }
    }

    private void loadCoursesFromDatabase() {
        List<String> courseList = dbHelper.getAllCourses();

        if (courseList == null || courseList.isEmpty()) {
            courseList.add("No courses added by lecturer yet");
        } else {
            courseList.add(0, "Select Program / Course");
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                courseList
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spRegProgram.setAdapter(adapter);
    }

    private void loadGroupsFromDatabase() {
        // Fetch groups with capacity under 15 members
        List<String> groupList = dbHelper.getAvailableLabGroups();

        // Fallback to all groups if available groups query is empty
        if (groupList == null || groupList.isEmpty()) {
            groupList = dbHelper.getAllGroups();
        }

        if (groupList == null || groupList.isEmpty()) {
            groupList.add("No lab groups available");
        } else {
            groupList.add(0, "Select Lab Group");
        }

        ArrayAdapter<String> groupAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                groupList
        );
        groupAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spRegGroup.setAdapter(groupAdapter);
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

        // Form Validations
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

        // Insert new student user into database
        boolean isInserted = dbHelper.insertUser(username, name, studentNum, email, password, selectedProgram, selectedGroup, "Student");

        if (isInserted) {
            // Save Session
            SessionManager sessionManager = new SessionManager(this);
            sessionManager.createLoginSession(username, "Student");

            SharedPreferences sharedPref = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString("username", username);
            editor.apply();

            Toast.makeText(this, "Registration Successful!", Toast.LENGTH_SHORT).show();

            // Redirect to Student Dashboard
            Intent intent = new Intent(StudentRegisterActivity.this, StudentHomeActivity.class);
            intent.putExtra("username", username);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, "Registration failed. Username may already be taken.", Toast.LENGTH_SHORT).show();
        }
    }
}