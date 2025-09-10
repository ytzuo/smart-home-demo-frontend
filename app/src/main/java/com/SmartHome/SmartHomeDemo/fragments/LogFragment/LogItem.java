package com.SmartHome.SmartHomeDemo.fragments.LogFragment;

import androidx.annotation.NonNull;

public class LogItem {
    private String logType;
    private String logTime;
    private String logID;
    private String logMsg;
    private String imagePath;
    private String logDevice;

    public LogItem(String logType, String logTime, String logID, String logMsg, String device) {
        this.logType = logType;
        this.logTime = logTime;
        this.logID = logID;
        this.logMsg = logMsg;
        this.logDevice = device;
    }

    public String getLogType() {
        return logType;
    }

    public String getLogTime() {
        return logTime;
    }

    public String getLogID() {
        return logID;
    }

    public void setLogType(String logType) {
        this.logType = logType;
    }

    public void setLogTime(String logTime) {
        this.logTime = logTime;
    }

    public void setLogID(String logID) {
        this.logID = logID;
    }

    public String getLogMsg() {
        return logMsg;
    }

    public void setLogMsg(String logMsg) {
        this.logMsg = logMsg;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public String getLogDevice() {
        return logDevice;
    }

    public void setLogDevice(String logDevice) {
        this.logDevice = logDevice;
    }
}
