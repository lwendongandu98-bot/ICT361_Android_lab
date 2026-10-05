package com.example.studentregistrationapp.data.model;

public class LabGroup {
    private String groupId;
    private String groupName;
    private int capacity;
    private int currentEnrolled;

    public LabGroup() {}

    public LabGroup(String groupId, String groupName, int capacity, int currentEnrolled) {
        this.groupId = groupId;
        this.groupName = groupName;
        this.capacity = capacity;
        this.currentEnrolled = currentEnrolled;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public int getCurrentEnrolled() {
        return currentEnrolled;
    }

    public void setCurrentEnrolled(int currentEnrolled) {
        this.currentEnrolled = currentEnrolled;
    }

    public boolean isFull() {
        return currentEnrolled >= capacity;
    }
}
