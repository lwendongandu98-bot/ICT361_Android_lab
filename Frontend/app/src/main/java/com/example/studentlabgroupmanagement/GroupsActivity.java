package com.example.studentlabgroupmanagement;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class GroupsActivity extends AppCompatActivity {

    private EditText etGroupName, etSearchGroups;
    private Button btnCreateGroup;
    private LinearLayout groupsContainer;
    private DatabaseHelper dbHelper;
    private String filterQuery = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_groups);

        dbHelper = new DatabaseHelper(this);

        etGroupName = findViewById(R.id.etGroupName);
        etSearchGroups = findViewById(R.id.etSearchGroups);
        btnCreateGroup = findViewById(R.id.btnCreateGroup);
        groupsContainer = findViewById(R.id.groupsContainer);

        btnCreateGroup.setOnClickListener(v -> {

            String groupName =
                    etGroupName.getText()
                            .toString()
                            .trim();

            if (groupName.isEmpty()) {

                Toast.makeText(
                        this,
                        "Please type a group name!",
                        Toast.LENGTH_SHORT
                ).show();

                return;
            }

            createNewLabGroup(groupName);
        });

        etSearchGroups.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(
                    CharSequence s,
                    int start,
                    int count,
                    int after) {
            }

            @Override
            public void onTextChanged(
                    CharSequence s,
                    int start,
                    int before,
                    int count) {

                filterQuery =
                        s.toString().trim();

                loadGroupsFromDatabase();
            }

            @Override
            public void afterTextChanged(
                    Editable s) {
            }
        });

        loadGroupsFromDatabase();
    }

    private void createNewLabGroup(String name) {

        SQLiteDatabase db =
                dbHelper.getWritableDatabase();

        ContentValues values =
                new ContentValues();

        values.put("group_name", name);
        values.put(
                "description",
                "Lab Batch " + name
        );

        long result =
                db.insert(
                        "lab_groups",
                        null,
                        values
                );

        if (result != -1) {

            Toast.makeText(
                    this,
                    "Group created successfully!",
                    Toast.LENGTH_SHORT
            ).show();

            etGroupName.setText("");

            loadGroupsFromDatabase();

        } else {

            Toast.makeText(
                    this,
                    "Group already exists!",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    private void loadGroupsFromDatabase() {

        groupsContainer.removeAllViews();

        SQLiteDatabase db =
                dbHelper.getReadableDatabase();

        String query =
                "SELECT g.group_name, "
                        + "(SELECT COUNT(*) FROM users u "
                        + "WHERE u.lab_group = g.group_name "
                        + "AND u.role = 'Student') "
                        + "AS current_count "
                        + "FROM lab_groups g "
                        + "WHERE g.group_name LIKE ?";

        Cursor cursor =
                db.rawQuery(
                        query,
                        new String[]{
                                "%" + filterQuery + "%"
                        });

        if (cursor != null) {

            LayoutInflater inflater =
                    LayoutInflater.from(this);

            while (cursor.moveToNext()) {

                String name =
                        cursor.getString(0);

                int count =
                        cursor.getInt(1);

                View cardView =
                        inflater.inflate(
                                R.layout.item_group_card,
                                groupsContainer,
                                false
                        );

                TextView txtTitle =
                        cardView.findViewById(
                                R.id.txtGroupNameDisplay
                        );

                TextView txtCount =
                        cardView.findViewById(
                                R.id.txtGroupCapacityCounter
                        );

                Button btnManage =
                        cardView.findViewById(
                                R.id.btnAssignStudentsInline
                        );

                Button btnDelete =
                        cardView.findViewById(
                                R.id.btnDeleteGroup
                        );

                txtTitle.setText(name);

                txtCount.setText(
                        "Occupancy: "
                                + count
                                + " / 15 Members Max"
                );

                // MANAGE GROUP

                btnManage.setOnClickListener(v -> {

                    Intent intent =
                            new Intent(
                                    GroupsActivity.this,
                                    AssignStudentsActivity.class
                            );

                    intent.putExtra(
                            "TARGET_GROUP_NAME",
                            name
                    );

                    startActivity(intent);
                });

                // DELETE GROUP

                btnDelete.setOnClickListener(v -> {

                    new AlertDialog.Builder(
                            GroupsActivity.this
                    )
                            .setTitle("Delete Group")
                            .setMessage(
                                    "Are you sure you want to delete \""
                                            + name
                                            + "\"?"
                            )
                            .setPositiveButton(
                                    "Delete",
                                    (dialog, which) -> {

                                        SQLiteDatabase database =
                                                dbHelper.getWritableDatabase();

                                        int deleted =
                                                database.delete(
                                                        "lab_groups",
                                                        "group_name=?",
                                                        new String[]{name}
                                                );

                                        if (deleted > 0) {

                                            Toast.makeText(
                                                    GroupsActivity.this,
                                                    "Group deleted successfully",
                                                    Toast.LENGTH_SHORT
                                            ).show();

                                            loadGroupsFromDatabase();

                                        } else {

                                            Toast.makeText(
                                                    GroupsActivity.this,
                                                    "Failed to delete group",
                                                    Toast.LENGTH_SHORT
                                            ).show();
                                        }
                                    })
                            .setNegativeButton(
                                    "Cancel",
                                    null
                            )
                            .show();
                });

                groupsContainer.addView(cardView);
            }

            cursor.close();
        }
    }
}