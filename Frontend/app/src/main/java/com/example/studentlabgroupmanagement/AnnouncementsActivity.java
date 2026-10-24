package com.example.studentlabgroupmanagement;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.studentlabgroupmanagement.data.local.AppDatabase;
import com.example.studentlabgroupmanagement.data.local.entity.AnnouncementEntity;
import com.example.studentlabgroupmanagement.data.remote.RetrofitClient;
import com.example.studentlabgroupmanagement.data.repository.GroupRepository;
import com.example.studentlabgroupmanagement.data.worker.SyncHelper;
import com.example.studentlabgroupmanagement.ui.viewmodel.AnnouncementsViewModel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AnnouncementsActivity extends AppCompatActivity {

    private EditText etTitle, etMessage;
    private Button btnPost;
    private RecyclerView rvAnnouncements;

    private AnnouncementsViewModel announcementsViewModel;
    private AnnouncementAdapter announcementAdapter;

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

            // --- DYNAMICALLY PUSH TOOLBAR BELOW STATUS BAR ---
            ViewCompat.setOnApplyWindowInsetsListener(toolbar, (v, windowInsets) -> {
                int statusBarHeight = windowInsets.getInsets(WindowInsetsCompat.Type.statusBars()).top;

                // Set top padding to the height of the status bar so content doesn't clip,
                // while the blue background color still stretches to the absolute top edge!
                v.setPadding(v.getPaddingLeft(), statusBarHeight, v.getPaddingRight(), v.getPaddingBottom());

                return windowInsets;
            });
            // ------------------------------------------------
        }

        etTitle = findViewById(R.id.etAnnouncementTitle);
        etMessage = findViewById(R.id.etAnnouncementMessage);
        btnPost = findViewById(R.id.btnPostAnnouncement);
        rvAnnouncements = findViewById(R.id.rvAnnouncements);

        rvAnnouncements.setLayoutManager(new LinearLayoutManager(this));

        // Initialize RecyclerView adapter with an empty list initially
        announcementAdapter = new AnnouncementAdapter(new ArrayList<>());
        rvAnnouncements.setAdapter(announcementAdapter);

        // Initialize ViewModel
        announcementsViewModel = new ViewModelProvider(this).get(AnnouncementsViewModel.class);

        // --- CHECK SERVER CONNECTION ON STARTUP ---
        NetworkCheckHelper.checkConnection(this);
        // ------------------------------------------

        btnPost.setOnClickListener(v -> postAnnouncement());

        // Observe announcements reactively via LiveData from ViewModel
        announcementsViewModel.getAllAnnouncements().observe(this, announcementEntities -> {
            if (announcementEntities != null) {
                announcementAdapter = new AnnouncementAdapter(announcementEntities);
                rvAnnouncements.setAdapter(announcementAdapter);
            }
        });

        // Pull latest announcements from backend on startup
        syncAndLoadAnnouncements();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Sync whenever the activity comes back into focus
        syncAndLoadAnnouncements();
    }

    private void syncAndLoadAnnouncements() {
        // 1. Silently pull latest announcements from the Node.js backend server
        RetrofitClient.getApiService().getAllAnnouncements().enqueue(new Callback<List<AnnouncementEntity>>() {
            @Override
            public void onResponse(Call<List<AnnouncementEntity>> call, Response<List<AnnouncementEntity>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<AnnouncementEntity> serverAnnouncements = response.body();

                    // 2. Save fetched announcements into local Room database in the background for offline access
                    new Thread(() -> {
                        AppDatabase db = AppDatabase.getInstance(getApplicationContext());
                        for (AnnouncementEntity announcement : serverAnnouncements) {
                            try {
                                db.announcementDao().insertAnnouncement(announcement);
                            } catch (Exception e) {
                                Log.w("AnnouncementSync", "Announcement already exists or update skipped: " + announcement.getTitle());
                            }
                        }
                    }).start();
                }
            }

            @Override
            public void onFailure(Call<List<AnnouncementEntity>> call, Throwable t) {
                // Offline fallback: LiveData will still display whatever is locally cached in Room
                Log.w("AnnouncementSync", "Offline mode: Sync skipped, loading local cache. Error: " + t.getMessage());
            }
        });
    }

    private void postAnnouncement() {
        String title = etTitle.getText().toString().trim();
        String message = etMessage.getText().toString().trim();

        if (TextUtils.isEmpty(title) || TextUtils.isEmpty(message)) {
            Toast.makeText(this, "Please enter both title and message", Toast.LENGTH_SHORT).show();
            return;
        }

        String currentDate = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(new Date());
        AnnouncementEntity announcement = new AnnouncementEntity(title, message, currentDate);

        // Delegate insertion and syncing through ViewModel and Repository
        announcementsViewModel.insertAnnouncement(announcement, new GroupRepository.OnOperationCallback() {
            @Override
            public void onSuccess(String message) {
                // --- TRIGGER UNIFIED WORKMANAGER BACKGROUND SYNC ---
                SyncHelper.triggerSync(AnnouncementsActivity.this);
                // --------------------------------------------------

                runOnUiThread(() -> {
                    Toast.makeText(AnnouncementsActivity.this, message, Toast.LENGTH_SHORT).show();
                    etTitle.setText("");
                    etMessage.setText("");
                    syncAndLoadAnnouncements(); // Refresh after posting
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    Toast.makeText(AnnouncementsActivity.this, "Error: " + error, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }
}