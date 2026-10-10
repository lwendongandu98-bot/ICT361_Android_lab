package com.example.studentlabgroupmanagement.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.studentlabgroupmanagement.data.local.entity.StudentEntity;
import java.util.List;

@Dao
public interface StudentDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertStudent(StudentEntity student);

    @Update
    void updateStudent(StudentEntity student);

    @Query("SELECT * FROM students WHERE studentId = :studentId")
    StudentEntity getStudentById(String studentId);

    @Query("SELECT * FROM students")
    LiveData<List<StudentEntity>> getAllStudentsLive();
}