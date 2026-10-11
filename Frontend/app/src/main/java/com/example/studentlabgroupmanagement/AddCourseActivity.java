package com.example.studentlabgroupmanagement;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.studentlabgroupmanagement.data.local.AppDatabase;
import com.example.studentlabgroupmanagement.data.local.entity.CourseEntity;

import java.util.ArrayList;
import java.util.List;

public class AddCourseActivity extends AppCompatActivity {

    // Variable declarations
    private EditText etCourseCode;
    private EditText etCourseName;
    private Button btnAddCourse;
    private ListView listViewCourses;

    private ArrayAdapter<String> adapter;
    private List<String> courseList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_course);

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

            // Perform Room database insertion on a background thread
            new Thread(() -> {
                try {
                    CourseEntity course = new CourseEntity(code, name);
                    AppDatabase.getInstance(getApplicationContext()).courseDao().insertCourse(course);

                    runOnUiThread(() -> {
                        Toast.makeText(AddCourseActivity.this, "Course added successfully!", Toast.LENGTH_SHORT).show();
                        etCourseCode.setText("");
                        etCourseName.setText("");
                        loadCourseList();
                    });
                } catch (Exception e) {
                    runOnUiThread(() -> {
                        Toast.makeText(AddCourseActivity.this, "Failed to add course: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
                }
            }).start();
        });
    }

    private void loadCourseList() {
        // Fetch courses from Room database on a background thread
        new Thread(() -> {
            List<CourseEntity> entities = AppDatabase.getInstance(getApplicationContext()).courseDao().getAllCourses();
            List<String> displayList = new ArrayList<>();
            if (entities != null) {
                for (CourseEntity entity : entities) {
                    displayList.add(entity.getCourseCode() + " - " + entity.getCourseName());
                }
            }

            // Update UI on the main thread
            runOnUiThread(() -> {
                courseList = displayList;
                adapter = new ArrayAdapter<>(AddCourseActivity.this, android.R.layout.simple_list_item_1, courseList);
                listViewCourses.setAdapter(adapter);
            });
        }).start();
    }
}