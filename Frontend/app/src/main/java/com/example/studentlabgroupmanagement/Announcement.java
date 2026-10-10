package com.example.studentlabgroupmanagement;

public class Announcement {
    private final String title;
    private final String message;
    private final String datePosted;

    public Announcement(String title, String message, String datePosted) {
        this.title = title;
        this.message = message;
        this.datePosted = datePosted;
    }

    public String getTitle() { return title; }
    public String getMessage() { return message; }
    public String getDatePosted() { return datePosted; }
}