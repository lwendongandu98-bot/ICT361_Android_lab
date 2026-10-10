package com.example.studentlabgroupmanagement;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class LecturerRegisterActivity extends AppCompatActivity {

    private EditText etFullName;
    private EditText etEmployeeNo;
    private EditText etDepartment;
    private EditText etEmail;
    private EditText etPhone;
    private EditText etPassword;
    private EditText etConfirmPassword;

    private Button btnRegister;

    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lecturer_register);

        dbHelper = new DatabaseHelper(this);

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

            boolean result = dbHelper.insertUser(
                    employeeNo,
                    fullName,
                    employeeNo,
                    email,
                    password,
                    department,
                    "LECTURER",
                    "Lecturer"
            );

            if (result) {

                Toast.makeText(
                        LecturerRegisterActivity.this,
                        "Lecturer Registered Successfully",
                        Toast.LENGTH_SHORT
                ).show();

                startActivity(
                        new Intent(
                                LecturerRegisterActivity.this,
                                MainActivity.class
                        )
                );

                finish();

            } else {

                Toast.makeText(
                        LecturerRegisterActivity.this,
                        "Registration Failed or User Exists",
                        Toast.LENGTH_SHORT
                ).show();
            }
        });
    }
}
