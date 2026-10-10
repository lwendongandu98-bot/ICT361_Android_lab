package com.example.studentlabgroupmanagement;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class StudentAnnouncementsActivity extends AppCompatActivity {

    private RecyclerView rvAnnouncements;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_announcements);

        Toolbar toolbar = findViewById(R.id.toolbarStudentAnnouncements);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            }
            toolbar.setNavigationOnClickListener(v -> onBackPressed());
        }

        rvAnnouncements = findViewById(R.id.rvStudentAnnouncements);
        dbHelper = new DatabaseHelper(this);

        rvAnnouncements.setLayoutManager(new LinearLayoutManager(this));

        List<Announcement> list = dbHelper.getAllAnnouncements();
        rvAnnouncements.setAdapter(new AnnouncementAdapter(list));
    }
}