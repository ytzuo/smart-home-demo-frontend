package com.SmartHome.SmartHomeDemo.fragments.LogFragment;

public class LogItem {
    private String logType;
    private String logTime;
    private String logID;
    private String logMsg;

    public LogItem(String logType, String logTime, String logID, String logMsg) {
        this.logType = logType;
        this.logTime = logTime;
        this.logID = logID;
        this.logMsg = logMsg;
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
}
