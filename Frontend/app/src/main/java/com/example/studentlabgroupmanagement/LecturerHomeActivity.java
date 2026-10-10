package com.example.studentlabgroupmanagement;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.navigation.NavigationView;

public class LecturerHomeActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout;

    private CardView cardCourses;
    private CardView cardGroups;
    private CardView cardStudents;
    private CardView cardLabs;
    private CardView cardAnnouncements;
    private CardView cardSubmissions;
    private LinearLayout layoutLecturerProfileBtn;
    private ShapeableImageView imgTopProfileIcon;
    private TextView tvWelcomeLecturer, tvDepartment;

    private SessionManager sessionManager;
    private DatabaseHelper databaseHelper;
    private String currentUsername;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Force lecturer home to stay in light mode
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lecturer_home);

        sessionManager = new SessionManager(this);
        databaseHelper = new DatabaseHelper(this);

        // Get active username from Intent or SessionManager
        currentUsername = getIntent().getStringExtra("USERNAME");
        if (currentUsername == null || currentUsername.isEmpty()) {
            currentUsername = sessionManager.getUsername();
        }

        // Initialize Welcome Card TextViews
        tvWelcomeLecturer = findViewById(R.id.tvWelcomeLecturer);
        tvDepartment = findViewById(R.id.tvDepartment);

        // Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
        }

        // Top-Right Profile Button & Icon
        imgTopProfileIcon = findViewById(R.id.imgTopProfileIcon);
        layoutLecturerProfileBtn = findViewById(R.id.layoutLecturerProfileBtn);
        if (layoutLecturerProfileBtn != null) {
            layoutLecturerProfileBtn.setOnClickListener(v -> {
                Intent intent = new Intent(LecturerHomeActivity.this, LecturerProfileActivity.class);
                intent.putExtra("USERNAME", currentUsername);
                startActivity(intent);
            });
        }

        // Drawer Layout
        drawerLayout = findViewById(R.id.drawerLayout);

        NavigationView navigationView = findViewById(R.id.navigationView);

        if (navigationView != null) {
            navigationView.setNavigationItemSelectedListener(this);
        }

        if (drawerLayout != null && toolbar != null) {
            ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                    this,
                    drawerLayout,
                    toolbar,
                    R.string.open_drawer,
                    R.string.close_drawer
            );

            drawerLayout.addDrawerListener(toggle);
            toggle.syncState();
        }

        // Modern Back Press Dispatcher for Navigation Drawer
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (drawerLayout != null && drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START);
                } else {
                    finish();
                }
            }
        });

        // Dashboard Cards
        cardCourses = findViewById(R.id.cardCourses);
        cardGroups = findViewById(R.id.cardGroups);
        cardStudents = findViewById(R.id.cardStudents);
        cardLabs = findViewById(R.id.cardLabs);
        cardAnnouncements = findViewById(R.id.cardAnnouncements);
        cardSubmissions = findViewById(R.id.cardSubmissions);

        if (cardCourses != null) {
            cardCourses.setOnClickListener(v ->
                    startActivity(new Intent(
                            LecturerHomeActivity.this,
                            AddCourseActivity.class)));
        }

        if (cardGroups != null) {
            cardGroups.setOnClickListener(v ->
                    startActivity(new Intent(
                            LecturerHomeActivity.this,
                            GroupsActivity.class)));
        }

        if (cardStudents != null) {
            cardStudents.setOnClickListener(v ->
                    startActivity(new Intent(
                            LecturerHomeActivity.this,
                            StudentsActivity.class)));
        }

        if (cardLabs != null) {
            cardLabs.setOnClickListener(v ->
                    startActivity(new Intent(
                            LecturerHomeActivity.this,
                            LabsActivity.class)));
        }

        if (cardAnnouncements != null) {
            cardAnnouncements.setOnClickListener(v ->
                    startActivity(new Intent(
                            LecturerHomeActivity.this,
                            AnnouncementsActivity.class)));
        }

        if (cardSubmissions != null) {
            cardSubmissions.setOnClickListener(v ->
                    startActivity(new Intent(
                            LecturerHomeActivity.this,
                            SubmissionsActivity.class)));
        }

        // Bottom Navigation (Home, Groups, Labs)
        BottomNavigationView bottomNavigation = findViewById(R.id.bottomNavigation);

        if (bottomNavigation != null) {

            bottomNavigation.setSelectedItemId(R.id.nav_home);

            bottomNavigation.setOnItemSelectedListener(item -> {

                int id = item.getItemId();

                if (id == R.id.nav_home) {
                    // Already on Home screen
                    return true;
                }

                if (id == R.id.nav_groups) {
                    startActivity(new Intent(
                            LecturerHomeActivity.this,
                            GroupsActivity.class));
                    return true;
                }

                if (id == R.id.nav_labs) {
                    startActivity(new Intent(
                            LecturerHomeActivity.this,
                            LabsActivity.class));
                    return true;
                }

                return false;
            });
        }

        // Load Lecturer info on create
        loadLecturerInfo();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadLecturerInfo();
    }

    private void loadLecturerInfo() {
        if (currentUsername != null && !currentUsername.isEmpty()) {
            SharedPreferences prefs = getSharedPreferences("LecturerPrefs", Context.MODE_PRIVATE);

            // 1. Fetch from Database first, fallback to SharedPreferences if empty/null
            String lecturerName = databaseHelper.getLecturerName(currentUsername);
            if (lecturerName == null || lecturerName.isEmpty()) {
                lecturerName = prefs.getString("name_" + currentUsername, currentUsername);
            }

            String lecturerDept = databaseHelper.getLecturerDepartment(currentUsername);
            if (lecturerDept == null || lecturerDept.isEmpty()) {
                lecturerDept = prefs.getString("department_" + currentUsername, "Department Not Specified");
            }

            if (tvWelcomeLecturer != null) {
                tvWelcomeLecturer.setText("Welcome " + lecturerName);
            }
            if (tvDepartment != null) {
                tvDepartment.setText(lecturerDept);
            }

            // Dynamically load updated profile image whenever user returns to dashboard
            if (imgTopProfileIcon != null) {
                String savedUriStr = prefs.getString("profile_image_" + currentUsername, null);
                if (savedUriStr != null) {
                    try {
                        imgTopProfileIcon.setImageURI(Uri.parse(savedUriStr));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.nav_home) {
            // Already on home screen, just close drawer
        } else if (id == R.id.nav_profile) {
            Intent intent = new Intent(this, LecturerProfileActivity.class);
            intent.putExtra("USERNAME", currentUsername);
            startActivity(intent);
        } else if (id == R.id.nav_help) {
            startActivity(new Intent(this, HelpActivity.class));
        } else if (id == R.id.nav_terms) {
            startActivity(new Intent(this, TermsActivity.class));
        } else if (id == R.id.nav_logout) {
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK |
                            Intent.FLAG_ACTIVITY_CLEAR_TASK
            );
            startActivity(intent);
            finish();
        }

        if (drawerLayout != null) {
            drawerLayout.closeDrawer(GravityCompat.START);
        }

        return true;
    }
}