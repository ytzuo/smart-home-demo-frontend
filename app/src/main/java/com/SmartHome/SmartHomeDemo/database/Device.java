package com.SmartHome.SmartHomeDemo.database;

import androidx.room.ColumnInfo;
import androidx.room.Entity;

@Entity(tableName = "devices")
public class Device {
        @ColumnInfo(name = "device_id")
        private String deviceId;
        @ColumnInfo(name = "device_type")
        private String deviceType;
        // 无参构造函数
        public Device() {}

        public Device(String deviceId, String deviceType) {
                this.deviceId = deviceId;
                this.deviceType = deviceType;
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
}