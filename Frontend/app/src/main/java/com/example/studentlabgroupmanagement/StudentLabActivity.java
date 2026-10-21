package com.example.studentlabgroupmanagement;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.studentlabgroupmanagement.data.local.AppDatabase;
import com.example.studentlabgroupmanagement.data.local.entity.LabEntity;
import com.example.studentlabgroupmanagement.data.local.entity.SubmissionEntity;
import com.example.studentlabgroupmanagement.data.remote.RetrofitClient;
import com.example.studentlabgroupmanagement.data.worker.SyncHelper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StudentLabActivity extends AppCompatActivity {

    private LinearLayout containerLecturerLabs;
    private TextView tvNoLecturerLabs;
    private LinearLayout toolbarStudentLabs;
    private int selectedLabIdForSubmission = -1;
    private SessionManager sessionManager;

    private final ActivityResultLauncher<String> submissionFilePicker =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null && selectedLabIdForSubmission != -1) {
                    confirmAndSubmitFile(uri);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_lab);

        sessionManager = new SessionManager(this);

        containerLecturerLabs = findViewById(R.id.containerLecturerLabs);
        tvNoLecturerLabs = findViewById(R.id.tvNoLecturerLabs);
        toolbarStudentLabs = findViewById(R.id.toolbarStudentLabs);

        // --- DYNAMICALLY PUSH HEADER BELOW STATUS BAR ---
        if (toolbarStudentLabs != null) {
            ViewCompat.setOnApplyWindowInsetsListener(toolbarStudentLabs, (v, windowInsets) -> {
                int statusBarHeight = windowInsets.getInsets(WindowInsetsCompat.Type.statusBars()).top;
                v.setPadding(v.getPaddingLeft(), statusBarHeight + 8, v.getPaddingRight(), v.getPaddingBottom());
                return windowInsets;
            });
        }

        // 1. INSTANT LOAD: Display cached labs immediately from local Room DB with zero lag
        loadLecturerPublishedLabs();

        // 2. BACKGROUND SYNC: Silently check server for updates without blocking UI
        syncLabsInBackground();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh local view safely without forcing network blocks on resume
        loadLecturerPublishedLabs();
    }

    private void syncLabsInBackground() {
        RetrofitClient.getApiService().getAllLabs().enqueue(new Callback<List<LabEntity>>() {
            @Override
            public void onResponse(Call<List<LabEntity>> call, Response<List<LabEntity>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<LabEntity> serverLabs = response.body();

                    // Save to Room on background thread, then update UI cache
                    new Thread(() -> {
                        AppDatabase db = AppDatabase.getInstance(getApplicationContext());
                        for (LabEntity lab : serverLabs) {
                            try {
                                db.labDao().insertLab(lab);
                            } catch (Exception e) {
                                Log.w("StudentLabSync", "Lab sync cache note: " + e.getMessage());
                            }
                        }
                        // Refresh UI with freshly synced server data
                        loadLecturerPublishedLabs();
                    }).start();
                }
            }

            @Override
            public void onFailure(Call<List<LabEntity>> call, Throwable t) {
                Log.w("StudentLabSync", "Offline mode active: utilizing cached local labs.");
            }
        });
    }

    private void confirmAndSubmitFile(Uri uri) {
        new AlertDialog.Builder(this)
                .setTitle("Confirm Submission")
                .setMessage("Are you sure you want to submit this file for your lab assignment?")
                .setPositiveButton("Yes, Submit", (dialog, which) -> processSubmission(uri))
                .setNegativeButton("Cancel", (dialog, which) -> {
                    dialog.dismiss();
                    Toast.makeText(StudentLabActivity.this, "Submission cancelled", Toast.LENGTH_SHORT).show();
                })
                .setCancelable(false)
                .show();
    }

    private void processSubmission(Uri uri) {
        String savedPath = saveSubmissionToInternalStorage(uri);

        if (savedPath != null) {
            String submittedAt = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(new Date());
            String studentNumber = sessionManager.getUsername();
            if (studentNumber == null || studentNumber.isEmpty()) {
                studentNumber = "STUDENT123";
            }

            final String finalStudentNum = studentNumber;
            final String finalPath = savedPath;

            new Thread(() -> {
                SubmissionEntity submission = new SubmissionEntity();
                submission.setLabId(selectedLabIdForSubmission);
                submission.setStudentNumber(finalStudentNum);
                submission.setFilePath(finalPath);
                submission.setSubmittedAt(submittedAt);
                submission.setStatus("PENDING");

                AppDatabase.getInstance(getApplicationContext()).submissionDao().insertSubmission(submission);

                // --- TRIGGER BACKGROUND SYNC WORKER ---
                SyncHelper.triggerSync(this);
                // --------------------------------------

                runOnUiThread(() -> Toast.makeText(this, "Lab work saved locally! Will sync when online.", Toast.LENGTH_SHORT).show());
            }).start();
        } else {
            Toast.makeText(this, "Failed to process selected file", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadLecturerPublishedLabs() {
        new Thread(() -> {
            List<LabEntity> labs = AppDatabase.getInstance(getApplicationContext())
                    .labDao()
                    .getAllLabsSync();

            runOnUiThread(() -> {
                containerLecturerLabs.removeAllViews();
                if (labs == null || labs.isEmpty()) {
                    if (tvNoLecturerLabs != null) tvNoLecturerLabs.setVisibility(View.VISIBLE);
                    return;
                }

                if (tvNoLecturerLabs != null) tvNoLecturerLabs.setVisibility(View.GONE);
                LayoutInflater inflater = LayoutInflater.from(this);

                for (LabEntity lab : labs) {
                    if (!"PUBLISHED".equals(lab.getStatus())) {
                        continue;
                    }

                    int labId = lab.getId();
                    String title = lab.getTitle();
                    String description = lab.getDescription();
                    String dueDate = lab.getDueDate();
                    String filePath = lab.getFilePath();

                    View card = inflater.inflate(R.layout.item_student_lab_card, containerLecturerLabs, false);

                    TextView txtTitle = card.findViewById(R.id.txtStudentLabTitle);
                    TextView txtDesc = card.findViewById(R.id.txtStudentLabDescription);
                    TextView txtDueDate = card.findViewById(R.id.txtStudentLabDueDate);
                    Button btnViewAttachment = card.findViewById(R.id.btnViewAttachment);
                    Button btnSubmitWork = card.findViewById(R.id.btnSubmitWork);

                    if (txtTitle != null) txtTitle.setText(title);
                    if (txtDesc != null) txtDesc.setText(description);
                    if (txtDueDate != null) txtDueDate.setText("Due Date: " + dueDate);

                    if (btnViewAttachment != null) {
                        btnViewAttachment.setOnClickListener(v -> {
                            if (filePath != null && !filePath.trim().isEmpty()) {
                                openOrDownloadFile(filePath);
                            } else {
                                Toast.makeText(StudentLabActivity.this, "No file attached by lecturer", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                    if (btnSubmitWork != null) {
                        btnSubmitWork.setOnClickListener(v -> {
                            selectedLabIdForSubmission = labId;
                            submissionFilePicker.launch("*/*");
                        });
                    }

                    containerLecturerLabs.addView(card);
                }
            });
        }).start();
    }

    private String saveSubmissionToInternalStorage(Uri sourceUri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(sourceUri);
            String extension = ".pdf";
            String mimeType = getContentResolver().getType(sourceUri);
            if (mimeType != null && mimeType.contains("word")) {
                extension = ".docx";
            } else if (mimeType != null && mimeType.contains("zip")) {
                extension = ".zip";
            }

            String fileName = "sub_" + System.currentTimeMillis() + extension;
            File internalFile = new File(getFilesDir(), fileName);
            OutputStream outputStream = new FileOutputStream(internalFile);

            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }

            outputStream.close();
            inputStream.close();

            return internalFile.getAbsolutePath();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void openOrDownloadFile(String filePathOrUri) {
        try {
            File file = new File(filePathOrUri);
            Uri fileUri;

            if (file.exists()) {
                fileUri = FileProvider.getUriForFile(
                        this,
                        getPackageName() + ".provider",
                        file
                );
            } else {
                fileUri = Uri.parse(filePathOrUri);
            }

            String mimeType = getContentResolver().getType(fileUri);
            if (mimeType == null) {
                mimeType = "*/*";
            }

            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(fileUri, mimeType);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            startActivity(Intent.createChooser(intent, "Open Lab Material With"));

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "No application found to open this file.", Toast.LENGTH_SHORT).show();
        }
    }
}