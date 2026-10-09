package com.example.studentlabgroupmanagement;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

public class StudentProfileActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private FrameLayout profileImageContainer;
    private ImageView imgProfile;
    private EditText etStudentName, etStudentEmail, etStudentProgram, etStudentAge;
    private Button btnUpdateProfile, btnBackToDashboard;

    private DatabaseHelper dbHelper;
    private SessionManager sessionManager;
    private String currentUsername;
    private Uri selectedImageUri;

    private ActivityResultLauncher<Intent> imagePickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_profile);

        dbHelper = new DatabaseHelper(this);
        sessionManager = new SessionManager(this);

        // Retrieve active username
        currentUsername = getIntent().getStringExtra("USERNAME");
        if (currentUsername == null || currentUsername.trim().isEmpty()) {
            currentUsername = sessionManager.getUsername();
        }
        if (currentUsername == null || currentUsername.trim().isEmpty()) {
            SharedPreferences prefs = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
            currentUsername = prefs.getString("username", "");
        }

        btnBack = findViewById(R.id.btnBack);
        profileImageContainer = findViewById(R.id.profileImageContainer);
        imgProfile = findViewById(R.id.imgProfile);
        etStudentName = findViewById(R.id.etStudentName);
        etStudentEmail = findViewById(R.id.etStudentEmail);
        etStudentProgram = findViewById(R.id.etStudentProgram);
        etStudentAge = findViewById(R.id.etStudentAge);
        btnUpdateProfile = findViewById(R.id.btnUpdateProfile);
        btnBackToDashboard = findViewById(R.id.btnBackToDashboard);

        // Image picker launcher for internal storage
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        selectedImageUri = result.getData().getData();
                        if (selectedImageUri != null) {
                            try {
                                getContentResolver().takePersistableUriPermission(
                                        selectedImageUri,
                                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                                );
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            imgProfile.setImageURI(selectedImageUri);
                            saveProfileImageUri(selectedImageUri.toString());
                        }
                    }
                }
        );

        profileImageContainer.setOnClickListener(v -> openImagePicker());

        loadStudentDetails();

        btnBack.setOnClickListener(v -> finish());
        btnBackToDashboard.setOnClickListener(v -> finish());

        btnUpdateProfile.setOnClickListener(v -> updateStudentDetails());
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        imagePickerLauncher.launch(intent);
    }

    private void saveProfileImageUri(String uriStr) {
        if (currentUsername != null && !currentUsername.trim().isEmpty()) {
            SharedPreferences prefs = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
            prefs.edit().putString("profile_image_" + currentUsername.trim(), uriStr).apply();
            Toast.makeText(this, "Profile picture updated successfully!", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadStudentDetails() {
        if (currentUsername != null && !currentUsername.trim().isEmpty()) {
            Cursor cursor = dbHelper.getUserProfile(currentUsername.trim());
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    int nameIdx = cursor.getColumnIndex("student_name");
                    int emailIdx = cursor.getColumnIndex("email");
                    int progIdx = cursor.getColumnIndex("program");
                    int ageIdx = cursor.getColumnIndex("age");

                    String name = (nameIdx != -1 && !cursor.isNull(nameIdx)) ? cursor.getString(nameIdx) : "";
                    String email = (emailIdx != -1 && !cursor.isNull(emailIdx)) ? cursor.getString(emailIdx) : "";
                    String program = (progIdx != -1 && !cursor.isNull(progIdx)) ? cursor.getString(progIdx) : "";
                    String age = (ageIdx != -1 && !cursor.isNull(ageIdx)) ? cursor.getString(ageIdx) : "";

                    etStudentName.setText(name);
                    etStudentEmail.setText(email);
                    etStudentProgram.setText(program);
                    etStudentAge.setText(age);
                }
                cursor.close();
            }

            // Load saved image if available
            SharedPreferences prefs = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
            String savedUriStr = prefs.getString("profile_image_" + currentUsername.trim(), null);
            if (savedUriStr != null) {
                try {
                    imgProfile.setImageURI(Uri.parse(savedUriStr));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void updateStudentDetails() {
        String name = etStudentName.getText().toString().trim();
        String email = etStudentEmail.getText().toString().trim();
        String program = etStudentProgram.getText().toString().trim();
        String age = etStudentAge.getText().toString().trim();

        if (name.isEmpty()) {
            etStudentName.setError("Name cannot be empty");
            return;
        }

        boolean success = dbHelper.updateUserProfile(currentUsername, name, email, program, age);
        if (success) {
            Toast.makeText(this, "Profile updated successfully!", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Failed to update profile", Toast.LENGTH_SHORT).show();
        }
    }
}