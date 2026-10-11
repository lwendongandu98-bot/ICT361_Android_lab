package com.example.studentlabgroupmanagement.data.local.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "announcements")
public class AnnouncementEntity {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private String title;
    private String message;
    private String date;

    public AnnouncementEntity() {}

    public AnnouncementEntity(String title, String message, String date) {
        this.title = title;
        this.message = message;
        this.date = date;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }
}