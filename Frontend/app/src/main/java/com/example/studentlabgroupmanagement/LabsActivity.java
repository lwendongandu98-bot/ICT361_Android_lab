package com.example.studentlabgroupmanagement;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
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
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.work.Constraints;
import androidx.work.ExistingWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.example.studentlabgroupmanagement.data.local.AppDatabase;
import com.example.studentlabgroupmanagement.data.local.entity.LabEntity;
import com.example.studentlabgroupmanagement.data.remote.RetrofitClient;
import com.example.studentlabgroupmanagement.data.worker.DataSyncWorker;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LabsActivity extends AppCompatActivity {

    private EditText etLabTitle;
    private EditText etLabDescription;
    private EditText etDueDate;

    private Button btnChooseFile;
    private Button btnSaveDraft;
    private Button btnPublishLab;

    private LinearLayout labsContainer;
    private Toolbar toolbarLabs;

    private String selectedFilePath = "";
    private int editingLabId = -1;

    private final ActivityResultLauncher<String> filePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
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

        etLabTitle = findViewById(R.id.etLabTitle);
        etLabDescription = findViewById(R.id.etLabDescription);
        etDueDate = findViewById(R.id.etDueDate);

        btnChooseFile = findViewById(R.id.btnChooseFile);
        btnSaveDraft = findViewById(R.id.btnSaveDraft);
        btnPublishLab = findViewById(R.id.btnPublishLab);

        labsContainer = findViewById(R.id.labsContainer);
        toolbarLabs = findViewById(R.id.toolbarLabs);

        // --- DYNAMICALLY PUSH TOOLBAR BELOW STATUS BAR ---
        if (toolbarLabs != null) {
            ViewCompat.setOnApplyWindowInsetsListener(toolbarLabs, (v, windowInsets) -> {
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

        btnChooseFile.setOnClickListener(v -> filePickerLauncher.launch("*/*"));

        btnSaveDraft.setOnClickListener(v -> saveOrUpdateLab("DRAFT"));
        btnPublishLab.setOnClickListener(v -> saveOrUpdateLab("PUBLISHED"));

        syncAndLoadLabs();
    }

    @Override
    protected void onResume() {
        super.onResume();
        syncAndLoadLabs();
    }

    private void syncAndLoadLabs() {
        // 1. Silently pull latest labs from the Node.js backend server
        RetrofitClient.getApiService().getAllLabs().enqueue(new Callback<List<LabEntity>>() {
            @Override
            public void onResponse(Call<List<LabEntity>> call, Response<List<LabEntity>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<LabEntity> serverLabs = response.body();

                    // 2. Save fetched labs into local Room database for offline access
                    new Thread(() -> {
                        AppDatabase db = AppDatabase.getInstance(getApplicationContext());
                        for (LabEntity lab : serverLabs) {
                            try {
                                db.labDao().insertLab(lab);
                            } catch (Exception e) {
                                Log.w("LabSync", "Lab already exists or update skipped: " + lab.getTitle());
                            }
                        }
                        // 3. Refresh UI from local database
                        loadLabs();
                    }).start();
                } else {
                    loadLabs();
                }
            }

            @Override
            public void onFailure(Call<List<LabEntity>> call, Throwable t) {
                Log.w("LabSync", "Offline mode: Sync skipped, loading local cache. Error: " + t.getMessage());
                loadLabs();
            }
        });
    }

    private void triggerSyncWorker() {
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
    }

    private void saveOrUpdateLab(String status) {
        String title = etLabTitle.getText().toString().trim();
        String description = etLabDescription.getText().toString().trim();
        String dueDate = etDueDate.getText().toString().trim();

        if (title.isEmpty() || description.isEmpty() || dueDate.isEmpty()) {
            Toast.makeText(this, "Please enter all lab fields", Toast.LENGTH_SHORT).show();
            return;
        }

        new Thread(() -> {
            AppDatabase db = AppDatabase.getInstance(getApplicationContext());
            if (editingLabId != -1) {
                LabEntity lab = db.labDao().getLabById(editingLabId);
                if (lab != null) {
                    lab.setTitle(title);
                    lab.setDescription(description);
                    lab.setDueDate(dueDate);
                    lab.setFilePath(selectedFilePath);
                    if ("PUBLISHED".equals(status)) {
                        lab.setStatus("PUBLISHED");
                    }
                    db.labDao().updateLab(lab);

                    triggerSyncWorker();

                    runOnUiThread(() -> {
                        Toast.makeText(this, "Lab saved locally! Will sync when online.", Toast.LENGTH_SHORT).show();
                        clearFields();
                        syncAndLoadLabs();
                    });
                }
            } else {
                LabEntity newLab = new LabEntity(title, description, dueDate, selectedFilePath, status);
                db.labDao().insertLab(newLab);

                triggerSyncWorker();

                runOnUiThread(() -> {
                    Toast.makeText(this, "Lab saved locally! Will sync when online.", Toast.LENGTH_SHORT).show();
                    clearFields();
                    syncAndLoadLabs();
                });
            }
        }).start();
    }

    private void loadLabs() {
        new Thread(() -> {
            List<LabEntity> labs = AppDatabase.getInstance(getApplicationContext()).labDao().getAllLabsSync();

            runOnUiThread(() -> {
                labsContainer.removeAllViews();
                if (labs == null) return;

                LayoutInflater inflater = LayoutInflater.from(this);

                for (LabEntity lab : labs) {
                    int labId = lab.getId();
                    String title = lab.getTitle();
                    String description = lab.getDescription();
                    String dueDate = lab.getDueDate();
                    String status = lab.getStatus();
                    String filePath = lab.getFilePath();

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
                        btnEdit.setOnClickListener(v -> {
                            editingLabId = labId;
                            etLabTitle.setText(title);
                            etLabDescription.setText(description);
                            etDueDate.setText(dueDate);
                            selectedFilePath = filePath != null ? filePath : "";

                            btnChooseFile.setText(!selectedFilePath.isEmpty() ? "File Attached" : "Choose Lab File");
                            btnSaveDraft.setText("Update Draft");
                            btnPublishLab.setText("Update & Publish");
                            Toast.makeText(LabsActivity.this, "Editing: " + title, Toast.LENGTH_SHORT).show();
                        });
                    }

                    if ("PUBLISHED".equals(status)) {
                        if (btnPublish != null) btnPublish.setVisibility(View.GONE);
                    } else {
                        if (btnPublish != null) {
                            btnPublish.setVisibility(View.VISIBLE);
                            btnPublish.setOnClickListener(v -> {
                                new Thread(() -> {
                                    AppDatabase.getInstance(getApplicationContext()).labDao().updateLabStatus(labId, "PUBLISHED");
                                    triggerSyncWorker();
                                    runOnUiThread(() -> {
                                        Toast.makeText(LabsActivity.this, "Lab published locally! Will sync when online.", Toast.LENGTH_SHORT).show();
                                        syncAndLoadLabs();
                                    });
                                }).start();
                            });
                        }
                    }

                    if (btnDelete != null) {
                        btnDelete.setOnClickListener(v -> {
                            new Thread(() -> {
                                AppDatabase.getInstance(getApplicationContext()).labDao().deleteLabById(labId);
                                triggerSyncWorker();
                                suppressSyncToastOrRefresh:
                                runOnUiThread(() -> {
                                    Toast.makeText(LabsActivity.this, "Lab deleted locally! Will sync when online.", Toast.LENGTH_SHORT).show();
                                    if (editingLabId == labId) clearFields();
                                    syncAndLoadLabs();
                                });
                            }).start();
                        });
                    }

                    labsContainer.addView(card);
                }
            });
        }).start();
    }

    private String saveFileToInternalStorage(Uri sourceUri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(sourceUri);
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