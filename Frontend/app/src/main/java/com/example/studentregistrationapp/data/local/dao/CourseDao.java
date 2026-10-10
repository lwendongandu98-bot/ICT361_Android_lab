package com.example.studentregistrationapp.data.local.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import com.example.studentregistrationapp.data.local.entities.CourseEntity;
import java.util.List;

@Dao
public interface CourseDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertCourse(CourseEntity course);

    @Query("SELECT * FROM courses")
    List<CourseEntity> getAllCourses();

    @Query("DELETE FROM courses WHERE id = :courseId")
    void deleteCourse(int courseId);
}