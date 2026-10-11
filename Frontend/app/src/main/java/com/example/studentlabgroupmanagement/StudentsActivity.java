package com.example.studentlabgroupmanagement;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.studentlabgroupmanagement.data.local.AppDatabase;
import com.example.studentlabgroupmanagement.data.local.entity.StudentEntity;

import java.util.ArrayList;
import java.util.List;

public class StudentsActivity extends AppCompatActivity {

    private RecyclerView rvStudents;
    private TextView tvEmpty;
    private SearchView searchViewStudents;
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

        rvStudents.setLayoutManager(new LinearLayoutManager(this));

        loadStudents();

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
        new Thread(() -> {
            List<StudentEntity> entities = AppDatabase.getInstance(getApplicationContext())
                    .studentDao()
                    .getAllStudents();

            List<Student> students = new ArrayList<>();
            if (entities != null) {
                for (StudentEntity e : entities) {
                    // Fixed to match the 2 arguments expected by the Student model constructor
                    students.add(new Student(e.getName(), e.getStudentNum()));
                }
            }

            runOnUiThread(() -> {
                if (students.isEmpty()) {
                    tvEmpty.setVisibility(View.VISIBLE);
                    rvStudents.setVisibility(View.GONE);
                } else {
                    tvEmpty.setVisibility(View.GONE);
                    rvStudents.setVisibility(View.VISIBLE);
                    adapter = new StudentAdapter(students);
                    rvStudents.setAdapter(adapter);
                }
            });
        }).start();
    }
}