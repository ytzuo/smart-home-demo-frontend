package com.SmartHome.SmartHomeDemo.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface DeviceDao {
    @Query("SELECT * FROM devices")
    List<Device> getAllDevices();

    @Query("SELECT * FROM devices WHERE id = :id")
    Device getDeviceById(int id);

    @Query("SELECT * FROM devices WHERE device_id = :deviceId")
    List<Device> getDevicesByDeviceId(String deviceId);

    @Insert
    void insertDevice(Device device);

    @Insert
    void insertDevices(List<Device> devices);

}