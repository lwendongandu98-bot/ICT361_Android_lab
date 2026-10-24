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
import com.example.studentlabgroupmanagement.data.local.entity.LecturerEntity;
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
            final String inputIdentifier = etUsername.getText().toString().trim();
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

            if (inputIdentifier.isEmpty() || password.isEmpty()) {
                Toast.makeText(LoginActivity.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            new Thread(() -> {
                Log.d("LoginDebug", "Attempting login for identifier: '" + inputIdentifier + "' with role: '" + role + "'");

                AppDatabase db = AppDatabase.getInstance(getApplicationContext());
                boolean userExists = false;
                boolean passwordMatch = false;

                // Hash entered password using SHA-256 to match local Room storage
                String hashedInputPassword = hashPassword(password);

                StudentEntity loggedInStudent = null;
                LecturerEntity loggedInLecturer = null;

                if (role.equalsIgnoreCase("Student")) {
                    // Use robust identifier search (matches username, email, or studentNum)
                    loggedInStudent = db.studentDao().getStudentByIdentifier(inputIdentifier);
                    if (loggedInStudent != null) {
                        userExists = true;
                        passwordMatch = hashedInputPassword.equals(loggedInStudent.getPassword());
                        Log.d("LoginDebug", "Student found locally.");
                    }
                } else {
                    // Check local Lecturer DAO for offline authentication
                    loggedInLecturer = db.lecturerDao().getLecturerByEmployeeNo(inputIdentifier);
                    if (loggedInLecturer != null) {
                        userExists = true;
                        passwordMatch = hashedInputPassword.equals(loggedInLecturer.getPassword());
                        Log.d("LoginDebug", "Lecturer found locally.");
                    }
                }

                final boolean isValid = userExists && passwordMatch;
                final boolean finalUserExists = userExists;
                final boolean finalPasswordMatch = passwordMatch;
                final StudentEntity finalStudent = loggedInStudent;

                runOnUiThread(() -> {
                    if (isValid) {
                        try {
                            if (session != null) {
                                if (role.equalsIgnoreCase("Student") && finalStudent != null) {
                                    // Save comprehensive student session details using correct entity getters
                                    session.createLoginSession(
                                            finalStudent.getUsername(),
                                            finalStudent.getStudentNum(),
                                            finalStudent.getEmail(),
                                            finalStudent.getName(),
                                            role
                                    );
                                } else {
                                    session.createLoginSession(inputIdentifier, "", "", inputIdentifier, role);
                                }
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
                            Toast.makeText(LoginActivity.this, "User does not exist in local database! Try connecting online to sync.", Toast.LENGTH_LONG).show();
                        } else if (!finalPasswordMatch) {
                            Toast.makeText(LoginActivity.this, "Incorrect password!", Toast.LENGTH_SHORT).show();
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
                String regRole = rbSelected.getText().toString();

                if (regRole.equalsIgnoreCase("Student")) {
                    startActivity(new Intent(LoginActivity.this, StudentRegisterActivity.class));
                } else if (regRole.equalsIgnoreCase("Lecturer")) {
                    startActivity(new Intent(LoginActivity.this, LecturerRegisterActivity.class));
                }
            });
        }
    }

    // Helper method to hash passwords using SHA-256 for local Room comparison
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