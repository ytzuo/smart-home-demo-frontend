package com.SmartHome.SmartHomeDemo.fragments.HomeFragment;

public class FurnitureItem {
    private String name;
    private String workingStatus;
    private String status;
    private String time;
    private int imageResource;
    private float acTemp;
    private String acStatus; //四位长的字符串, 类似1001, 代表开关
    private boolean lightOn;
    private float lightPercent;


    public float getAcTemp() {
        return acTemp;
    }

    public void setAcTemp(float acTemp) {
        this.acTemp = acTemp;
    }

    public String getAcStatus() {
        return acStatus;
    }

    public void setAcStatus(String acStatus) {
        this.acStatus = acStatus;
    }

    public boolean isLightOn() {
        return lightOn;
    }

    public void setLightOn(boolean lightOn) {
        this.lightOn = lightOn;
    }

    public float getLightPercent() {
        return lightPercent;
    }

    public void setLightPercent(float lightPercent) {
        this.lightPercent = lightPercent;
    }

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
