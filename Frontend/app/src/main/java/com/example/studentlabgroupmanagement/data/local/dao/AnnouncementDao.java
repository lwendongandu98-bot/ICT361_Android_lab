package com.example.studentlabgroupmanagement.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.studentlabgroupmanagement.data.local.entity.AnnouncementEntity;

import java.util.List;

@Dao
public interface AnnouncementDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAnnouncement(AnnouncementEntity announcement);

    // Reactive LiveData query for UI ViewModels
    @Query("SELECT * FROM announcements")
    LiveData<List<AnnouncementEntity>> getAllAnnouncements();

    // Synchronous query for background threads / activities
    @Query("SELECT * FROM announcements")
    List<AnnouncementEntity> getAllAnnouncementsSync();
}