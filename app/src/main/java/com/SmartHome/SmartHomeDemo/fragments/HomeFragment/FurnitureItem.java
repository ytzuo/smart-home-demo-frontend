package com.SmartHome.SmartHomeDemo.fragments.HomeFragment;

import java.io.Serializable;
public class FurnitureItem implements Serializable {
    private static final long serialVersionUID = 1L;
    private String deviceId;
    private String deviceType; //"air_conditioner" 或 "light"
    private String workingStatus;
    private String status;
    private String time;
    private int imageResource;
    private float acTemp; //空调温度
    private String switchStatus; //八位长的字符串, 类似00000000, 代表家具的功能开关状态
    private boolean lightOn;
    private float lightPercent;


    public float getAcTemp() {
        return acTemp;
    }

    public void setAcTemp(float acTemp) {
        this.acTemp = acTemp;
    }

    public String getSwitchStatus() {
        return switchStatus;
    }

    public void setSwitchStatus(String acStatus) {
        this.switchStatus = acStatus;
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

    public FurnitureItem(String name, String deviceType, String workingStatus, String status, String time, int imageResource,
                         float acTemp, String switchStatus, boolean lightOn, float lightPercent) {
        this.deviceId = name;
        this.deviceType = deviceType;
        this.workingStatus = workingStatus;
        this.status = status;
        this.time = time;
        this.imageResource = imageResource;
        this.acTemp = acTemp;
        this.switchStatus = switchStatus;
        this.lightOn = lightOn;
        this.lightPercent = lightPercent;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
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

    public String getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

}
