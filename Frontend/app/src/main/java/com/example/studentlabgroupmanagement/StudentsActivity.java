package com.example.studentlabgroupmanagement;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class StudentsActivity extends AppCompatActivity {

    private RecyclerView rvStudents;
    private TextView tvEmpty;
    private SearchView searchViewStudents;
    private DatabaseHelper dbHelper;
    private StudentAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_students);

        Toolbar toolbar = findViewById(R.id.toolbarStudents);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            }
            toolbar.setNavigationOnClickListener(v -> onBackPressed());
        }

        rvStudents = findViewById(R.id.rvStudents);
        tvEmpty = findViewById(R.id.tvEmpty);
        searchViewStudents = findViewById(R.id.searchViewStudents);
        dbHelper = new DatabaseHelper(this);

        rvStudents.setLayoutManager(new LinearLayoutManager(this));

        loadStudents();

        // Search bar query listener
        searchViewStudents.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (adapter != null) {
                    adapter.filter(query);
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (adapter != null) {
                    adapter.filter(newText);
                }
                return false;
            }
        });
    }

    private void loadStudents() {
        List<Student> students = dbHelper.getAllStudents();

        if (students.isEmpty()) {
            tvEmpty.setVisibility(View.VISIBLE);
            rvStudents.setVisibility(View.GONE);
        } else {
            tvEmpty.setVisibility(View.GONE);
            rvStudents.setVisibility(View.VISIBLE);
            adapter = new StudentAdapter(students);
            rvStudents.setAdapter(adapter);
        }
    }
}