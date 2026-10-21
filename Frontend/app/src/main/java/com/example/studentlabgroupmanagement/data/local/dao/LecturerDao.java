package com.example.studentlabgroupmanagement.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.studentlabgroupmanagement.data.local.entity.LecturerEntity;

import java.util.List;

@Dao
public interface LecturerDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertLecturer(LecturerEntity lecturer);

    @Query("SELECT * FROM lecturers")
    LiveData<List<LecturerEntity>> getAllLecturers();

    @Query("SELECT * FROM lecturers WHERE employee_number = :employeeNumber")
    LecturerEntity getLecturerByEmployeeNo(String employeeNumber);

    @Query("DELETE FROM lecturers WHERE employee_number = :employeeNumber")
    void deleteLecturer(String employeeNumber);
}