package com.yourpackage.data.local.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import com.yourpackage.data.local.entities.StudentEntity;
import java.util.List;

@Dao
public interface StudentDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertStudent(StudentEntity student);

    @Query("SELECT * FROM students")
    List<StudentEntity> getAllStudents();

    @Query("SELECT * FROM students WHERE username = :username AND password = :password LIMIT 1")
    StudentEntity login(String username, String password);
}