package com.example.studentlabgroupmanagement;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.imageview.ShapeableImageView;

public class LecturerProfileActivity extends AppCompatActivity {

    private ShapeableImageView imgProfile;
    private ImageButton btnUploadImage;
    private EditText etName;
    private EditText etEmail;
    private EditText etDepartment;
    private EditText etPhone;

    private Button btnUpdateProfile;
    private Button btnBack;

    private DatabaseHelper databaseHelper;
    private SessionManager sessionManager;
    private String username;
    private Uri selectedImageUri;

    // Image Picker Launcher
    private final ActivityResultLauncher<String> imagePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    selectedImageUri = uri;
                    imgProfile.setImageURI(uri);

                    // Persist Uri permission across app restarts
                    try {
                        getContentResolver().takePersistableUriPermission(
                                uri,
                                Intent.FLAG_GRANT_READ_URI_PERMISSION
                        );
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lecturer_profile);

        databaseHelper = new DatabaseHelper(this);
        sessionManager = new SessionManager(this);

        // Toolbar setup
        Toolbar toolbar = findViewById(R.id.toolbarProfile);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            }
            toolbar.setNavigationOnClickListener(v -> finish());
        }

        imgProfile = findViewById(R.id.imgProfile);
        btnUploadImage = findViewById(R.id.btnUploadImage);
        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etDepartment = findViewById(R.id.etDepartment);
        etPhone = findViewById(R.id.etPhone);

        btnUpdateProfile = findViewById(R.id.btnUpdateProfile);
        btnBack = findViewById(R.id.btnBack);

        // Retrieve active username from Intent or SessionManager fallback
        username = getIntent().getStringExtra("USERNAME");
        if (username == null || username.isEmpty()) {
            username = sessionManager.getUsername();
        }

        loadLecturerData();

        // Gallery picker launcher call
        if (btnUploadImage != null) {
            btnUploadImage.setOnClickListener(v -> imagePickerLauncher.launch("image/*"));
        }

        // Save updated profile details
        btnUpdateProfile.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String department = etDepartment.getText().toString().trim();

            if (name.isEmpty() || email.isEmpty()) {
                Toast.makeText(this, "Please fill in all required fields", Toast.LENGTH_SHORT).show();
                return;
            }

            if (username != null && !username.isEmpty()) {
                boolean updated = databaseHelper.updateLecturerProfile(username, name, email, department);

                if (selectedImageUri != null) {
                    SharedPreferences prefs = getSharedPreferences("LecturerPrefs", Context.MODE_PRIVATE);
                    prefs.edit().putString("profile_image_" + username, selectedImageUri.toString()).apply();
                }

                if (updated) {
                    Toast.makeText(this, "Profile Updated Successfully!", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(this, "Failed to update profile", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "No active user session found", Toast.LENGTH_SHORT).show();
            }
        });

        btnBack.setOnClickListener(v -> finish());
    }

    private void loadLecturerData() {
        if (username != null && !username.isEmpty()) {
            Cursor cursor = databaseHelper.getLecturerProfile(username);

            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    int nameIdx = cursor.getColumnIndex("name");
                    int emailIdx = cursor.getColumnIndex("email");
                    int programIdx = cursor.getColumnIndex("program");
                    int phoneIdx = cursor.getColumnIndex("student_num");

                    if (nameIdx != -1 && !cursor.isNull(nameIdx)) {
                        etName.setText(cursor.getString(nameIdx));
                    }
                    if (emailIdx != -1 && !cursor.isNull(emailIdx)) {
                        etEmail.setText(cursor.getString(emailIdx));
                    }
                    if (programIdx != -1 && !cursor.isNull(programIdx)) {
                        etDepartment.setText(cursor.getString(programIdx));
                    }
                    if (phoneIdx != -1 && !cursor.isNull(phoneIdx)) {
                        etPhone.setText(cursor.getString(phoneIdx));
                    }
                }
                cursor.close();
            }

            // Load saved profile image URI
            SharedPreferences prefs = getSharedPreferences("LecturerPrefs", Context.MODE_PRIVATE);
            String savedUriStr = prefs.getString("profile_image_" + username, null);
            if (savedUriStr != null) {
                selectedImageUri = Uri.parse(savedUriStr);
                try {
                    imgProfile.setImageURI(selectedImageUri);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}