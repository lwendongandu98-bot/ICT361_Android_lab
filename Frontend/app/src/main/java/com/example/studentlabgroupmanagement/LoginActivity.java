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

import com.example.studentlabgroupmanagement.data.remote.LoginRequest;
import com.example.studentlabgroupmanagement.data.remote.LoginResponse;
import com.example.studentlabgroupmanagement.data.remote.RetrofitClient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

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

        // LOGIN BUTTON - Authenticates via Retrofit (Node.js backend + MySQL + Bcrypt)
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

            Log.d("LoginDebug", "Attempting network login for identifier: '" + inputIdentifier + "' with role: '" + role + "'");

            // Create login request payload
            LoginRequest loginRequest = new LoginRequest(inputIdentifier, password);

            // Execute Retrofit Login Call
            RetrofitClient.getApiService().loginUser(loginRequest).enqueue(new Callback<LoginResponse>() {
                @Override
                public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        LoginResponse loginResponse = response.body();
                        Toast.makeText(LoginActivity.this, "Login Successful!", Toast.LENGTH_SHORT).show();

                        try {
                            if (session != null) {
                                LoginResponse.UserDto user = loginResponse.getUser();
                                String name = (user != null && user.getName() != null) ? user.getName() : inputIdentifier;

                                // Save comprehensive session details
                                session.createLoginSession(
                                        inputIdentifier,
                                        "",
                                        "",
                                        name,
                                        role
                                );
                            }
                        } catch (Exception e) {
                            Log.e("LoginActivity", "Session save error: " + e.getMessage());
                        }

                        // Navigate based on role
                        if (role.equalsIgnoreCase("Student")) {
                            startActivity(new Intent(LoginActivity.this, StudentHomeActivity.class));
                            finish();
                        } else if (role.equalsIgnoreCase("Lecturer")) {
                            startActivity(new Intent(LoginActivity.this, LecturerHomeActivity.class));
                            finish();
                        }
                    } else {
                        Toast.makeText(LoginActivity.this, "Invalid credentials! Please check your username/password.", Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onFailure(Call<LoginResponse> call, Throwable t) {
                    Log.e("LoginError", "Network failure: " + t.getMessage());
                    Toast.makeText(LoginActivity.this, "Network error: Make sure your server is running and reachable.", Toast.LENGTH_LONG).show();
                }
            });
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
}