package com.example.studentlabgroupmanagement;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.studentlabgroupmanagement.data.local.AppDatabase;
import com.example.studentlabgroupmanagement.data.local.entity.StudentEntity;

import java.util.List;

public class AssignStudentsActivity extends AppCompatActivity {

    private TextView txtTargetGroupTitle, txtTargetGroupCount;
    private EditText etSearchAvailable;
    private Spinner spinnerProgramFilter;
    private Button btnEditGroupName;
    private LinearLayout containerAvailableStudents, containerGroupMembers;

    private String targetGroupName;

    private String searchString = "";
    private String selectedProgram = "All Programs";
    private int currentGroupSize = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_assign_students);

        targetGroupName = getIntent().getStringExtra("TARGET_GROUP_NAME");
        if (targetGroupName == null) targetGroupName = "Unassigned";

        txtTargetGroupTitle = findViewById(R.id.txtTargetGroupTitle);
        txtTargetGroupCount = findViewById(R.id.txtTargetGroupCount);
        etSearchAvailable = findViewById(R.id.etSearchAvailable);
        spinnerProgramFilter = findViewById(R.id.spinnerProgramFilter);
        btnEditGroupName = findViewById(R.id.btnEditGroupName);
        containerAvailableStudents = findViewById(R.id.containerAvailableStudents);
        containerGroupMembers = findViewById(R.id.containerGroupMembers);

        txtTargetGroupTitle.setText("Managing: " + targetGroupName);

        String[] programs = {"All Programs", "BSc Computer Science", "BSc Information Technology", "BIT"};
        ArrayAdapter<String> spinAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, programs);
        spinAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerProgramFilter.setAdapter(spinAdapter);

        btnEditGroupName.setOnClickListener(v -> showRenameGroupDialog());

        etSearchAvailable.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchString = s.toString().trim();
                renderAllLists();
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        spinnerProgramFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedProgram = parent.getItemAtPosition(position).toString();
                renderAllLists();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        renderAllLists();
    }

    private void renderAllLists() {
        loadCurrentGroupRoster();
        loadAvailableStudentsToEnroll();
    }

    private void loadCurrentGroupRoster() {
        new Thread(() -> {
            List<StudentEntity> groupMembers = AppDatabase.getInstance(getApplicationContext())
                    .studentDao()
                    .getStudentsByLabGroup(targetGroupName);

            runOnUiThread(() -> {
                containerGroupMembers.removeAllViews();
                currentGroupSize = 0;

                if (groupMembers != null) {
                    LayoutInflater inflater = LayoutInflater.from(this);
                    for (StudentEntity student : groupMembers) {
                        currentGroupSize++;
                        final String username = student.getUsername();
                        final String name = student.getName();
                        String idNum = student.getStudentNum();
                        String programStr = student.getProgram();

                        View row = inflater.inflate(R.layout.item_student_card, containerGroupMembers, false);
                        TextView txtName = row.findViewById(R.id.lblStudentName);
                        TextView txtNum = row.findViewById(R.id.lblStudentNumber);
                        TextView txtMeta = row.findViewById(R.id.lblProgramGroup);
                        Button btnRemove = row.findViewById(R.id.btnRemoveStudent);
                        Button btnDeleteRecord = row.findViewById(R.id.btnDeleteRecordEntirely);

                        txtName.setText(name);
                        txtNum.setText("ID: " + idNum);
                        txtMeta.setText(programStr + " | " + targetGroupName);

                        btnRemove.setText("Remove");
                        btnRemove.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFF1976D2));
                        btnRemove.setOnClickListener(v -> changeStudentGroupAllocation(username, "Unassigned", "Member removed from group."));

                        if (btnDeleteRecord != null) {
                            btnDeleteRecord.setOnClickListener(v -> promptPermanentDeletion(username, name));
                        }

                        containerGroupMembers.addView(row);
                    }
                }
                txtTargetGroupCount.setText("Occupancy: " + currentGroupSize + " / 15 Students Max");
            });
        }).start();
    }

    private void loadAvailableStudentsToEnroll() {
        new Thread(() -> {
            List<StudentEntity> availableStudents = AppDatabase.getInstance(getApplicationContext())
                    .studentDao()
                    .getAllStudents();

            runOnUiThread(() -> {
                containerAvailableStudents.removeAllViews();
                if (availableStudents != null) {
                    LayoutInflater inflater = LayoutInflater.from(this);
                    for (StudentEntity student : availableStudents) {
                        String grp = student.getLabGroup();
                        if (grp != null && grp.equals(targetGroupName)) continue;
                        if (!"Student".equals(student.getRole())) continue;

                        if (!selectedProgram.equals("All Programs") && !selectedProgram.equalsIgnoreCase(student.getProgram())) {
                            continue;
                        }
                        if (!searchString.isEmpty()) {
                            boolean matchesName = student.getName() != null && student.getName().toLowerCase().contains(searchString.toLowerCase());
                            boolean matchesNum = student.getStudentNum() != null && student.getStudentNum().toLowerCase().contains(searchString.toLowerCase());
                            if (!matchesName && !matchesNum) continue;
                        }

                        final String username = student.getUsername();
                        final String name = student.getName();
                        String idNum = student.getStudentNum();

                        View row = inflater.inflate(R.layout.item_student_card, containerAvailableStudents, false);
                        TextView txtName = row.findViewById(R.id.lblStudentName);
                        TextView txtNum = row.findViewById(R.id.lblStudentNumber);
                        TextView txtMeta = row.findViewById(R.id.lblProgramGroup);
                        Button btnAdd = row.findViewById(R.id.btnRemoveStudent);
                        Button btnDeleteRecord = row.findViewById(R.id.btnDeleteRecordEntirely);

                        txtName.setText(name);
                        txtNum.setText("ID: " + idNum);
                        txtMeta.setText("Current: " + (grp == null || grp.isEmpty() ? "Unassigned" : grp));

                        btnAdd.setText("Add");
                        btnAdd.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFF4CAF50));
                        btnAdd.setOnClickListener(v -> {
                            if (currentGroupSize >= 15) {
                                Toast.makeText(AssignStudentsActivity.this, "Denied: Group cap limit hit (Max 15)!", Toast.LENGTH_LONG).show();
                                return;
                            }
                            changeStudentGroupAllocation(username, targetGroupName, "Student added to group!");
                        });

                        if (btnDeleteRecord != null) {
                            btnDeleteRecord.setOnClickListener(v -> promptPermanentDeletion(username, name));
                        }

                        containerAvailableStudents.addView(row);
                    }
                }
            });
        }).start();
    }

    private void changeStudentGroupAllocation(String username, String targetGroup, String successMsg) {
        new Thread(() -> {
            AppDatabase.getInstance(getApplicationContext())
                    .studentDao()
                    .updateStudentGroup(username, targetGroup);

            runOnUiThread(() -> {
                Toast.makeText(this, successMsg, Toast.LENGTH_SHORT).show();
                renderAllLists();
            });
        }).start();
    }

    private void promptPermanentDeletion(final String usernameKey, String studentDisplayName) {
        new AlertDialog.Builder(this)
                .setTitle("Permanently Delete Student?")
                .setMessage("Are you absolutely sure you want to delete " + studentDisplayName + " completely from the entire system? This action cannot be undone.")
                .setPositiveButton("Yes, Delete", (dialog, which) -> {
                    new Thread(() -> {
                        AppDatabase.getInstance(getApplicationContext())
                                .studentDao()
                                .deleteUserByUsername(usernameKey);

                        runOnUiThread(() -> {
                            Toast.makeText(AssignStudentsActivity.this, "Student profile dropped successfully.", Toast.LENGTH_SHORT).show();
                            renderAllLists();
                        });
                    }).start();
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .create()
                .show();
    }

    private void showRenameGroupDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Rename Lab Group");

        final EditText input = new EditText(this);
        input.setText(targetGroupName);
        input.setSelectAllOnFocus(true);
        builder.setView(input);

        builder.setPositiveButton("Save", (dialog, which) -> {
            String newName = input.getText().toString().trim();
            if (newName.isEmpty()) {
                Toast.makeText(this, "Group label cannot be blank!", Toast.LENGTH_SHORT).show();
                return;
            }

            final String oldGroupName = targetGroupName;
            new Thread(() -> {
                AppDatabase db = AppDatabase.getInstance(getApplicationContext());
                db.studentDao().updateGroupMembersLabGroup(oldGroupName, newName);

                runOnUiThread(() -> {
                    targetGroupName = newName;
                    txtTargetGroupTitle.setText("Managing: " + targetGroupName);
                    Toast.makeText(this, "Group details updated!", Toast.LENGTH_SHORT).show();
                    renderAllLists();
                });
            }).start();
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }
}