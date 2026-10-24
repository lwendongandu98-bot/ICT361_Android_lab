package com.example.studentlabgroupmanagement;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.example.studentlabgroupmanagement.data.local.AppDatabase;
import com.example.studentlabgroupmanagement.data.local.entity.LecturerEntity;
import com.example.studentlabgroupmanagement.data.remote.RetrofitClient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LecturerRegisterActivity extends AppCompatActivity {

    private static final String TAG = "LecturerRegister";
    private EditText etFullName, etEmployeeNo, etDepartment, etEmail, etPhone, etPassword, etConfirmPassword;
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

            if (fullName.isEmpty() || employeeNo.isEmpty() || department.isEmpty() ||
                    email.isEmpty() || phone.isEmpty() || password.isEmpty() || confirm.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!password.equals(confirm)) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                return;
            }

            // Hash password for local Room storage consistency
            String hashedPassword = hashPassword(password);

            // Create the lecturer entity with the hashed password for local persistence
            LecturerEntity newLecturer = new LecturerEntity(
                    employeeNo,
                    fullName,
                    department,
                    email,
                    phone,
                    hashedPassword
            );

            // Note: For the network request, Retrofit sends the original plaintext password
            // so that your Node.js backend controller can apply its own secure bcrypt salt & hash!
            LecturerEntity serverLecturer = new LecturerEntity(
                    employeeNo,
                    fullName,
                    department,
                    email,
                    phone,
                    password
            );

            // Run database and network operations on a background thread for offline-first support
            new Thread(() -> {
                // 1. Save locally to Room database FIRST so offline authentication works instantly
                try {
                    AppDatabase.getInstance(getApplicationContext()).lecturerDao().insertLecturer(newLecturer);
                    Log.d(TAG, "Lecturer saved locally to Room SQLite database with hashed password.");
                } catch (Exception e) {
                    Log.e(TAG, "Failed to save locally to Room: " + e.getMessage());
                }

                // 2. Silently attempt to sync with remote MySQL Backend via Retrofit (no error pop-ups)
                RetrofitClient.getApiService().registerLecturer(serverLecturer).enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        if (response.isSuccessful()) {
                            Log.d(TAG, "Synced to remote MySQL server successfully.");
                        } else {
                            Log.w(TAG, "Server response code: " + response.code());
                        }
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        Log.w(TAG, "Offline mode active: Background sync skipped. Error: " + t.getMessage());
                    }
                });

                // UI updates must run on the main thread
                runOnUiThread(() -> {
                    Toast.makeText(LecturerRegisterActivity.this, "Registration Successful!", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(LecturerRegisterActivity.this, MainActivity.class));
                    finish();
                });
            }).start();
        });
    }

    // Helper method to hash passwords using SHA-256 for local Room verification
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