package com.SmartHome.SmartHomeDemo.database;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "blacklisted_devices")
public class BlacklistedDevice {
    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "device_id")
    private String deviceId;

    @ColumnInfo(name = "device_type")
    private String deviceType;

    @ColumnInfo(name = "timestamp")
    private String timestamp;

    // 无参构造函数
    public BlacklistedDevice() {}

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }


    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
