package com.example.studentlabgroupmanagement;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.studentlabgroupmanagement.data.local.AppDatabase;
import com.example.studentlabgroupmanagement.data.local.entity.AnnouncementEntity;

import java.util.List;

public class StudentAnnouncementsActivity extends AppCompatActivity {

    private RecyclerView rvAnnouncements;

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
        rvAnnouncements.setLayoutManager(new LinearLayoutManager(this));

        new Thread(() -> {
            List<AnnouncementEntity> list = AppDatabase.getInstance(getApplicationContext()).announcementDao().getAllAnnouncements();
            runOnUiThread(() -> rvAnnouncements.setAdapter(new AnnouncementAdapter(list)));
        }).start();
    }
}