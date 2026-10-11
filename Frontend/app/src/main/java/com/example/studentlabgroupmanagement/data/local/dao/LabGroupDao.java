package com.example.studentlabgroupmanagement.data.local.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import com.example.studentlabgroupmanagement.data.local.entity.LabGroupEntity;
import java.util.List;

@Dao
public interface LabGroupDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertGroup(LabGroupEntity group);

    @Query("SELECT * FROM lab_groups")
    List<LabGroupEntity> getAllGroups();

    @Query("SELECT * FROM lab_groups WHERE group_name = :groupName")
    LabGroupEntity getGroupByName(String groupName);

    @Query("SELECT * FROM lab_groups WHERE group_name LIKE :query")
    List<LabGroupEntity> searchGroups(String query);

    @Query("DELETE FROM lab_groups WHERE id = :groupId")
    void deleteGroup(int groupId);

    @Query("DELETE FROM lab_groups WHERE group_name = :groupName")
    void deleteGroupByName(String groupName);
}