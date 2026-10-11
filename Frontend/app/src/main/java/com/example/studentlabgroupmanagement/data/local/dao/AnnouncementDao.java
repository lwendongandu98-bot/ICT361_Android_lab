package com.example.studentlabgroupmanagement.data.local.dao;

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

    @Query("SELECT * FROM announcements")
    List<AnnouncementEntity> getAllAnnouncements();
}