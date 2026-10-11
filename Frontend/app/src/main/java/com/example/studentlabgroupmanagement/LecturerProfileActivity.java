package com.example.studentlabgroupmanagement;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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

import com.example.studentlabgroupmanagement.data.local.AppDatabase;
import com.example.studentlabgroupmanagement.data.local.entity.StudentEntity;
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

    private SessionManager sessionManager;
    private String username;
    private Uri selectedImageUri;

    private final ActivityResultLauncher<String> imagePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    selectedImageUri = uri;
                    imgProfile.setImageURI(uri);
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

        sessionManager = new SessionManager(this);

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

        username = getIntent().getStringExtra("USERNAME");
        if (username == null || username.isEmpty()) {
            username = sessionManager.getUsername();
        }

        loadLecturerData();

        if (btnUploadImage != null) {
            btnUploadImage.setOnClickListener(v -> imagePickerLauncher.launch("image/*"));
        }

        btnUpdateProfile.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String department = etDepartment.getText().toString().trim();
            String phone = etPhone.getText().toString().trim();

            if (name.isEmpty() || email.isEmpty()) {
                Toast.makeText(this, "Please fill in all required fields", Toast.LENGTH_SHORT).show();
                return;
            }

            if (username != null && !username.isEmpty()) {
                new Thread(() -> {
                    AppDatabase db = AppDatabase.getInstance(getApplicationContext());
                    StudentEntity lecturer = db.studentDao().getStudentById(username);

                    if (lecturer != null) {
                        lecturer.setName(name);
                        lecturer.setEmail(email);
                        lecturer.setProgram(department);
                        lecturer.setStudentNum(phone);
                        db.studentDao().updateStudent(lecturer);

                        if (selectedImageUri != null) {
                            SharedPreferences prefs = getSharedPreferences("LecturerPrefs", Context.MODE_PRIVATE);
                            prefs.edit().putString("profile_image_" + username, selectedImageUri.toString()).apply();
                        }

                        runOnUiThread(() -> {
                            Toast.makeText(this, "Profile Updated Successfully!", Toast.LENGTH_SHORT).show();
                            finish();
                        });
                    } else {
                        runOnUiThread(() -> Toast.makeText(this, "Failed to update profile", Toast.LENGTH_SHORT).show());
                    }
                }).start();
            } else {
                Toast.makeText(this, "No active user session found", Toast.LENGTH_SHORT).show();
            }
        });

        btnBack.setOnClickListener(v -> finish());
    }

    private void loadLecturerData() {
        if (username != null && !username.isEmpty()) {
            new Thread(() -> {
                StudentEntity lecturer = AppDatabase.getInstance(getApplicationContext())
                        .studentDao()
                        .getStudentById(username);

                runOnUiThread(() -> {
                    if (lecturer != null) {
                        if (lecturer.getName() != null) etName.setText(lecturer.getName());
                        if (lecturer.getEmail() != null) etEmail.setText(lecturer.getEmail());
                        if (lecturer.getProgram() != null) etDepartment.setText(lecturer.getProgram());
                        if (lecturer.getStudentNum() != null) etPhone.setText(lecturer.getStudentNum());
                    }

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
                });
            }).start();
        }
    }
}