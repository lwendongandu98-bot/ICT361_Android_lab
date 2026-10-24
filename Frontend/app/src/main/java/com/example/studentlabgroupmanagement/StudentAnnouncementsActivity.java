package com.example.studentlabgroupmanagement;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.studentlabgroupmanagement.data.local.AppDatabase;
import com.example.studentlabgroupmanagement.data.local.entity.AnnouncementEntity;

import java.util.List;

public class StudentAnnouncementsActivity extends AppCompatActivity {

    private RecyclerView rvAnnouncements;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_announcements);

        toolbar = findViewById(R.id.toolbarStudentAnnouncements);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            }
            toolbar.setNavigationOnClickListener(v -> onBackPressed());

            // --- DYNAMICALLY PUSH TOOLBAR BELOW STATUS BAR ---
            ViewCompat.setOnApplyWindowInsetsListener(toolbar, (v, windowInsets) -> {
                int statusBarHeight = windowInsets.getInsets(WindowInsetsCompat.Type.statusBars()).top;

                // Set top padding to the height of the status bar so content doesn't clip,
                // while the background color stretches to the absolute top edge!
                v.setPadding(v.getPaddingLeft(), statusBarHeight, v.getPaddingRight(), v.getPaddingBottom());

                return windowInsets;
            });
            // ------------------------------------------------
        }

        rvAnnouncements = findViewById(R.id.rvStudentAnnouncements);
        rvAnnouncements.setLayoutManager(new LinearLayoutManager(this));

        new Thread(() -> {
            List<AnnouncementEntity> list = AppDatabase.getInstance(getApplicationContext()).announcementDao().getAllAnnouncementsSync();
            runOnUiThread(() -> rvAnnouncements.setAdapter(new AnnouncementAdapter(list)));
        }).start();
    }
}