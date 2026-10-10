package com.example.studentregistrationapp.data.local.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import com.example.studentregistrationapp.data.local.entities.LabEntity;
import java.util.List;

@Dao
public interface LabDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertLab(LabEntity lab);

    @Query("SELECT * FROM labs")
    List<LabEntity> getAllLabs();
}