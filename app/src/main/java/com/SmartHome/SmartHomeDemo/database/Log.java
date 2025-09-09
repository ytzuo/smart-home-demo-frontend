package com.SmartHome.SmartHomeDemo.database;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "logs")
public class Log {
    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "log_type")
    private String logType;

    public String getLogDevice() {
        return logDevice;
    }

    public void setLogDevice(String logDevice) {
        this.logDevice = logDevice;
    }

    @ColumnInfo(name = "log_device")
    private String logDevice;

    @ColumnInfo(name = "timestamp")
    private String timestamp;

    @ColumnInfo(name = "log_id")
    private String logId;

    @ColumnInfo(name = "description")
    private String description;

    @ColumnInfo(name = "image_path")
    private String imagePath;

    // 无参构造函数
    public Log() {}

//    public Log(String logType, long timestamp, String logId, String description) {
//        this.logType = logType;
//        this.timestamp = timestamp;
//        this.logId = logId;
//        this.description = description;
//    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getLogType() {
        return logType;
    }

    public void setLogType(String logType) {
        this.logType = logType;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getLogId() {
        return logId;
    }

    public void setLogId(String logId) {
        this.logId = logId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }
}
