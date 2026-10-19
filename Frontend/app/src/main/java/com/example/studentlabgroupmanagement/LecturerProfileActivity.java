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
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.studentlabgroupmanagement.data.local.entity.StudentEntity; // Or LecturerEntity depending on your schema
import com.example.studentlabgroupmanagement.data.repository.GroupRepository;
import com.example.studentlabgroupmanagement.ui.viewmodel.LecturerProfileViewModel;
import com.google.android.material.imageview.ShapeableImageView;

public class LecturerProfileActivity extends AppCompatActivity {

    private ShapeableImageView imgProfile;
    private ImageButton btnUploadImage;
    private EditText etName, etEmail, etDepartment, etPhone;
    private Button btnUpdateProfile, btnBack;

    private SessionManager sessionManager;
    private LecturerProfileViewModel profileViewModel;
    private String username;
    private Uri selectedImageUri;

    // Track loaded entity object to update it
    private StudentEntity currentLecturer;

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

        // Initialize ViewModel
        profileViewModel = new ViewModelProvider(this).get(LecturerProfileViewModel.class);

        Toolbar toolbar = findViewById(R.id.toolbarProfile);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            }
            toolbar.setNavigationOnClickListener(v -> finish());

            // --- DYNAMICALLY PUSH TOOLBAR BELOW STATUS BAR ---
            ViewCompat.setOnApplyWindowInsetsListener(toolbar, (v, windowInsets) -> {
                int statusBarHeight = windowInsets.getInsets(WindowInsetsCompat.Type.statusBars()).top;

                // Set top padding to the height of the status bar so content doesn't clip,
                // while the background color still stretches to the absolute top edge!
                v.setPadding(v.getPaddingLeft(), statusBarHeight, v.getPaddingRight(), v.getPaddingBottom());

                return windowInsets;
            });
            // ------------------------------------------------
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

        // Load data reactively via ViewModel LiveData observer
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

            if (currentLecturer != null) {
                // Update entity fields
                currentLecturer.setName(name);
                currentLecturer.setEmail(email);
                currentLecturer.setProgram(department);
                currentLecturer.setStudentNum(phone);

                // Save profile image URI locally in SharedPreferences if changed
                if (selectedImageUri != null) {
                    SharedPreferences prefs = getSharedPreferences("LecturerPrefs", Context.MODE_PRIVATE);
                    prefs.edit().putString("profile_image_" + username, selectedImageUri.toString()).apply();
                }

                // Send update through ViewModel / Repository (Syncs with MySQL backend or falls back locally)
                profileViewModel.updateLecturerProfile(currentLecturer, new GroupRepository.OnOperationCallback() {
                    @Override
                    public void onSuccess(String message) {
                        runOnUiThread(() -> {
                            Toast.makeText(LecturerProfileActivity.this, message, Toast.LENGTH_LONG).show();
                            finish();
                        });
                    }

                    @Override
                    public void onError(String error) {
                        runOnUiThread(() -> {
                            Toast.makeText(LecturerProfileActivity.this, "Error: " + error, Toast.LENGTH_LONG).show();
                        });
                    }
                });
            } else {
                Toast.makeText(this, "No active user profile found to update", Toast.LENGTH_SHORT).show();
            }
        });

        btnBack.setOnClickListener(v -> finish());
    }

    private void loadLecturerData() {
        if (username != null && !username.isEmpty()) {
            // Observe the profile data reactively from Room/Repository
            profileViewModel.getLecturerProfile(username).observe(this, lecturer -> {
                if (lecturer != null) {
                    currentLecturer = lecturer;
                    if (lecturer.getName() != null) etName.setText(lecturer.getName());
                    if (lecturer.getEmail() != null) etEmail.setText(lecturer.getEmail());
                    if (lecturer.getProgram() != null) etDepartment.setText(lecturer.getProgram());
                    if (lecturer.getStudentNum() != null) etPhone.setText(lecturer.getStudentNum());
                }
            });

            // Load saved profile image preference
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