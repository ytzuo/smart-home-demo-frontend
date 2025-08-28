package com.SmartHome.SmartHomeDemo.fragments.HomeFragment;

public class FurnitureItem {
    private String name;
    private String workingStatus;
    private String status;
    private String time;
    private int imageResource;

    public FurnitureItem(String name, String workingStatus, String status, String time, int imageResource) {
        this.name = name;
        this.workingStatus = workingStatus;
        this.status = status;
        this.time = time;
        this.imageResource = imageResource;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getWorkingStatus() {
        return workingStatus;
    }

    public void setWorkingStatus(String workingStatus) {
        this.workingStatus = workingStatus;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public int getImageResource() {
        return imageResource;
    }

    public void setImageResource(int imageResource) {
        this.imageResource = imageResource;
    }
}
