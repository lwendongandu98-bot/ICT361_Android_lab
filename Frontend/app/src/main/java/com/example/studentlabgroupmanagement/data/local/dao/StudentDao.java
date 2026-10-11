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

    @Query("SELECT * FROM users WHERE username = :username")
    StudentEntity getStudentById(String username);

    @Query("SELECT * FROM users")
    LiveData<List<StudentEntity>> getAllStudentsLive();
}