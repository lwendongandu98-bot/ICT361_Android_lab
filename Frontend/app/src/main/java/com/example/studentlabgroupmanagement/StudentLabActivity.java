package com.example.studentlabgroupmanagement;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
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

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class StudentLabActivity extends AppCompatActivity {

    private LinearLayout containerLecturerLabs;
    private TextView tvNoLecturerLabs;
    private DatabaseHelper dbHelper;
    private int selectedLabIdForSubmission = -1;

    // Launcher for selecting a student submission file
    private final ActivityResultLauncher<String> submissionFilePicker =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null && selectedLabIdForSubmission != -1) {
                    // Ask confirmation before saving and submitting
                    confirmAndSubmitFile(uri);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_lab);

        dbHelper = new DatabaseHelper(this);

        // Bind layout components matching activity_student_lab.xml
        containerLecturerLabs = findViewById(R.id.containerLecturerLabs);
        tvNoLecturerLabs = findViewById(R.id.tvNoLecturerLabs);

        loadLecturerPublishedLabs();
    }

    /**
     * Prompts the student with an AlertDialog to confirm submission before saving.
     */
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

    /**
     * Saves the picked file into internal storage and inserts the submission record into SQLite.
     */
    private void processSubmission(Uri uri) {
        String savedPath = saveSubmissionToInternalStorage(uri);

        if (savedPath != null) {
            String submittedAt = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(new Date());

            // Retrieve logged-in student number
            String studentNumber = "STUDENT123";

            boolean success = dbHelper.saveSubmission(
                    selectedLabIdForSubmission,
                    studentNumber,
                    savedPath, // Store local path in SQLite
                    submittedAt
            );

            if (success) {
                Toast.makeText(this, "Lab Work Submitted Successfully!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Submission Failed", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Failed to process selected file", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadLecturerPublishedLabs() {
        containerLecturerLabs.removeAllViews();
        Cursor cursor = dbHelper.getPublishedLabs();

        if (cursor == null || cursor.getCount() == 0) {
            tvNoLecturerLabs.setVisibility(View.VISIBLE);
            if (cursor != null) cursor.close();
            return;
        }

        tvNoLecturerLabs.setVisibility(View.GONE);
        LayoutInflater inflater = LayoutInflater.from(this);

        while (cursor.moveToNext()) {
            int labId = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
            String title = cursor.getString(cursor.getColumnIndexOrThrow("lab_title"));
            String description = cursor.getString(cursor.getColumnIndexOrThrow("lab_description"));
            String dueDate = cursor.getString(cursor.getColumnIndexOrThrow("due_date"));

            String filePath = "";
            int filePathIndex = cursor.getColumnIndex("file_path");
            if (filePathIndex != -1) {
                filePath = cursor.getString(filePathIndex);
            }

            // Inflate individual card item
            View card = inflater.inflate(R.layout.item_student_lab_card, containerLecturerLabs, false);

            TextView txtTitle = card.findViewById(R.id.txtStudentLabTitle);
            TextView txtDesc = card.findViewById(R.id.txtStudentLabDescription);
            TextView txtDueDate = card.findViewById(R.id.txtStudentLabDueDate);
            Button btnViewAttachment = card.findViewById(R.id.btnViewAttachment);
            Button btnSubmitWork = card.findViewById(R.id.btnSubmitWork);

            txtTitle.setText(title);
            txtDesc.setText(description);
            txtDueDate.setText("Due Date: " + dueDate);

            final String finalFilePath = filePath;

            // Open/View Lecturer Attachment
            btnViewAttachment.setOnClickListener(v -> {
                if (finalFilePath != null && !finalFilePath.trim().isEmpty()) {
                    openOrDownloadFile(finalFilePath);
                } else {
                    Toast.makeText(StudentLabActivity.this, "No file attached by lecturer", Toast.LENGTH_SHORT).show();
                }
            });

            // Submit Student Work
            btnSubmitWork.setOnClickListener(v -> {
                selectedLabIdForSubmission = labId;
                submissionFilePicker.launch("*/*");
            });

            containerLecturerLabs.addView(card);
        }

        cursor.close();
    }

    /**
     * Copies student submission into internal application storage
     * so it persists and can be accessed across activities by the lecturer.
     */
    private String saveSubmissionToInternalStorage(Uri sourceUri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(sourceUri);

            // Determine file extension
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

            // Use FileProvider for local files saved in app storage
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