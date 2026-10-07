package com.example.studentlabgroupmanagement;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;

public class StudentHomeActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private ImageButton menuButton;

    private TextView tvWelcomeName;
    private TextView tvStudentProgram;

    private CardView cardMyGroups;
    private CardView cardTimetable;
    private CardView cardProfile;
    private CardView cardTasks;
    private CardView cardLabs;

    private DatabaseHelper dbHelper;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_home);

        dbHelper = new DatabaseHelper(this);
        sessionManager = new SessionManager(this);

        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navigationView);
        menuButton = findViewById(R.id.menuButton);

        tvWelcomeName = findViewById(R.id.tvWelcomeName);
        tvStudentProgram = findViewById(R.id.tvStudentProgram);

        cardMyGroups = findViewById(R.id.cardMyGroups);
        cardTimetable = findViewById(R.id.cardTimetable);
        cardProfile = findViewById(R.id.cardProfile);
        cardTasks = findViewById(R.id.cardTasks);
        cardLabs = findViewById(R.id.cardLabs);

        loadUserProfile();

        menuButton.setOnClickListener(v ->
                drawerLayout.openDrawer(GravityCompat.START));

        setupDashboardClickListeners();

        navigationView.setNavigationItemSelectedListener(item -> {

            int id = item.getItemId();

            if (id == R.id.nav_home) {

                drawerLayout.closeDrawer(GravityCompat.START);

            } else if (id == R.id.nav_labs) {

                startActivity(new Intent(
                        StudentHomeActivity.this,
                        StudentLabActivity.class));

            } else if (id == R.id.nav_profile) {

                startActivity(new Intent(
                        StudentHomeActivity.this,
                        StudentProfileActivity.class));

            } else if (id == R.id.nav_groups) {

                startActivity(new Intent(
                        StudentHomeActivity.this,
                        MyGroupActivity.class));

            } else if (id == R.id.nav_timetable) {

                startActivity(new Intent(
                        StudentHomeActivity.this,
                        TimetableActivity.class));

            } else if (id == R.id.nav_tasks) {

                startActivity(new Intent(
                        StudentHomeActivity.this,
                        TasksActivity.class));

            } else if (id == R.id.nav_notifications) {

                startActivity(new Intent(
                        StudentHomeActivity.this,
                        NotificationActivity.class));

            } else if (id == R.id.nav_settings) {

                startActivity(new Intent(
                        StudentHomeActivity.this,
                        SettingsActivity.class));

            } else if (id == R.id.nav_logout) {

                performLogout();
            }

            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });

        getOnBackPressedDispatcher().addCallback(
                this,
                new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {

                        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                            drawerLayout.closeDrawer(GravityCompat.START);
                        } else {
                            finish();
                        }
                    }
                });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadUserProfile();
    }

    private void loadUserProfile() {
        // Priority 1: Read active username from SessionManager
        String username = sessionManager.getUsername();

        // Priority 2: Fall back to UserPrefs SharedPreferences
        if (username == null || username.trim().isEmpty()) {
            SharedPreferences sharedPref = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
            username = sharedPref.getString("username", "");
        }

        // Priority 3: Fall back to Intent extra
        if ((username == null || username.trim().isEmpty()) && getIntent() != null) {
            username = getIntent().getStringExtra("username");
        }

        if (username != null && !username.trim().isEmpty()) {
            Cursor cursor = dbHelper.getUserProfile(username.trim());
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    int nameIdx = cursor.getColumnIndex("student_name");
                    int progIdx = cursor.getColumnIndex("program");

                    String name = (nameIdx != -1 && !cursor.isNull(nameIdx)) ? cursor.getString(nameIdx) : "";
                    String program = (progIdx != -1 && !cursor.isNull(progIdx)) ? cursor.getString(progIdx) : "";

                    if (tvWelcomeName != null && !name.isEmpty()) {
                        tvWelcomeName.setText("Hello, " + name);
                    }
                    if (tvStudentProgram != null && !program.isEmpty()) {
                        tvStudentProgram.setText("Student • " + program);
                    }
                }
                cursor.close();
            }
        }
    }

    private void setupDashboardClickListeners() {

        if (cardMyGroups != null) {
            cardMyGroups.setOnClickListener(v ->
                    startActivity(new Intent(
                            StudentHomeActivity.this,
                            MyGroupActivity.class)));
        }

        if (cardTimetable != null) {
            cardTimetable.setOnClickListener(v ->
                    startActivity(new Intent(
                            StudentHomeActivity.this,
                            TimetableActivity.class)));
        }

        if (cardProfile != null) {
            cardProfile.setOnClickListener(v ->
                    startActivity(new Intent(
                            StudentHomeActivity.this,
                            StudentProfileActivity.class)));
        }

        if (cardTasks != null) {
            cardTasks.setOnClickListener(v ->
                    startActivity(new Intent(
                            StudentHomeActivity.this,
                            TasksActivity.class)));
        }

        if (cardLabs != null) {
            cardLabs.setOnClickListener(v ->
                    startActivity(new Intent(
                            StudentHomeActivity.this,
                            StudentLabActivity.class)));
        }
    }

    private void performLogout() {

        sessionManager.logoutUser();

        SharedPreferences sharedPref = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        sharedPref.edit().clear().apply();

        Intent intent = new Intent(
                StudentHomeActivity.this,
                MainActivity.class);

        intent.setFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK
                        | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        startActivity(intent);
        finish();
    }
}