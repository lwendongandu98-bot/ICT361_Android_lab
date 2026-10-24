package com.example.studentlabgroupmanagement.data.local.dao;

import androidx.lifecycle.LiveData;
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

    // Reactive LiveData query for UI ViewModels
    @Query("SELECT * FROM lab_groups")
    LiveData<List<LabGroupEntity>> getAllGroups();

    // Synchronous query for background workers
    @Query("SELECT * FROM lab_groups")
    List<LabGroupEntity> getAllGroupsSync();

    @Query("SELECT * FROM lab_groups WHERE group_name = :groupName")
    LabGroupEntity getGroupByName(String groupName);

    @Query("SELECT * FROM lab_groups WHERE group_name LIKE :query")
    List<LabGroupEntity> searchGroups(String query);

    @Query("DELETE FROM lab_groups WHERE id = :groupId")
    void deleteGroup(int groupId);

    @Query("DELETE FROM lab_groups WHERE group_name = :groupName")
    void deleteGroupByName(String groupName);
}