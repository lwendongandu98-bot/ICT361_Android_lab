package com.example.studentlabgroupmanagement;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.work.Constraints;
import androidx.work.ExistingWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.example.studentlabgroupmanagement.data.local.AppDatabase;
import com.example.studentlabgroupmanagement.data.local.entity.SubmissionEntity;
import com.example.studentlabgroupmanagement.data.worker.DataSyncWorker;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

public class SubmissionsActivity extends AppCompatActivity {

    private LinearLayout containerSubmissions;
    private TextView tvNoSubmissions;
    private Button btnOpenCheckedLabs;
    private Toolbar toolbarSubmissions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_submissions);

        containerSubmissions = findViewById(R.id.containerSubmissions);
        tvNoSubmissions = findViewById(R.id.tvNoSubmissions);
        btnOpenCheckedLabs = findViewById(R.id.btnOpenCheckedLabs);
        toolbarSubmissions = findViewById(R.id.toolbarSubmissions);

        // --- DYNAMICALLY PUSH TOOLBAR BELOW STATUS BAR ---
        if (toolbarSubmissions != null) {
            ViewCompat.setOnApplyWindowInsetsListener(toolbarSubmissions, (v, windowInsets) -> {
                int statusBarHeight = windowInsets.getInsets(WindowInsetsCompat.Type.statusBars()).top;

                // Set top padding to the height of the status bar so content doesn't clip,
                // while the blue background color still stretches to the absolute top edge!
                v.setPadding(v.getPaddingLeft(), statusBarHeight, v.getPaddingRight(), v.getPaddingBottom());

                return windowInsets;
            });
        }
        // ------------------------------------------------

        // --- CHECK SERVER CONNECTION ON STARTUP ---
        NetworkCheckHelper.checkConnection(this);
        // ------------------------------------------

        if (btnOpenCheckedLabs != null) {
            btnOpenCheckedLabs.setOnClickListener(v -> {
                Intent intent = new Intent(SubmissionsActivity.this, CheckedLabsActivity.class);
                startActivity(intent);
            });
        }

        loadSubmissions();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadSubmissions();
    }

    private void loadSubmissions() {
        new Thread(() -> {
            List<SubmissionEntity> submissions = AppDatabase.getInstance(getApplicationContext())
                    .submissionDao()
                    .getPendingSubmissions();

            runOnUiThread(() -> {
                containerSubmissions.removeAllViews();
                if (submissions == null || submissions.isEmpty()) {
                    if (tvNoSubmissions != null) tvNoSubmissions.setVisibility(View.VISIBLE);
                    return;
                }

                if (tvNoSubmissions != null) tvNoSubmissions.setVisibility(View.GONE);
                LayoutInflater inflater = LayoutInflater.from(this);

                for (SubmissionEntity sub : submissions) {
                    int submissionId = sub.getId();
                    String studentNumber = sub.getStudentNumber();
                    String submittedAt = sub.getSubmittedAt();
                    String filePath = sub.getFilePath();

                    View card = inflater.inflate(R.layout.item_submission_card, containerSubmissions, false);

                    TextView txtStudentNumber = card.findViewById(R.id.txtStudentNumber);
                    TextView txtSubmittedAt = card.findViewById(R.id.txtSubmittedAt);
                    Button btnViewSubmission = card.findViewById(R.id.btnViewSubmission);
                    Button btnDownloadSubmission = card.findViewById(R.id.btnDownloadSubmission);
                    Button btnMoveToChecked = card.findViewById(R.id.btnMoveToChecked);

                    txtStudentNumber.setText("Student: " + studentNumber);
                    txtSubmittedAt.setText("Submitted: " + submittedAt);

                    if (btnViewSubmission != null) {
                        btnViewSubmission.setOnClickListener(v -> {
                            if (filePath != null && !filePath.trim().isEmpty()) {
                                openSubmissionFile(filePath);
                            } else {
                                Toast.makeText(SubmissionsActivity.this, "No file attached", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                    if (btnDownloadSubmission != null) {
                        btnDownloadSubmission.setOnClickListener(v -> {
                            if (filePath != null && !filePath.trim().isEmpty()) {
                                downloadFileToStorage(filePath);
                            } else {
                                Toast.makeText(SubmissionsActivity.this, "No file to download", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                    if (btnMoveToChecked != null) {
                        btnMoveToChecked.setOnClickListener(v -> {
                            new Thread(() -> {
                                AppDatabase.getInstance(getApplicationContext())
                                        .submissionDao()
                                        .markSubmissionAsChecked(submissionId);

                                // --- TRIGGER WORKMANAGER BACKGROUND SYNC ---
                                Constraints constraints = new Constraints.Builder()
                                        .setRequiredNetworkType(NetworkType.CONNECTED)
                                        .build();

                                OneTimeWorkRequest syncRequest = new OneTimeWorkRequest.Builder(DataSyncWorker.class)
                                        .setConstraints(constraints)
                                        .build();

                                WorkManager.getInstance(getApplicationContext()).enqueueUniqueWork(
                                        "FullAppSyncWork",
                                        ExistingWorkPolicy.KEEP,
                                        syncRequest
                                );
                                // ------------------------------------------

                                runOnUiThread(() -> {
                                    Toast.makeText(SubmissionsActivity.this, "Submission marked as checked locally! Will sync when online.", Toast.LENGTH_SHORT).show();
                                    loadSubmissions();
                                });
                            }).start();
                        });
                    }

                    containerSubmissions.addView(card);
                }
            });
        }).start();
    }

    private void openSubmissionFile(String filePathOrUri) {
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
            if (mimeType == null) mimeType = "*/*";

            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(fileUri, mimeType);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            startActivity(Intent.createChooser(intent, "Open Student Submission With"));

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "No application found to open this file.", Toast.LENGTH_SHORT).show();
        }
    }

    private void downloadFileToStorage(String sourceFilePath) {
        try {
            File srcFile = new File(sourceFilePath);
            if (!srcFile.exists()) {
                Toast.makeText(this, "Source file not found on device", Toast.LENGTH_SHORT).show();
                return;
            }

            File downloadsFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            if (!downloadsFolder.exists()) downloadsFolder.mkdirs();

            File destFile = new File(downloadsFolder, "Downloaded_" + srcFile.getName());

            InputStream in = new FileInputStream(srcFile);
            OutputStream out = new FileOutputStream(destFile);

            byte[] buffer = new byte[1024];
            int length;
            while ((length = in.read(buffer)) > 0) {
                out.write(buffer, 0, length);
            }

            in.close();
            out.close();

            Toast.makeText(this, "Saved to Downloads: " + destFile.getName(), Toast.LENGTH_LONG).show();

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to download file: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}