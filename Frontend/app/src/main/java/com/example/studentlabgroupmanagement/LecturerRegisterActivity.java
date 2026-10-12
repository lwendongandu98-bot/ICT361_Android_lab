package com.example.studentlabgroupmanagement;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.example.studentlabgroupmanagement.data.local.AppDatabase;
import com.example.studentlabgroupmanagement.data.local.entity.StudentEntity;
import com.example.studentlabgroupmanagement.data.worker.SyncHelper;

public class LecturerRegisterActivity extends AppCompatActivity {

    private EditText etFullName;
    private EditText etEmployeeNo;
    private EditText etDepartment;
    private EditText etEmail;
    private EditText etPhone;
    private EditText etPassword;
    private EditText etConfirmPassword;

    private Button btnRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Force Light Mode so text colors remain visible on physical devices
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lecturer_register);

        etFullName = findViewById(R.id.etFullName);
        etEmployeeNo = findViewById(R.id.etEmployeeNo);
        etDepartment = findViewById(R.id.etDepartment);
        etEmail = findViewById(R.id.etEmail);
        etPhone = findViewById(R.id.etPhone);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);

        btnRegister = findViewById(R.id.btnRegister);

        btnRegister.setOnClickListener(v -> {
            String fullName = etFullName.getText().toString().trim();
            String employeeNo = etEmployeeNo.getText().toString().trim();
            String department = etDepartment.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String phone = etPhone.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            String confirm = etConfirmPassword.getText().toString().trim();

            if (fullName.isEmpty()
                    || employeeNo.isEmpty()
                    || department.isEmpty()
                    || email.isEmpty()
                    || phone.isEmpty()
                    || password.isEmpty()
                    || confirm.isEmpty()) {

                Toast.makeText(
                        LecturerRegisterActivity.this,
                        "Please fill all fields",
                        Toast.LENGTH_SHORT
                ).show();
                return;
            }

            if (!password.equals(confirm)) {
                Toast.makeText(
                        LecturerRegisterActivity.this,
                        "Passwords do not match",
                        Toast.LENGTH_SHORT
                ).show();
                return;
            }

            new Thread(() -> {
                AppDatabase db = AppDatabase.getInstance(getApplicationContext());
                StudentEntity existing = db.studentDao().getStudentById(employeeNo);

                if (existing != null) {
                    runOnUiThread(() -> Toast.makeText(
                            LecturerRegisterActivity.this,
                            "Registration Failed or User Exists",
                            Toast.LENGTH_SHORT
                    ).show());
                } else {
                    StudentEntity newLecturer = new StudentEntity(
                            employeeNo,
                            fullName,
                            phone,
                            email,
                            password,
                            department,
                            "",
                            "Lecturer"
                    );
                    db.studentDao().insertStudent(newLecturer);

                    // --- TRIGGER UNIFIED WORKMANAGER BACKGROUND SYNC ---
                    SyncHelper.triggerSync(this);
                    // --------------------------------------------------

                    runOnUiThread(() -> {
                        Toast.makeText(
                                LecturerRegisterActivity.this,
                                "Registration saved locally! Will sync when online.",
                                Toast.LENGTH_SHORT
                        ).show();

                        startActivity(new Intent(LecturerRegisterActivity.this, MainActivity.class));
                        finish();
                    });
                }
            }).start();
        });
    }
}