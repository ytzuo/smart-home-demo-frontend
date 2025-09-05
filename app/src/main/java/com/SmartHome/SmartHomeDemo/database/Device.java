package com.SmartHome.SmartHomeDemo.database;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "devices")
public class Device {
        @PrimaryKey(autoGenerate = true)
        private int id;
        @ColumnInfo(name = "device_id")
        private String deviceId;
        @ColumnInfo(name = "device_type")
        private String deviceType;
        @ColumnInfo(name = "device_group")
        private String deviceGroup;

        // 无参构造函数
        public Device() {}

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

        public int getId() {
                return id;
        }

        public void setId(int id) {
                this.id = id;
        }

        public String getDeviceGroup() {
                return deviceGroup;
        }

        public void setDeviceGroup(String deviceGroup) {
                this.deviceGroup = deviceGroup;
        }
}
