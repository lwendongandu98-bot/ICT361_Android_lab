package com.example.studentlabgroupmanagement;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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

        // Safely set up toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
        }

        // Safely set up drawer layout & navigation view
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

        // Quick Action Cards initialization
        cardGroups = findViewById(R.id.cardGroups);
        cardStudents = findViewById(R.id.cardStudents);
        cardLabs = findViewById(R.id.cardLabs);
        cardAssignments = findViewById(R.id.cardAssignments);
        cardAnnouncements = findViewById(R.id.cardAnnouncements);
        cardSubmissions = findViewById(R.id.cardSubmissions);

        // Set Click Listeners safely with Null Checks
        if (cardGroups != null) {
            cardGroups.setOnClickListener(v ->
                    startActivity(new Intent(LecturerHomeActivity.this, GroupsActivity.class))
            );
        } else {
            Log.e("LecturerHome", "cardGroups missing in activity_lecturer_home.xml");
        }

        if (cardStudents != null) {
            cardStudents.setOnClickListener(v ->
                    startActivity(new Intent(LecturerHomeActivity.this, StudentsActivity.class))
            );
        } else {
            Log.e("LecturerHome", "cardStudents missing in activity_lecturer_home.xml");
        }

        if (cardLabs != null) {
            cardLabs.setOnClickListener(v ->
                    startActivity(new Intent(LecturerHomeActivity.this, LabsActivity.class))
            );
        } else {
            Log.e("LecturerHome", "cardLabs missing in activity_lecturer_home.xml");
        }

        if (cardAssignments != null) {
            cardAssignments.setOnClickListener(v ->
                    startActivity(new Intent(LecturerHomeActivity.this, AssignmentsActivity.class))
            );
        } else {
            Log.e("LecturerHome", "cardAssignments missing in activity_lecturer_home.xml");
        }

        if (cardAnnouncements != null) {
            cardAnnouncements.setOnClickListener(v ->
                    startActivity(new Intent(LecturerHomeActivity.this, AnnouncementsActivity.class))
            );
        } else {
            Log.e("LecturerHome", "cardAnnouncements missing in activity_lecturer_home.xml");
        }

        if (cardSubmissions != null) {
            cardSubmissions.setOnClickListener(v ->
                    startActivity(new Intent(LecturerHomeActivity.this, SubmissionsActivity.class))
            );
        } else {
            Log.e("LecturerHome", "cardSubmissions missing in activity_lecturer_home.xml");
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        // Integrated drawer layout items to mirror dashboard capabilities
        if (id == R.id.nav_groups) {
            startActivity(new Intent(LecturerHomeActivity.this, GroupsActivity.class));
        } else if (id == R.id.nav_students) {
            startActivity(new Intent(LecturerHomeActivity.this, StudentsActivity.class));
        } else if (id == R.id.nav_labs) {
            startActivity(new Intent(LecturerHomeActivity.this, LabsActivity.class));
        } else if (id == R.id.nav_assignments) {
            startActivity(new Intent(LecturerHomeActivity.this, AssignmentsActivity.class));
        } else if (id == R.id.nav_announcements) {
            startActivity(new Intent(LecturerHomeActivity.this, AnnouncementsActivity.class));
        } else if (id == R.id.nav_submissions) {
            startActivity(new Intent(LecturerHomeActivity.this, SubmissionsActivity.class));
        } else if (id == R.id.nav_logout) {
            Intent intent = new Intent(LecturerHomeActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }

        if (drawerLayout != null) {
            drawerLayout.closeDrawer(GravityCompat.START);
        }

        return true;
    }
}
