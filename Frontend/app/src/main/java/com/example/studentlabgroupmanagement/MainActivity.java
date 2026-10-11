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

public class MainActivity extends AppCompatActivity {

    private EditText etUsername, etPassword;
    private RadioGroup rgRole;
    private Button btnLogin;
    private TextView tvRegisterLink, tvForgotPassword;

    private SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try {
            session = new SessionManager(this);
        } catch (Exception e) {
            Log.e("MainActivity", "SessionManager initialization failed", e);
        }

        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        rgRole = findViewById(R.id.rgRole);
        btnLogin = findViewById(R.id.btnLogin);
        tvRegisterLink = findViewById(R.id.tvRegisterLink);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);

        if (etUsername == null || etPassword == null || rgRole == null || btnLogin == null) {
            Toast.makeText(this, "Layout Error: Check activity_main.xml IDs", Toast.LENGTH_LONG).show();
            return;
        }

        // LOGIN BUTTON
        btnLogin.setOnClickListener(v -> {
            String username = etUsername.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            int selectedId = rgRole.getCheckedRadioButtonId();

            if (selectedId == -1) {
                Toast.makeText(MainActivity.this, "Please select Student or Lecturer", Toast.LENGTH_SHORT).show();
                return;
            }

            RadioButton rbSelected = findViewById(selectedId);
            if (rbSelected == null) {
                Toast.makeText(MainActivity.this, "Please select a valid role", Toast.LENGTH_SHORT).show();
                return;
            }

            String selectedRoleText = rbSelected.getText().toString().trim();
            String role = selectedRoleText.equalsIgnoreCase("Student") ? "Student" : "Lecturer";

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(MainActivity.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            new Thread(() -> {
                StudentEntity student = AppDatabase.getInstance(getApplicationContext())
                        .studentDao()
                        .getStudentById(username);

                boolean isValid = student != null
                        && password.equals(student.getPassword())
                        && role.equalsIgnoreCase(student.getRole());

                runOnUiThread(() -> {
                    if (isValid) {
                        try {
                            if (session != null) {
                                session.createLoginSession(username, role);
                            }
                        } catch (Exception e) {
                            Log.e("MainActivity", "Session save error: " + e.getMessage());
                        }

                        Toast.makeText(MainActivity.this, "Login Successful!", Toast.LENGTH_SHORT).show();

                        if (role.equalsIgnoreCase("Student")) {
                            Intent intent = new Intent(MainActivity.this, StudentHomeActivity.class);
                            startActivity(intent);
                            finish();
                        } else if (role.equalsIgnoreCase("Lecturer")) {
                            Intent intent = new Intent(MainActivity.this, LecturerHomeActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    } else {
                        Toast.makeText(MainActivity.this, "Invalid credentials!", Toast.LENGTH_SHORT).show();
                    }
                });
            }).start();
        });

        if (tvForgotPassword != null) {
            tvForgotPassword.setOnClickListener(v ->
                    startActivity(new Intent(MainActivity.this, ForgotPasswordActivity.class))
            );
        }

        if (tvRegisterLink != null) {
            tvRegisterLink.setOnClickListener(v -> {
                int selectedId = rgRole.getCheckedRadioButtonId();

                if (selectedId == -1) {
                    Toast.makeText(MainActivity.this, "Please select Student or Lecturer first", Toast.LENGTH_SHORT).show();
                    return;
                }

                RadioButton rbSelected = findViewById(selectedId);
                String role = rbSelected.getText().toString();

                if (role.equalsIgnoreCase("Student")) {
                    startActivity(new Intent(MainActivity.this, StudentRegisterActivity.class));
                } else if (role.equalsIgnoreCase("Lecturer")) {
                    startActivity(new Intent(MainActivity.this, LecturerRegisterActivity.class));
                }
            });
        }
    }
}