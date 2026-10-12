package com.example.studentlabgroupmanagement;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.studentlabgroupmanagement.data.local.entity.CourseEntity;
import com.example.studentlabgroupmanagement.data.repository.GroupRepository;
import com.example.studentlabgroupmanagement.data.worker.SyncHelper;
import com.example.studentlabgroupmanagement.ui.viewmodel.CoursesViewModel;

import java.util.ArrayList;
import java.util.List;

public class AddCourseActivity extends AppCompatActivity {

    private EditText etCourseCode;
    private EditText etCourseName;
    private Button btnAddCourse;
    private ListView listViewCourses;

    private CoursesViewModel coursesViewModel;
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

        // Initialize ViewModel
        coursesViewModel = new ViewModelProvider(this).get(CoursesViewModel.class);

        // Initialize list and adapter
        courseList = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, courseList);
        listViewCourses.setAdapter(adapter);

        // --- CHECK SERVER CONNECTION ON STARTUP ---
        NetworkCheckHelper.checkConnection(this);
        // ------------------------------------------

        // Observe courses reactively via LiveData from ViewModel
        coursesViewModel.getAllCourses().observe(this, courseEntities -> {
            courseList.clear();
            if (courseEntities != null) {
                for (CourseEntity entity : courseEntities) {
                    courseList.add(entity.getCourseCode() + " - " + entity.getCourseName());
                }
            }
            adapter.notifyDataSetChanged();
        });

        btnAddCourse.setOnClickListener(v -> {
            String code = etCourseCode.getText().toString().trim();
            String name = etCourseName.getText().toString().trim();

            if (code.isEmpty() || name.isEmpty()) {
                Toast.makeText(AddCourseActivity.this, "Please enter course code and name", Toast.LENGTH_SHORT).show();
                return;
            }

            // Use ViewModel to handle creation, MySQL server sync, and local Room caching
            coursesViewModel.createCourse(code, name, new GroupRepository.OnOperationCallback() {
                @Override
                public void onSuccess(String message) {
                    // --- TRIGGER UNIFIED WORKMANAGER BACKGROUND SYNC ---
                    SyncHelper.triggerSync(AddCourseActivity.this);
                    // --------------------------------------------------

                    runOnUiThread(() -> {
                        Toast.makeText(AddCourseActivity.this, message, Toast.LENGTH_SHORT).show();
                        etCourseCode.setText("");
                        etCourseName.setText("");
                    });
                }

                @Override
                public void onError(String error) {
                    runOnUiThread(() -> {
                        Toast.makeText(AddCourseActivity.this, "Error: " + error, Toast.LENGTH_SHORT).show();
                    });
                }
            });
        });
    }
}