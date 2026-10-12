package com.example.studentlabgroupmanagement.ui.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import com.example.studentlabgroupmanagement.data.local.entity.CourseEntity;
import com.example.studentlabgroupmanagement.data.repository.CourseRepository;
import com.example.studentlabgroupmanagement.data.repository.GroupRepository;
import java.util.List;

public class CoursesViewModel extends AndroidViewModel {
    private CourseRepository repository;
    private LiveData<List<CourseEntity>> allCourses;

    public CoursesViewModel(@NonNull Application application) {
        super(application);
        repository = new CourseRepository(application);
        allCourses = repository.getAllCourses();
    }

    public LiveData<List<CourseEntity>> getAllCourses() {
        return allCourses;
    }

    public void createCourse(String courseCode, String courseName, GroupRepository.OnOperationCallback callback) {
        CourseEntity course = new CourseEntity();
        course.setCourseCode(courseCode);
        course.setCourseName(courseName);
        repository.insertCourse(course, callback);
    }
}