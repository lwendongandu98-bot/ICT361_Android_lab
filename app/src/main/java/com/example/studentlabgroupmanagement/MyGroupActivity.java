package com.example.studentlabgroupmanagement;

import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class MyGroupActivity extends AppCompatActivity {

    private TextView tvGroupNameHeader, tvMemberCountHeader, tvNoMembers;
    private LinearLayout containerGroupMembers;

    private DatabaseHelper dbHelper;
    private SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_group);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("My Group");
        }

        dbHelper = new DatabaseHelper(this);
        session = new SessionManager(this);

        tvGroupNameHeader = findViewById(R.id.tvGroupNameHeader);
        tvMemberCountHeader = findViewById(R.id.tvMemberCountHeader);
        tvNoMembers = findViewById(R.id.tvNoMembers);
        containerGroupMembers = findViewById(R.id.containerGroupMembers);

        loadGroupMembers();
    }

    private void loadGroupMembers() {
        String loggedInUsername = session.getUsername();
        String groupName = dbHelper.getStudentGroup(loggedInUsername);

        if (groupName.equals("Unassigned") || groupName.isEmpty()) {
            tvGroupNameHeader.setText("Unassigned Group");
            tvMemberCountHeader.setText("Members Registered: 0 / 15");
            tvNoMembers.setVisibility(View.VISIBLE);
            tvNoMembers.setText("You are currently not assigned to any group.");
            return;
        }

        tvGroupNameHeader.setText(groupName);

        Cursor cursor = dbHelper.getGroupMembers(groupName);

        if (cursor == null || cursor.getCount() == 0) {
            tvMemberCountHeader.setText("Members Registered: 0 / 15");
            tvNoMembers.setVisibility(View.VISIBLE);
            if (cursor != null) cursor.close();
            return;
        }

        int totalMembers = cursor.getCount();
        tvMemberCountHeader.setText("Members Registered: " + totalMembers + " / 15");
        tvNoMembers.setVisibility(View.GONE);

        containerGroupMembers.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(this);

        while (cursor.moveToNext()) {
            String name = cursor.getString(cursor.getColumnIndexOrThrow("student_name"));
            String studentNum = cursor.getString(cursor.getColumnIndexOrThrow("student_number"));
            String contact = cursor.getString(cursor.getColumnIndexOrThrow("contact"));

            View card = inflater.inflate(R.layout.item_group_member, containerGroupMembers, false);

            TextView tvMemberName = card.findViewById(R.id.tvMemberName);
            TextView tvMemberNum = card.findViewById(R.id.tvMemberNum);
            TextView tvMemberContact = card.findViewById(R.id.tvMemberContact);

            tvMemberName.setText(name);
            tvMemberNum.setText("Student No: " + studentNum);
            tvMemberContact.setText("Contact: " + contact);

            containerGroupMembers.addView(card);
        }

        cursor.close();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}