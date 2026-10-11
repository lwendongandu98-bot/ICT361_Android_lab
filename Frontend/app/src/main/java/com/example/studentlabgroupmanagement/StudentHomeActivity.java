package com.example.studentlabgroupmanagement;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.studentlabgroupmanagement.data.local.AppDatabase;
import com.example.studentlabgroupmanagement.data.local.entity.StudentEntity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;

public class StudentHomeActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private ImageButton menuButton;
    private BottomNavigationView bottomNavigation;

    private TextView tvWelcomeName;
    private TextView tvStudentProgram;

    private LinearLayout layoutStudentProfileBtn;
    private ImageView imgTopProfileIcon;

    private CardView cardMyGroups;
    private CardView cardProfile;
    private CardView cardLabs;
    private CardView cardAnnouncements;

    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_home);

        sessionManager = new SessionManager(this);

        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navigationView);
        menuButton = findViewById(R.id.menuButton);
        bottomNavigation = findViewById(R.id.bottomNavigation);

        tvWelcomeName = findViewById(R.id.tvWelcomeName);
        tvStudentProgram = findViewById(R.id.tvStudentProgram);

        imgTopProfileIcon = findViewById(R.id.imgTopProfileIcon);
        layoutStudentProfileBtn = findViewById(R.id.layoutStudentProfileBtn);

        String activeUsername = sessionManager.getUsername();
        if (activeUsername == null || activeUsername.trim().isEmpty()) {
            SharedPreferences sharedPref = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
            activeUsername = sharedPref.getString("username", "");
        }

        final String currentUsername = activeUsername;

        if (layoutStudentProfileBtn != null) {
            layoutStudentProfileBtn.setOnClickListener(v -> {
                Intent intent = new Intent(StudentHomeActivity.this, StudentProfileActivity.class);
                intent.putExtra("USERNAME", currentUsername);
                startActivity(intent);
            });
        }

        cardMyGroups = findViewById(R.id.cardMyGroups);
        cardProfile = findViewById(R.id.cardProfile);
        cardLabs = findViewById(R.id.cardLabs);
        cardAnnouncements = findViewById(R.id.cardAnnouncements);

        loadUserProfile();

        menuButton.setOnClickListener(v ->
                drawerLayout.openDrawer(GravityCompat.START));

        setupDashboardClickListeners();

        if (bottomNavigation != null) {
            bottomNavigation.setSelectedItemId(R.id.navHome);
            bottomNavigation.setOnItemSelectedListener(item -> {
                int id = item.getItemId();

                if (id == R.id.navHome) {
                    return true;
                } else if (id == R.id.navLabs) {
                    startActivity(new Intent(StudentHomeActivity.this, StudentLabActivity.class));
                    return true;
                } else if (id == R.id.navAnnouncements) {
                    startActivity(new Intent(StudentHomeActivity.this, StudentAnnouncementsActivity.class));
                    return true;
                }
                return false;
            });
        }

        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                drawerLayout.closeDrawer(GravityCompat.START);
            } else if (id == R.id.nav_profile) {
                Intent intent = new Intent(StudentHomeActivity.this, StudentProfileActivity.class);
                intent.putExtra("USERNAME", currentUsername);
                startActivity(intent);
            } else if (id == R.id.nav_help) {
                startActivity(new Intent(StudentHomeActivity.this, HelpActivity.class));
            } else if (id == R.id.nav_terms) {
                startActivity(new Intent(StudentHomeActivity.this, TermsActivity.class));
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
        if (bottomNavigation != null) {
            bottomNavigation.setSelectedItemId(R.id.navHome);
        }
    }

    private void loadUserProfile() {
        String username = sessionManager.getUsername();

        if (username == null || username.trim().isEmpty()) {
            SharedPreferences sharedPref = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
            username = sharedPref.getString("username", "");
        }

        if ((username == null || username.trim().isEmpty()) && getIntent() != null) {
            username = getIntent().getStringExtra("username");
        }

        if (username != null && !username.trim().isEmpty()) {
            final String targetUser = username.trim();
            new Thread(() -> {
                StudentEntity student = AppDatabase.getInstance(getApplicationContext())
                        .studentDao()
                        .getStudentById(targetUser);

                runOnUiThread(() -> {
                    if (student != null) {
                        String name = student.getName() != null ? student.getName() : "";
                        String program = student.getProgram() != null ? student.getProgram() : "";

                        if (tvWelcomeName != null && !name.isEmpty()) {
                            tvWelcomeName.setText("Hello, " + name);
                        }
                        if (tvStudentProgram != null && !program.isEmpty()) {
                            tvStudentProgram.setText("Student • " + program);
                        }
                    }

                    SharedPreferences prefs = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
                    String savedUriStr = prefs.getString("profile_image_" + targetUser, null);
                    if (savedUriStr != null && imgTopProfileIcon != null) {
                        try {
                            imgTopProfileIcon.setImageURI(Uri.parse(savedUriStr));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            }).start();
        }
    }

    private void setupDashboardClickListeners() {
        if (cardMyGroups != null) {
            cardMyGroups.setOnClickListener(v ->
                    startActivity(new Intent(StudentHomeActivity.this, MyGroupActivity.class)));
        }

        if (cardProfile != null) {
            cardProfile.setOnClickListener(v -> {
                String username = sessionManager.getUsername();
                Intent intent = new Intent(StudentHomeActivity.this, StudentProfileActivity.class);
                intent.putExtra("USERNAME", username);
                startActivity(intent);
            });
        }

        if (cardLabs != null) {
            cardLabs.setOnClickListener(v ->
                    startActivity(new Intent(StudentHomeActivity.this, StudentLabActivity.class)));
        }

        if (cardAnnouncements != null) {
            cardAnnouncements.setOnClickListener(v ->
                    startActivity(new Intent(StudentHomeActivity.this, StudentAnnouncementsActivity.class)));
        }
    }

    private void performLogout() {
        sessionManager.logoutUser();

        SharedPreferences sharedPref = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        sharedPref.edit().clear().apply();

        Intent intent = new Intent(StudentHomeActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        startActivity(intent);
        finish();
    }
}