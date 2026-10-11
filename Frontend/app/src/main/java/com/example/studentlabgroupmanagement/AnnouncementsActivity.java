package com.example.studentlabgroupmanagement;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.studentlabgroupmanagement.data.local.AppDatabase;
import com.example.studentlabgroupmanagement.data.local.entity.AnnouncementEntity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AnnouncementsActivity extends AppCompatActivity {

    private EditText etTitle, etMessage;
    private Button btnPost;
    private RecyclerView rvAnnouncements;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_announcements);

        Toolbar toolbar = findViewById(R.id.toolbarAnnouncements);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            }
            toolbar.setNavigationOnClickListener(v -> onBackPressed());
        }

        etTitle = findViewById(R.id.etAnnouncementTitle);
        etMessage = findViewById(R.id.etAnnouncementMessage);
        btnPost = findViewById(R.id.btnPostAnnouncement);
        rvAnnouncements = findViewById(R.id.rvAnnouncements);

        rvAnnouncements.setLayoutManager(new LinearLayoutManager(this));

        btnPost.setOnClickListener(v -> postAnnouncement());

        loadAnnouncements();
    }

    private void postAnnouncement() {
        String title = etTitle.getText().toString().trim();
        String message = etMessage.getText().toString().trim();

        if (TextUtils.isEmpty(title) || TextUtils.isEmpty(message)) {
            Toast.makeText(this, "Please enter both title and message", Toast.LENGTH_SHORT).show();
            return;
        }

        // Perform Room insertion on a background thread
        new Thread(() -> {
            try {
                String currentDate = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(new Date());
                AnnouncementEntity announcement = new AnnouncementEntity(title, message, currentDate);

                AppDatabase.getInstance(getApplicationContext()).announcementDao().insertAnnouncement(announcement);

                runOnUiThread(() -> {
                    Toast.makeText(this, "Announcement posted successfully!", Toast.LENGTH_SHORT).show();
                    etTitle.setText("");
                    etMessage.setText("");
                    loadAnnouncements();
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    Toast.makeText(this, "Failed to post announcement: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    private void loadAnnouncements() {
        new Thread(() -> {
            List<AnnouncementEntity> list = AppDatabase.getInstance(getApplicationContext()).announcementDao().getAllAnnouncements();

            runOnUiThread(() -> {
                rvAnnouncements.setAdapter(new AnnouncementAdapter(list));
            });
        }).start();
    }
}