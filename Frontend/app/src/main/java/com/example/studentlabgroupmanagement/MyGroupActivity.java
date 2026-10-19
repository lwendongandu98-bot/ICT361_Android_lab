package com.example.studentlabgroupmanagement;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.studentlabgroupmanagement.data.local.AppDatabase;
import com.example.studentlabgroupmanagement.data.local.entity.StudentEntity;

import java.util.List;

public class MyGroupActivity extends AppCompatActivity {

    private TextView tvGroupNameHeader, tvMemberCountHeader, tvNoMembers;
    private LinearLayout containerGroupMembers, headerGroup;

    private SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_group);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("My Group");
        }

        session = new SessionManager(this);

        tvGroupNameHeader = findViewById(R.id.tvGroupNameHeader);
        tvMemberCountHeader = findViewById(R.id.tvMemberCountHeader);
        tvNoMembers = findViewById(R.id.tvNoMembers);
        containerGroupMembers = findViewById(R.id.containerGroupMembers);
        headerGroup = findViewById(R.id.headerAssignStudents); // Ensure your activity_my_group.xml header banner has this ID

        // --- DYNAMICALLY PUSH HEADER BELOW STATUS BAR ---
        if (headerGroup != null) {
            ViewCompat.setOnApplyWindowInsetsListener(headerGroup, (v, windowInsets) -> {
                int statusBarHeight = windowInsets.getInsets(WindowInsetsCompat.Type.statusBars()).top;

                // Set top padding to the height of the status bar so content doesn't clip,
                // while the blue background color stretches to the absolute top edge!
                v.setPadding(v.getPaddingLeft(), statusBarHeight + 24, v.getPaddingRight(), v.getPaddingBottom());

                return windowInsets;
            });
        }
        // ------------------------------------------------

        loadGroupMembers();
    }

    private void loadGroupMembers() {
        String loggedInUsername = session.getUsername();

        new Thread(() -> {
            AppDatabase db = AppDatabase.getInstance(getApplicationContext());
            StudentEntity student = db.studentDao().getStudentById(loggedInUsername);
            String groupName = (student != null && student.getLabGroup() != null) ? student.getLabGroup() : "Unassigned";

            if (groupName.equals("Unassigned") || groupName.isEmpty()) {
                runOnUiThread(() -> {
                    tvGroupNameHeader.setText("Unassigned Group");
                    tvMemberCountHeader.setText("Members Registered: 0 / 15");
                    tvNoMembers.setVisibility(View.VISIBLE);
                    tvNoMembers.setText("You are currently not assigned to any group.");
                });
                return;
            }

            List<StudentEntity> members = db.studentDao().getStudentsByLabGroup(groupName);

            runOnUiThread(() -> {
                tvGroupNameHeader.setText(groupName);

                if (members == null || members.isEmpty()) {
                    tvMemberCountHeader.setText("Members Registered: 0 / 15");
                    tvNoMembers.setVisibility(View.VISIBLE);
                    return;
                }

                int totalMembers = members.size();
                tvMemberCountHeader.setText("Members Registered: " + totalMembers + " / 15");
                tvNoMembers.setVisibility(View.GONE);

                containerGroupMembers.removeAllViews();
                LayoutInflater inflater = LayoutInflater.from(this);

                for (StudentEntity member : members) {
                    String name = member.getName();
                    String studentNum = member.getStudentNum();
                    String contact = member.getEmail();

                    View card = inflater.inflate(R.layout.item_group_member, containerGroupMembers, false);

                    TextView tvMemberName = card.findViewById(R.id.tvMemberName);
                    TextView tvMemberNum = card.findViewById(R.id.tvMemberNum);
                    TextView tvMemberContact = card.findViewById(R.id.tvMemberContact);

                    tvMemberName.setText(name != null ? name : "");
                    tvMemberNum.setText("Student No: " + (studentNum != null ? studentNum : ""));
                    tvMemberContact.setText("Email: " + (contact != null ? contact : ""));

                    containerGroupMembers.addView(card);
                }
            });
        }).start();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}