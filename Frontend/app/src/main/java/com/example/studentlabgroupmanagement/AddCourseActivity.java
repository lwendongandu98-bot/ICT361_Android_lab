package com.example.studentlabgroupmanagement;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

public class AddCourseActivity extends AppCompatActivity {

    // Variable declarations
    private EditText etCourseCode;
    private EditText etCourseName;
    private Button btnAddCourse;
    private ListView listViewCourses;

    private DatabaseHelper dbHelper;
    private ArrayAdapter<String> adapter;
    private List<String> courseList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_course);

        dbHelper = new DatabaseHelper(this);

        // Bind layout views
        etCourseCode = findViewById(R.id.etCourseCode);
        etCourseName = findViewById(R.id.etCourseName);
        btnAddCourse = findViewById(R.id.btnAddCourse);
        listViewCourses = findViewById(R.id.listViewCourses);

        loadCourseList();

        btnAddCourse.setOnClickListener(v -> {
            String code = etCourseCode.getText().toString().trim();
            String name = etCourseName.getText().toString().trim();

            if (code.isEmpty() || name.isEmpty()) {
                Toast.makeText(AddCourseActivity.this, "Please enter course code and name", Toast.LENGTH_SHORT).show();
                return;
            }

            boolean success = dbHelper.insertCourse(code, name);
            if (success) {
                Toast.makeText(AddCourseActivity.this, "Course added successfully!", Toast.LENGTH_SHORT).show();
                etCourseCode.setText("");
                etCourseName.setText("");
                loadCourseList();
            } else {
                Toast.makeText(AddCourseActivity.this, "Failed to add course", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadCourseList() {
        courseList = dbHelper.getAllCourses();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, courseList);
        listViewCourses.setAdapter(adapter);
    }
}