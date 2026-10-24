package com.example.studentlabgroupmanagement;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.studentlabgroupmanagement.data.local.AppDatabase;
import com.example.studentlabgroupmanagement.data.local.entity.LabGroupEntity;
import com.example.studentlabgroupmanagement.data.remote.RetrofitClient;
import com.example.studentlabgroupmanagement.data.repository.GroupRepository;
import com.example.studentlabgroupmanagement.ui.viewmodel.GroupsViewModel;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GroupsActivity extends AppCompatActivity {

    private EditText etGroupName, etSearchGroups;
    private Button btnCreateGroup;
    private LinearLayout groupsContainer;
    private Toolbar toolbarGroups;
    private String filterQuery = "";
    private GroupsViewModel groupsViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_groups);

        // Initialize ViewModel for API & Database operations
        groupsViewModel = new ViewModelProvider(this).get(GroupsViewModel.class);

        // Bind layout views
        etGroupName = findViewById(R.id.etGroupName);
        etSearchGroups = findViewById(R.id.etSearchGroups);
        btnCreateGroup = findViewById(R.id.btnCreateGroup);
        groupsContainer = findViewById(R.id.groupsContainer);
        toolbarGroups = findViewById(R.id.toolbarGroups);

        // --- DYNAMICALLY PUSH TOOLBAR BELOW STATUS BAR ---
        if (toolbarGroups != null) {
            ViewCompat.setOnApplyWindowInsetsListener(toolbarGroups, (v, windowInsets) -> {
                int statusBarHeight = windowInsets.getInsets(WindowInsetsCompat.Type.statusBars()).top;

                // Set top padding to the height of the status bar so content doesn't clip,
                // while the blue background color still stretches to the absolute top edge!
                v.setPadding(v.getPaddingLeft(), statusBarHeight, v.getPaddingRight(), v.getPaddingBottom());

                return windowInsets;
            });
        }
        // ------------------------------------------------

        btnCreateGroup.setOnClickListener(v -> {
            String groupName = etGroupName.getText().toString().trim();
            if (groupName.isEmpty()) {
                Toast.makeText(this, "Please type a group name!", Toast.LENGTH_SHORT).show();
                return;
            }
            createNewLabGroup(groupName);
        });

        etSearchGroups.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterQuery = s.toString().trim();
                loadGroupsFromDatabase();
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Fetch latest server groups and sync into local Room on startup
        syncAndLoadGroups();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Also sync whenever the activity comes back into focus
        syncAndLoadGroups();
    }

    private void syncAndLoadGroups() {
        // 1. Silently pull latest groups from the Node.js backend server
        RetrofitClient.getApiService().getAllGroups().enqueue(new Callback<List<LabGroupEntity>>() {
            @Override
            public void onResponse(Call<List<LabGroupEntity>> call, Response<List<LabGroupEntity>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<LabGroupEntity> serverGroups = response.body();

                    // 2. Save fetched groups into local Room database in the background for offline access
                    new Thread(() -> {
                        AppDatabase db = AppDatabase.getInstance(getApplicationContext());
                        for (LabGroupEntity group : serverGroups) {
                            try {
                                db.labGroupDao().insertGroup(group);
                            } catch (Exception e) {
                                Log.w("GroupsSync", "Group already exists or update skipped: " + group.getGroupName());
                            }
                        }
                        // 3. Refresh UI from local database once synced
                        loadGroupsFromDatabase();
                    }).start();
                } else {
                    // Fallback to local database if server response code isn't successful
                    loadGroupsFromDatabase();
                }
            }

            @Override
            public void onFailure(Call<List<LabGroupEntity>> call, Throwable t) {
                // Offline fallback: load whatever is already cached locally
                Log.w("GroupsSync", "Offline mode: Sync skipped, loading local cache. Error: " + t.getMessage());
                loadGroupsFromDatabase();
            }
        });
    }

    private void createNewLabGroup(String name) {
        // Route group creation through ViewModel & Repository for MySQL synchronization
        groupsViewModel.createGroup(name, 15, new GroupRepository.OnOperationCallback() {
            @Override
            public void onSuccess(String message) {
                runOnUiThread(() -> {
                    Toast.makeText(GroupsActivity.this, message, Toast.LENGTH_SHORT).show();
                    etGroupName.setText("");
                    syncAndLoadGroups(); // Sync again to reflect newly created group instantly
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    Toast.makeText(GroupsActivity.this, "Error: " + error, Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private void loadGroupsFromDatabase() {
        new Thread(() -> {
            AppDatabase db = AppDatabase.getInstance(getApplicationContext());
            List<LabGroupEntity> groups = db.labGroupDao().searchGroups("%" + filterQuery + "%");

            if (groups == null) return;

            runOnUiThread(() -> {
                groupsContainer.removeAllViews();
                LayoutInflater inflater = LayoutInflater.from(GroupsActivity.this);

                for (LabGroupEntity group : groups) {
                    String name = group.getGroupName();

                    View cardView = inflater.inflate(R.layout.item_group_card, groupsContainer, false);
                    TextView txtTitle = cardView.findViewById(R.id.txtGroupNameDisplay);
                    TextView txtCount = cardView.findViewById(R.id.txtGroupCapacityCounter);
                    Button btnManage = cardView.findViewById(R.id.btnAssignStudentsInline);
                    Button btnDelete = cardView.findViewById(R.id.btnDeleteGroup);

                    txtTitle.setText(name);

                    new Thread(() -> {
                        int count = db.studentDao().getStudentCountByGroup(name);
                        runOnUiThread(() -> txtCount.setText("Occupancy: " + count + " / 15 Members Max"));
                    }).start();

                    btnManage.setOnClickListener(v -> {
                        Intent intent = new Intent(GroupsActivity.this, AssignStudentsActivity.class);
                        intent.putExtra("TARGET_GROUP_NAME", name);
                        startActivity(intent);
                    });

                    btnDelete.setOnClickListener(v -> {
                        new AlertDialog.Builder(GroupsActivity.this)
                                .setTitle("Delete Group")
                                .setMessage("Are you sure you want to delete \"" + name + "\"?")
                                .setPositiveButton("Delete", (dialog, which) -> {
                                    new Thread(() -> {
                                        db.labGroupDao().deleteGroupByName(name);
                                        runOnUiThread(() -> {
                                            Toast.makeText(GroupsActivity.this, "Group deleted successfully", Toast.LENGTH_SHORT).show();
                                            syncAndLoadGroups();
                                        });
                                    }).start();
                                })
                                .setNegativeButton("Cancel", null)
                                .show();
                    });

                    groupsContainer.addView(cardView);
                }
            });
        }).start();
    }
}