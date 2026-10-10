package com.example.studentlabgroupmanagement;

import android.content.Intent;
import android.database.Cursor;
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
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class SubmissionsActivity extends AppCompatActivity {

    private LinearLayout containerSubmissions;
    private TextView tvNoSubmissions;
    private Button btnOpenCheckedLabs;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_submissions);

        dbHelper = new DatabaseHelper(this);

        containerSubmissions = findViewById(R.id.containerSubmissions);
        tvNoSubmissions = findViewById(R.id.tvNoSubmissions);
        btnOpenCheckedLabs = findViewById(R.id.btnOpenCheckedLabs);

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
        containerSubmissions.removeAllViews();
        Cursor cursor = dbHelper.getPendingSubmissions();

        if (cursor == null || cursor.getCount() == 0) {
            if (tvNoSubmissions != null) tvNoSubmissions.setVisibility(View.VISIBLE);
            if (cursor != null) cursor.close();
            return;
        }

        if (tvNoSubmissions != null) tvNoSubmissions.setVisibility(View.GONE);
        LayoutInflater inflater = LayoutInflater.from(this);

        while (cursor.moveToNext()) {
            int submissionId = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
            String studentNumber = cursor.getString(cursor.getColumnIndexOrThrow("student_number"));
            String submittedAt = cursor.getString(cursor.getColumnIndexOrThrow("submitted_at"));

            String filePath = "";
            int pathIndex = cursor.getColumnIndex("file_path");
            if (pathIndex != -1) {
                filePath = cursor.getString(pathIndex);
            }

            View card = inflater.inflate(R.layout.item_submission_card, containerSubmissions, false);

            TextView txtStudentNumber = card.findViewById(R.id.txtStudentNumber);
            TextView txtSubmittedAt = card.findViewById(R.id.txtSubmittedAt);
            Button btnViewSubmission = card.findViewById(R.id.btnViewSubmission);
            Button btnDownloadSubmission = card.findViewById(R.id.btnDownloadSubmission);
            Button btnMoveToChecked = card.findViewById(R.id.btnMoveToChecked);

            txtStudentNumber.setText("Student: " + studentNumber);
            txtSubmittedAt.setText("Submitted: " + submittedAt);

            final String finalFilePath = filePath;

            // View File Option
            if (btnViewSubmission != null) {
                btnViewSubmission.setOnClickListener(v -> {
                    if (finalFilePath != null && !finalFilePath.trim().isEmpty()) {
                        openSubmissionFile(finalFilePath);
                    } else {
                        Toast.makeText(SubmissionsActivity.this, "No file attached", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            // Download File Option
            if (btnDownloadSubmission != null) {
                btnDownloadSubmission.setOnClickListener(v -> {
                    if (finalFilePath != null && !finalFilePath.trim().isEmpty()) {
                        downloadFileToStorage(finalFilePath);
                    } else {
                        Toast.makeText(SubmissionsActivity.this, "No file to download", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            // Move to Checked Option
            if (btnMoveToChecked != null) {
                btnMoveToChecked.setOnClickListener(v -> {
                    boolean success = dbHelper.markSubmissionAsChecked(submissionId);
                    if (success) {
                        Toast.makeText(SubmissionsActivity.this, "Submission marked as checked", Toast.LENGTH_SHORT).show();
                        loadSubmissions();
                    } else {
                        Toast.makeText(SubmissionsActivity.this, "Failed to update submission", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            containerSubmissions.addView(card);
        }

        cursor.close();
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
            if (mimeType == null) {
                mimeType = "*/*";
            }

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
            if (!downloadsFolder.exists()) {
                downloadsFolder.mkdirs();
            }

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