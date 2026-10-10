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

public class CheckedLabsActivity extends AppCompatActivity {

    private LinearLayout containerCheckedLabs;
    private TextView tvNoCheckedLabs;
    private Button btnBackToPending;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checked_labs);

        dbHelper = new DatabaseHelper(this);

        // Bind layout views directly to exact XML resource IDs
        containerCheckedLabs = findViewById(R.id.containerCheckedLabs);
        tvNoCheckedLabs = findViewById(R.id.tvNoCheckedLabs);

        // Back Button Listener
        btnBackToPending = findViewById(R.id.btnBackToPending);
        if (btnBackToPending != null) {
            btnBackToPending.setOnClickListener(v -> finish());
        }

        loadCheckedLabs();
    }

    private void loadCheckedLabs() {
        containerCheckedLabs.removeAllViews();
        Cursor cursor = dbHelper.getCheckedSubmissions();

        if (cursor == null || cursor.getCount() == 0) {
            if (tvNoCheckedLabs != null) tvNoCheckedLabs.setVisibility(View.VISIBLE);
            if (cursor != null) cursor.close();
            return;
        }

        if (tvNoCheckedLabs != null) tvNoCheckedLabs.setVisibility(View.GONE);
        LayoutInflater inflater = LayoutInflater.from(this);

        String currentGroupDate = "";

        while (cursor.moveToNext()) {
            String studentNumber = cursor.getString(cursor.getColumnIndexOrThrow("student_number"));
            String submittedAt = cursor.getString(cursor.getColumnIndexOrThrow("submitted_at"));

            // Retrieve checked_at timestamp
            String checkedAt = "";
            int checkedAtIdx = cursor.getColumnIndex("checked_at");
            if (checkedAtIdx != -1 && !cursor.isNull(checkedAtIdx)) {
                checkedAt = cursor.getString(checkedAtIdx);
            }

            // Extract date portion (YYYY-MM-DD) for grouping headers
            String dateOnly = "Checked Submissions";
            if (!checkedAt.isEmpty()) {
                dateOnly = checkedAt.contains(" ") ? checkedAt.split(" ")[0] : checkedAt;
            } else if (submittedAt != null && !submittedAt.isEmpty()) {
                dateOnly = submittedAt.contains(" ") ? submittedAt.split(" ")[0] : submittedAt;
            }

            // Insert dynamic date header when move date changes
            if (!dateOnly.equals(currentGroupDate)) {
                currentGroupDate = dateOnly;
                View headerView = inflater.inflate(R.layout.item_date_header, containerCheckedLabs, false);
                TextView txtDateHeader = headerView.findViewById(R.id.txtDateHeader);
                if (txtDateHeader != null) {
                    txtDateHeader.setText("Moved on: " + currentGroupDate);
                }
                containerCheckedLabs.addView(headerView);
            }

            String filePath = "";
            int pathIndex = cursor.getColumnIndex("file_path");
            if (pathIndex != -1) {
                filePath = cursor.getString(pathIndex);
            }

            View card = inflater.inflate(R.layout.item_submission_card, containerCheckedLabs, false);

            TextView txtStudentNumber = card.findViewById(R.id.txtStudentNumber);
            TextView txtSubmittedAt = card.findViewById(R.id.txtSubmittedAt);
            Button btnViewSubmission = card.findViewById(R.id.btnViewSubmission);
            Button btnDownloadSubmission = card.findViewById(R.id.btnDownloadSubmission);
            Button btnMoveToChecked = card.findViewById(R.id.btnMoveToChecked);

            if (btnMoveToChecked != null) {
                btnMoveToChecked.setVisibility(View.GONE);
            }

            txtStudentNumber.setText("Student: " + studentNumber);
            txtSubmittedAt.setText("Submitted: " + submittedAt + (!checkedAt.isEmpty() ? " (Checked)" : ""));

            final String finalFilePath = filePath;

            if (btnViewSubmission != null) {
                btnViewSubmission.setOnClickListener(v -> {
                    if (finalFilePath != null && !finalFilePath.trim().isEmpty()) {
                        openSubmissionFile(finalFilePath);
                    } else {
                        Toast.makeText(CheckedLabsActivity.this, "No file attached", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            if (btnDownloadSubmission != null) {
                btnDownloadSubmission.setOnClickListener(v -> {
                    if (finalFilePath != null && !finalFilePath.trim().isEmpty()) {
                        downloadFileToStorage(finalFilePath);
                    } else {
                        Toast.makeText(CheckedLabsActivity.this, "No file to download", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            containerCheckedLabs.addView(card);
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