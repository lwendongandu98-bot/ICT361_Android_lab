package com.example.studentlabgroupmanagement.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import com.example.studentlabgroupmanagement.data.local.entity.CourseEntity;
import java.util.List;

@Dao
public interface CourseDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertCourse(CourseEntity course);

    // Reactive LiveData query for UI ViewModels
    @Query("SELECT * FROM courses")
    LiveData<List<CourseEntity>> getAllCourses();

    // Synchronous query for background workers
    @Query("SELECT * FROM courses")
    List<CourseEntity> getAllCoursesSync();

    @Query("DELETE FROM courses WHERE id = :courseId")
    void deleteCourse(int courseId);
}