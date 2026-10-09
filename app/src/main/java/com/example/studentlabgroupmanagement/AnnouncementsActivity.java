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

import java.util.List;

public class AnnouncementsActivity extends AppCompatActivity {

    private EditText etTitle, etMessage;
    private Button btnPost;
    private RecyclerView rvAnnouncements;
    private DatabaseHelper dbHelper;

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
        dbHelper = new DatabaseHelper(this);

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

        if (dbHelper.saveAnnouncement(title, message)) {
            Toast.makeText(this, "Announcement posted successfully!", Toast.LENGTH_SHORT).show();
            etTitle.setText("");
            etMessage.setText("");
            loadAnnouncements();
        } else {
            Toast.makeText(this, "Failed to post announcement.", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadAnnouncements() {
        List<Announcement> list = dbHelper.getAllAnnouncements();
        rvAnnouncements.setAdapter(new AnnouncementAdapter(list));
    }
}