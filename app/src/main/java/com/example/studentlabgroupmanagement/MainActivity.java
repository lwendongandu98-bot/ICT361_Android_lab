package com.example.studentlabgroupmanagement;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private EditText etUsername, etPassword;
    private RadioGroup rgRole;
    private Button btnLogin;
    private TextView tvRegisterLink, tvForgotPassword;

    private DatabaseHelper dbHelper;
    private SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dbHelper = new DatabaseHelper(this);

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

        // Safety check for views
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

            // Normalize role string so it matches Database representation ("Student" or "Lecturer")
            String role = selectedRoleText.equalsIgnoreCase("Student") ? "Student" : "Lecturer";

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(MainActivity.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            boolean isValid = dbHelper.checkUserLogin(username, password, role);

            if (isValid) {
                try {
                    if (session != null) {
                        // Store logged-in student's username and role into SharedPreferences session
                        session.createLoginSession(username, role);
                    }
                } catch (Exception e) {
                    Log.e("MainActivity", "Session save error: " + e.getMessage());
                }

                Toast.makeText(MainActivity.this, "Login Successful!", Toast.LENGTH_SHORT).show();

                // STUDENT LOGIN
                if (role.equalsIgnoreCase("Student")) {
                    Intent intent = new Intent(MainActivity.this, StudentHomeActivity.class);
                    startActivity(intent);
                    finish();
                }
                // LECTURER LOGIN
                else if (role.equalsIgnoreCase("Lecturer")) {
                    Intent intent = new Intent(MainActivity.this, LecturerHomeActivity.class);
                    startActivity(intent);
                    finish();
                }

            } else {
                Toast.makeText(MainActivity.this, "Invalid credentials!", Toast.LENGTH_SHORT).show();
            }
        });

        // FORGOT PASSWORD
        if (tvForgotPassword != null) {
            tvForgotPassword.setOnClickListener(v ->
                    startActivity(new Intent(MainActivity.this, ForgotPasswordActivity.class))
            );
        }

        // REGISTER LINK
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