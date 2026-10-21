package com.example.studentlabgroupmanagement.data.local.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.google.gson.annotations.SerializedName;

@Entity(tableName = "announcements")
public class AnnouncementEntity {
    @PrimaryKey(autoGenerate = true)
    private int id;

    @SerializedName(value = "title", alternate = {"announcementTitle", "heading"})
    private String title;

    @SerializedName(value = "message", alternate = {"content", "body", "announcementMessage"})
    private String message;

    @SerializedName(value = "date", alternate = {"created_at", "createdAt", "timestamp"})
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