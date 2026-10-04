package com.example.studentlabgroupmanagement;

import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class StudentRegisterActivity extends AppCompatActivity {

    private EditText etRegUsername,
            etRegName,
            etRegStudentNum,
            etRegEmail,
            etRegPassword,
            etRegConfirmPassword;

    private Spinner spRegProgram,
            spRegGroup;

    private Button btnRegisterSubmit;
    private TextView tvBackToLogin;

    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_register);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Register");
        }

        dbHelper = new DatabaseHelper(this);

        etRegUsername = findViewById(R.id.etRegUsername);
        etRegName = findViewById(R.id.etRegName);
        etRegStudentNum = findViewById(R.id.etRegStudentNum);
        etRegEmail = findViewById(R.id.etRegEmail);
        etRegPassword = findViewById(R.id.etRegPassword);
        etRegConfirmPassword = findViewById(R.id.etRegConfirmPassword);

        spRegProgram = findViewById(R.id.spRegProgram);
        spRegGroup = findViewById(R.id.spRegGroup);

        btnRegisterSubmit = findViewById(R.id.btnRegisterSubmit);
        tvBackToLogin = findViewById(R.id.tvBackToLogin);

        String[] programs = {
                "Select program",
                "BSc Computing",
                "BSc Information Technology",
                "BSc Software Engineering"
        };

        ArrayAdapter<String> progAdapter =
                new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_spinner_item,
                        programs
                );

        progAdapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );

        spRegProgram.setAdapter(progAdapter);

        String[] groups = {
                "Select group",
                "Group 1",
                "Group 2",
                "Group 3",
                "Group 4"
        };

        ArrayAdapter<String> grpAdapter =
                new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_spinner_item,
                        groups
                );

        grpAdapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );

        spRegGroup.setAdapter(grpAdapter);

        btnRegisterSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String username =
                        etRegUsername.getText().toString().trim();

                String name =
                        etRegName.getText().toString().trim();

                String studentNum =
                        etRegStudentNum.getText().toString().trim();

                String email =
                        etRegEmail.getText().toString().trim();

                String password =
                        etRegPassword.getText().toString().trim();

                String confirmPassword =
                        etRegConfirmPassword.getText().toString().trim();

                String program =
                        spRegProgram.getSelectedItem().toString();

                String labGroup =
                        spRegGroup.getSelectedItem().toString();

                if (username.isEmpty()
                        || name.isEmpty()
                        || studentNum.isEmpty()
                        || email.isEmpty()
                        || password.isEmpty()
                        || confirmPassword.isEmpty()) {

                    Toast.makeText(
                            StudentRegisterActivity.this,
                            "Please fill out all fields!",
                            Toast.LENGTH_SHORT
                    ).show();

                    return;
                }

                if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {

                    etRegEmail.setError(
                            "Please enter a valid email address"
                    );

                    etRegEmail.requestFocus();

                    return;
                }

                if (program.equals("Select program")
                        || labGroup.equals("Select group")) {

                    Toast.makeText(
                            StudentRegisterActivity.this,
                            "Please select a valid Program and Lab Group!",
                            Toast.LENGTH_SHORT
                    ).show();

                    return;
                }

                if (!password.equals(confirmPassword)) {

                    Toast.makeText(
                            StudentRegisterActivity.this,
                            "Passwords do not match!",
                            Toast.LENGTH_SHORT
                    ).show();

                    return;
                }

                if (password.length() < 4) {

                    Toast.makeText(
                            StudentRegisterActivity.this,
                            "Password must be at least 4 characters!",
                            Toast.LENGTH_SHORT
                    ).show();

                    return;
                }

                boolean success = dbHelper.insertUser(
                        username,
                        name,
                        studentNum,
                        email,
                        password,
                        program,
                        labGroup,
                        "Student"
                );

                if (success) {

                    Toast.makeText(
                            StudentRegisterActivity.this,
                            "Account Created Successfully!",
                            Toast.LENGTH_LONG
                    ).show();

                    finish();

                } else {

                    Toast.makeText(
                            StudentRegisterActivity.this,
                            "Username already exists!",
                            Toast.LENGTH_LONG
                    ).show();
                }
            }
        });

        tvBackToLogin.setOnClickListener(v -> finish());
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}