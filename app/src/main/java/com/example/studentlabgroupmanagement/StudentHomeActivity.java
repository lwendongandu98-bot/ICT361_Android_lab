package com.example.studentlabgroupmanagement;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class StudentHomeActivity extends AppCompatActivity {

    private TextView tvWelcomeName, tvProfileUsername, tvProfileProgram, tvCurrentGroupStatus;
    private Button btnLogout, btnViewAvailableGroups;
    private SessionManager session;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_home);

        session = new SessionManager(this);
        dbHelper = new DatabaseHelper(this);

        // Map Visual XML fields to clean layout tokens
        tvWelcomeName = findViewById(R.id.tvWelcomeName);
        tvProfileUsername = findViewById(R.id.tvProfileUsername);
        tvProfileProgram = findViewById(R.id.tvProfileProgram);
        tvCurrentGroupStatus = findViewById(R.id.tvCurrentGroupStatus);
        btnLogout = findViewById(R.id.btnLogout);
        btnViewAvailableGroups = findViewById(R.id.btnViewAvailableGroups);

        // Populate active student session profile data rows dynamically from SQLite
        loadStudentData();

        // Handle logout process action cleanly
        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                session.logoutUser();
                Toast.makeText(StudentHomeActivity.this, "Logged out successfully", Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(StudentHomeActivity.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            }
        });

        // Setup browse groups action trigger link
        btnViewAvailableGroups.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(StudentHomeActivity.this, "Opening Lab Groups browser...", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadStudentData() {
        String currentUsername = session.getUsername();
        if (currentUsername == null) {
            finish();
            return;
        }

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT name, program, lab_group FROM students WHERE username = ?", new String[]{currentUsername});

        if (cursor.moveToFirst()) {
            String name = cursor.getString(0);
            String program = cursor.getString(1);
            String labGroup = cursor.getString(2);

            // Bind values dynamically
            tvWelcomeName.setText("Welcome, " + name + "!");
            tvProfileUsername.setText("Username: " + currentUsername);
            tvProfileProgram.setText("Program: " + program);

            if (labGroup != null && !labGroup.equals("None") && !labGroup.equals("Select group")) {
                tvCurrentGroupStatus.setText("Assigned: " + labGroup);
            } else {
                tvCurrentGroupStatus.setText("Not Assigned to Any Group");
            }
        }
        cursor.close();
    }
}
