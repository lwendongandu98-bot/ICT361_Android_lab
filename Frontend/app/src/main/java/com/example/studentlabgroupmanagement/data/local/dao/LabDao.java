package com.example.studentlabgroupmanagement.data.local.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import com.example.studentlabgroupmanagement.data.local.entity.LabEntity;
import java.util.List;

@Dao
public interface LabDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertLab(LabEntity lab);

    @Update
    void updateLab(LabEntity lab);

    @Query("SELECT * FROM labs")
    List<LabEntity> getAllLabs();

    @Query("SELECT * FROM labs WHERE id = :id")
    LabEntity getLabById(int id);

    @Query("UPDATE labs SET status = :status WHERE id = :id")
    void updateLabStatus(int id, String status);

    @Query("DELETE FROM labs WHERE id = :id")
    void deleteLabById(int id);
}