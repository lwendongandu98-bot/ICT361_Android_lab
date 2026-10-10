package com.yourpackage.data.local.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import com.yourpackage.data.local.entities.LabGroupEntity;
import java.util.List;

@Dao
public interface LabGroupDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertGroup(LabGroupEntity group);

    @Query("SELECT * FROM lab_groups")
    List<LabGroupEntity> getAllGroups();

    @Query("DELETE FROM lab_groups WHERE id = :groupId")
    void deleteGroup(int groupId);
}