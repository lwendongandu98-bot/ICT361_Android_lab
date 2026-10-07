package com.example.studentlabgroupmanagement;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class LabsActivity extends AppCompatActivity {

    private EditText etLabTitle;
    private EditText etLabDescription;
    private EditText etDueDate;

    private Button btnChooseFile;
    private Button btnSaveDraft;
    private Button btnPublishLab;

    private LinearLayout labsContainer;

    private DatabaseHelper dbHelper;

    private String selectedFilePath = "";
    private int editingLabId = -1;

    // File picker launcher setup
    private final ActivityResultLauncher<String> filePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    // Copy file to internal app storage and retrieve local path
                    String savedPath = saveFileToInternalStorage(uri);

                    if (savedPath != null) {
                        selectedFilePath = savedPath;
                        String fileName = getFileNameFromUri(uri);
                        btnChooseFile.setText("Selected: " + fileName);
                        Toast.makeText(this, "File Attached Successfully", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Failed to Process File", Toast.LENGTH_SHORT).show();
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_labs);

        dbHelper = new DatabaseHelper(this);

        etLabTitle = findViewById(R.id.etLabTitle);
        etLabDescription = findViewById(R.id.etLabDescription);
        etDueDate = findViewById(R.id.etDueDate);

        btnChooseFile = findViewById(R.id.btnChooseFile);
        btnSaveDraft = findViewById(R.id.btnSaveDraft);
        btnPublishLab = findViewById(R.id.btnPublishLab);

        labsContainer = findViewById(R.id.labsContainer);

        // Open system document picker for PDF, DOCX, ZIP, or all file types (*/*)
        btnChooseFile.setOnClickListener(v -> filePickerLauncher.launch("*/*"));

        btnSaveDraft.setOnClickListener(v -> saveOrUpdateLab("DRAFT"));
        btnPublishLab.setOnClickListener(v -> saveOrUpdateLab("PUBLISHED"));

        loadLabs();
    }

    private void saveOrUpdateLab(String status) {
        String title = etLabTitle.getText().toString().trim();
        String description = etLabDescription.getText().toString().trim();
        String dueDate = etDueDate.getText().toString().trim();

        if (title.isEmpty()) {
            Toast.makeText(this, "Please enter a lab title", Toast.LENGTH_SHORT).show();
            return;
        }

        if (description.isEmpty()) {
            Toast.makeText(this, "Please enter a lab description", Toast.LENGTH_SHORT).show();
            return;
        }

        if (dueDate.isEmpty()) {
            Toast.makeText(this, "Please enter a due date", Toast.LENGTH_SHORT).show();
            return;
        }

        if (editingLabId != -1) {
            boolean success = dbHelper.updateLab(editingLabId, title, description, dueDate, selectedFilePath);
            if (success) {
                if ("PUBLISHED".equals(status)) {
                    dbHelper.publishLab(editingLabId);
                }
                Toast.makeText(this, "Lab Updated Successfully", Toast.LENGTH_SHORT).show();
                clearFields();
                loadLabs();
            } else {
                Toast.makeText(this, "Failed To Update Lab", Toast.LENGTH_SHORT).show();
            }
        } else {
            boolean success = dbHelper.saveLab(title, description, dueDate, selectedFilePath, status);
            if (success) {
                Toast.makeText(
                        this,
                        status.equals("DRAFT") ? "Lab Saved As Draft" : "Lab Published Successfully",
                        Toast.LENGTH_SHORT
                ).show();
                clearFields();
                loadLabs();
            } else {
                Toast.makeText(this, "Failed To Save Lab", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void loadLabs() {
        labsContainer.removeAllViews();

        Cursor cursor = dbHelper.getAllLabs();
        if (cursor == null) {
            return;
        }

        LayoutInflater inflater = LayoutInflater.from(this);

        while (cursor.moveToNext()) {
            int labId = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
            String title = cursor.getString(cursor.getColumnIndexOrThrow("lab_title"));
            String description = cursor.getString(cursor.getColumnIndexOrThrow("lab_description"));
            String dueDate = cursor.getString(cursor.getColumnIndexOrThrow("due_date"));
            String status = cursor.getString(cursor.getColumnIndexOrThrow("status"));

            String filePath = "";
            int filePathIndex = cursor.getColumnIndex("file_path");
            if (filePathIndex != -1) {
                filePath = cursor.getString(filePathIndex);
            }

            View card = inflater.inflate(R.layout.item_lab_card, labsContainer, false);

            TextView txtLabTitle = card.findViewById(R.id.txtLabTitle);
            TextView txtLabDescription = card.findViewById(R.id.txtLabDescription);
            TextView txtLabDueDate = card.findViewById(R.id.txtLabDueDate);
            TextView txtLabStatus = card.findViewById(R.id.txtLabStatus);

            Button btnEdit = card.findViewById(R.id.btnEdit);
            Button btnPublish = card.findViewById(R.id.btnPublish);
            Button btnDelete = card.findViewById(R.id.btnDeleteLab);

            txtLabTitle.setText(title);
            txtLabDescription.setText(description);
            txtLabDueDate.setText("Due Date: " + dueDate);
            txtLabStatus.setText("Status: " + status);

            if (btnEdit != null) {
                final String finalFilePath = filePath;
                btnEdit.setOnClickListener(v -> {
                    editingLabId = labId;
                    etLabTitle.setText(title);
                    etLabDescription.setText(description);
                    etDueDate.setText(dueDate);
                    selectedFilePath = finalFilePath != null ? finalFilePath : "";

                    if (!selectedFilePath.isEmpty()) {
                        btnChooseFile.setText("File Attached");
                    } else {
                        btnChooseFile.setText("Choose Lab File");
                    }

                    btnSaveDraft.setText("Update Draft");
                    btnPublishLab.setText("Update & Publish");
                    Toast.makeText(LabsActivity.this, "Editing: " + title, Toast.LENGTH_SHORT).show();
                });
            }

            if ("PUBLISHED".equals(status)) {
                if (btnPublish != null) {
                    btnPublish.setVisibility(View.GONE);
                }
            } else {
                if (btnPublish != null) {
                    btnPublish.setVisibility(View.VISIBLE);
                    btnPublish.setOnClickListener(v -> {
                        if (dbHelper.publishLab(labId)) {
                            Toast.makeText(LabsActivity.this, "Lab Published", Toast.LENGTH_SHORT).show();
                            loadLabs();
                        }
                    });
                }
            }

            if (btnDelete != null) {
                btnDelete.setOnClickListener(v -> {
                    if (dbHelper.deleteLab(labId)) {
                        Toast.makeText(LabsActivity.this, "Lab Deleted", Toast.LENGTH_SHORT).show();
                        if (editingLabId == labId) {
                            clearFields();
                        }
                        loadLabs();
                    }
                });
            }

            labsContainer.addView(card);
        }

        cursor.close();
    }

    /**
     * Copies the picked URI file into internal application storage
     * so it remains permanently accessible across activities via FileProvider.
     */
    private String saveFileToInternalStorage(Uri sourceUri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(sourceUri);

            // Extract original extension or fallback to pdf
            String extension = ".pdf";
            String mimeType = getContentResolver().getType(sourceUri);
            if (mimeType != null && mimeType.contains("word")) {
                extension = ".docx";
            } else if (mimeType != null && mimeType.contains("zip")) {
                extension = ".zip";
            }

            String fileName = "lab_" + System.currentTimeMillis() + extension;
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

    private String getFileNameFromUri(Uri uri) {
        String path = uri.getPath();
        if (path != null && path.contains("/")) {
            return path.substring(path.lastIndexOf('/') + 1);
        }
        return "File Attached";
    }

    private void clearFields() {
        etLabTitle.setText("");
        etLabDescription.setText("");
        etDueDate.setText("");

        selectedFilePath = "";
        editingLabId = -1;

        btnChooseFile.setText("Choose Lab File");
        btnSaveDraft.setText("Save Draft");
        btnPublishLab.setText("Publish Now");
    }
}