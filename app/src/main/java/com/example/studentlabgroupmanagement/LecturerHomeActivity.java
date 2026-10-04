package com.example.studentlabgroupmanagement;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;

public class LecturerHomeActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout;

    private CardView cardGroups;
    private CardView cardStudents;
    private CardView cardLabs;
    private CardView cardAssignments;
    private CardView cardAnnouncements;
    private CardView cardSubmissions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lecturer_home);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawerLayout);

        NavigationView navigationView =
                findViewById(R.id.navigationView);

        navigationView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle =
                new ActionBarDrawerToggle(
                        this,
                        drawerLayout,
                        toolbar,
                        R.string.open_drawer,
                        R.string.close_drawer
                );

        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // Quick Action Cards
        cardGroups = findViewById(R.id.cardGroups);
        cardStudents = findViewById(R.id.cardStudents);
        cardLabs = findViewById(R.id.cardLabs);
        cardAssignments = findViewById(R.id.cardAssignments);
        cardAnnouncements = findViewById(R.id.cardAnnouncements);
        cardSubmissions = findViewById(R.id.cardSubmissions);

        // Groups
        cardGroups.setOnClickListener(v ->
                startActivity(
                        new Intent(
                                LecturerHomeActivity.this,
                                GroupsActivity.class
                        )
                )
        );

        // Students
        cardStudents.setOnClickListener(v ->
                startActivity(
                        new Intent(
                                LecturerHomeActivity.this,
                                StudentsActivity.class
                        )
                )
        );

        // Labs
        cardLabs.setOnClickListener(v ->
                startActivity(
                        new Intent(
                                LecturerHomeActivity.this,
                                LabsActivity.class
                        )
                )
        );

        // Assignments
        cardAssignments.setOnClickListener(v ->
                startActivity(
                        new Intent(
                                LecturerHomeActivity.this,
                                AssignmentsActivity.class
                        )
                )
        );

        // Announcements
        cardAnnouncements.setOnClickListener(v ->
                startActivity(
                        new Intent(
                                LecturerHomeActivity.this,
                                AnnouncementsActivity.class
                        )
                )
        );

        // Submissions
        cardSubmissions.setOnClickListener(v ->
                startActivity(
                        new Intent(
                                LecturerHomeActivity.this,
                                SubmissionsActivity.class
                        )
                )
        );
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.nav_logout) {
            finish();
        }

        drawerLayout.closeDrawer(GravityCompat.START);

        return true;
    }
}