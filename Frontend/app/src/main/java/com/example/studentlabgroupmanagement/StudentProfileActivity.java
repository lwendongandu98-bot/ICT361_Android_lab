package com.example.studentlabgroupmanagement;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.studentlabgroupmanagement.data.local.AppDatabase;
import com.example.studentlabgroupmanagement.data.local.entity.StudentEntity;

public class StudentProfileActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private FrameLayout profileImageContainer;
    private ImageView imgProfile;
    private EditText etStudentName, etStudentEmail, etStudentProgram, etStudentAge;
    private Button btnUpdateProfile, btnBackToDashboard;
    private LinearLayout headerProfile;

    private SessionManager sessionManager;
    private String currentUsername;
    private Uri selectedImageUri;

    private ActivityResultLauncher<Intent> imagePickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_profile);

        sessionManager = new SessionManager(this);

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
        headerProfile = findViewById(R.id.headerProfile); // Ensure your activity_student_profile.xml top header layout has this ID

        // --- DYNAMICALLY PUSH HEADER BELOW STATUS BAR ---
        if (headerProfile != null) {
            ViewCompat.setOnApplyWindowInsetsListener(headerProfile, (v, windowInsets) -> {
                int statusBarHeight = windowInsets.getInsets(WindowInsetsCompat.Type.statusBars()).top;

                // Set top padding to the height of the status bar so content doesn't clip,
                // while the background color stretches to the absolute top edge!
                v.setPadding(v.getPaddingLeft(), statusBarHeight + 16, v.getPaddingRight(), v.getPaddingBottom());

                return windowInsets;
            });
        }
        // ------------------------------------------------

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
            final String targetUser = currentUsername.trim();
            new Thread(() -> {
                StudentEntity student = AppDatabase.getInstance(getApplicationContext())
                        .studentDao()
                        .getStudentById(targetUser);

                runOnUiThread(() -> {
                    if (student != null) {
                        if (student.getName() != null) etStudentName.setText(student.getName());
                        if (student.getEmail() != null) etStudentEmail.setText(student.getEmail());
                        if (student.getProgram() != null) etStudentProgram.setText(student.getProgram());
                    }

                    SharedPreferences prefs = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
                    String savedUriStr = prefs.getString("profile_image_" + targetUser, null);
                    if (savedUriStr != null) {
                        try {
                            imgProfile.setImageURI(Uri.parse(savedUriStr));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            }).start();
        }
    }

    private void updateStudentDetails() {
        String name = etStudentName.getText().toString().trim();
        String email = etStudentEmail.getText().toString().trim();
        String program = etStudentProgram.getText().toString().trim();

        if (name.isEmpty()) {
            etStudentName.setError("Name cannot be empty");
            return;
        }

        if (currentUsername != null && !currentUsername.trim().isEmpty()) {
            final String targetUser = currentUsername.trim();
            new Thread(() -> {
                AppDatabase db = AppDatabase.getInstance(getApplicationContext());
                StudentEntity student = db.studentDao().getStudentById(targetUser);
                if (student != null) {
                    student.setName(name);
                    student.setEmail(email);
                    student.setProgram(program);
                    db.studentDao().updateStudent(student);

                    runOnUiThread(() -> {
                        Toast.makeText(this, "Profile updated successfully!", Toast.LENGTH_SHORT).show();
                        finish();
                    });
                } else {
                    runOnUiThread(() -> Toast.makeText(this, "Failed to update profile", Toast.LENGTH_SHORT).show());
                }
            }).start();
        }
    }
}