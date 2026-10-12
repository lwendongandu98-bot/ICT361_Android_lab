package com.example.studentlabgroupmanagement;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.studentlabgroupmanagement.data.local.AppDatabase;
import com.example.studentlabgroupmanagement.data.local.entity.StudentEntity;

public class LoginActivity extends AppCompatActivity {

    private EditText etUsername, etPassword;
    private RadioGroup rgRole;
    private Button btnLogin;
    private TextView tvRegisterLink, tvForgotPassword;

    private SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.loginactivity);

        try {
            session = new SessionManager(this);
        } catch (Exception e) {
            Log.e("LoginActivity", "SessionManager initialization failed", e);
        }

        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        rgRole = findViewById(R.id.rgRole);
        btnLogin = findViewById(R.id.btnLogin);
        tvRegisterLink = findViewById(R.id.tvRegisterLink);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);

        if (etUsername == null || etPassword == null || rgRole == null || btnLogin == null) {
            Toast.makeText(this, "Layout Error: Check LoginActivity.xml IDs", Toast.LENGTH_LONG).show();
            return;
        }

        // LOGIN BUTTON
        btnLogin.setOnClickListener(v -> {
            final String username = etUsername.getText().toString().trim();
            final String password = etPassword.getText().toString().trim();

            int selectedId = rgRole.getCheckedRadioButtonId();

            if (selectedId == -1) {
                Toast.makeText(LoginActivity.this, "Please select Student or Lecturer", Toast.LENGTH_SHORT).show();
                return;
            }

            RadioButton rbSelected = findViewById(selectedId);
            if (rbSelected == null) {
                Toast.makeText(LoginActivity.this, "Please select a valid role", Toast.LENGTH_SHORT).show();
                return;
            }

            String selectedRoleText = rbSelected.getText().toString().trim();
            final String role = selectedRoleText.equalsIgnoreCase("Student") ? "Student" : "Lecturer";

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(LoginActivity.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            new Thread(() -> {
                Log.d("LoginDebug", "Attempting login for username: '" + username + "' with role: '" + role + "'");

                StudentEntity student = AppDatabase.getInstance(getApplicationContext())
                        .studentDao()
                        .getStudentById(username);

                boolean userExists = (student != null);
                boolean passwordMatch = false;
                boolean roleMatch = false;

                if (userExists) {
                    passwordMatch = password.equals(student.getPassword());
                    roleMatch = role.equalsIgnoreCase(student.getRole());
                    Log.d("LoginDebug", "User found. Stored Password: '" + student.getPassword() + "', Stored Role: '" + student.getRole() + "'");
                } else {
                    Log.d("LoginDebug", "User NOT found in database for ID: " + username);
                }

                final boolean isValid = userExists && passwordMatch && roleMatch;
                final boolean finalUserExists = userExists;
                final boolean finalPasswordMatch = passwordMatch;

                runOnUiThread(() -> {
                    if (isValid) {
                        try {
                            if (session != null) {
                                session.createLoginSession(username, role);
                            }
                        } catch (Exception e) {
                            Log.e("LoginActivity", "Session save error: " + e.getMessage());
                        }

                        Toast.makeText(LoginActivity.this, "Login Successful!", Toast.LENGTH_SHORT).show();

                        if (role.equalsIgnoreCase("Student")) {
                            startActivity(new Intent(LoginActivity.this, StudentHomeActivity.class));
                            finish();
                        } else if (role.equalsIgnoreCase("Lecturer")) {
                            startActivity(new Intent(LoginActivity.this, LecturerHomeActivity.class));
                            finish();
                        }
                    } else {
                        if (!finalUserExists) {
                            Toast.makeText(LoginActivity.this, "User does not exist in database!", Toast.LENGTH_SHORT).show();
                        } else if (!finalPasswordMatch) {
                            Toast.makeText(LoginActivity.this, "Incorrect password!", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(LoginActivity.this, "Role mismatch (Student/Lecturer)", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }).start();
        });

        if (tvForgotPassword != null) {
            tvForgotPassword.setOnClickListener(v ->
                    startActivity(new Intent(LoginActivity.this, ForgotPasswordActivity.class))
            );
        }

        if (tvRegisterLink != null) {
            tvRegisterLink.setOnClickListener(v -> {
                int selectedId = rgRole.getCheckedRadioButtonId();

                if (selectedId == -1) {
                    Toast.makeText(LoginActivity.this, "Please select Student or Lecturer first", Toast.LENGTH_SHORT).show();
                    return;
                }

                RadioButton rbSelected = findViewById(selectedId);
                String role = rbSelected.getText().toString();

                if (role.equalsIgnoreCase("Student")) {
                    startActivity(new Intent(LoginActivity.this, StudentRegisterActivity.class));
                } else if (role.equalsIgnoreCase("Lecturer")) {
                    startActivity(new Intent(LoginActivity.this, LecturerRegisterActivity.class));
                }
            });
        }
    }
}