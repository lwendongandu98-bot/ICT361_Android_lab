package com.example.studentlabgroupmanagement;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.Toast;

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

    private CardView cardMyGroups;
    private CardView cardTimetable;
    private CardView cardProfile;
    private CardView cardTasks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_home);

        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navigationView);
        menuButton = findViewById(R.id.menuButton);

        cardMyGroups = findViewById(R.id.cardMyGroups);
        cardTimetable = findViewById(R.id.cardTimetable);
        cardProfile = findViewById(R.id.cardProfile);
        cardTasks = findViewById(R.id.cardTasks);

        menuButton.setOnClickListener(v ->
                drawerLayout.openDrawer(GravityCompat.START));

        setupDashboardClickListeners();

        navigationView.setNavigationItemSelectedListener(item -> {

            int id = item.getItemId();

            if (id == R.id.nav_home) {

                drawerLayout.closeDrawer(GravityCompat.START);

            } else if (id == R.id.nav_profile) {

                startActivity(new Intent(
                        StudentHomeActivity.this,
                        StudentProfileActivity.class));

            } else if (id == R.id.nav_groups) {

                Toast.makeText(
                        StudentHomeActivity.this,
                        "Opening Groups...",
                        Toast.LENGTH_SHORT).show();

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

    private void setupDashboardClickListeners() {

        cardMyGroups.setOnClickListener(v ->
                Toast.makeText(
                        StudentHomeActivity.this,
                        "Opening Groups...",
                        Toast.LENGTH_SHORT).show());

        cardTimetable.setOnClickListener(v ->
                startActivity(new Intent(
                        StudentHomeActivity.this,
                        TimetableActivity.class)));

        cardProfile.setOnClickListener(v ->
                startActivity(new Intent(
                        StudentHomeActivity.this,
                        StudentProfileActivity.class)));

        cardTasks.setOnClickListener(v ->
                startActivity(new Intent(
                        StudentHomeActivity.this,
                        TasksActivity.class)));
    }

    private void performLogout() {

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