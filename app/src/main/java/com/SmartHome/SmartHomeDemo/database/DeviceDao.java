package com.SmartHome.SmartHomeDemo.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface DeviceDao {
    @Query("SELECT * FROM devices")
    List<Device> getAllDevices();

    @Query("SELECT * FROM devices WHERE id = :id")
    Device getDeviceById(int id);

    @Query("SELECT * FROM devices WHERE device_id = :deviceId")
    Device getDeviceByDeviceId(String deviceId);

    @Insert
    void insertDevice(Device device);

    @Insert
    void insertDevices(List<Device> devices);

    @Query("DELETE FROM devices")
    void deleteAll();

    @Query("UPDATE devices SET device_group = :newGroup WHERE device_group = :oldGroup")
    void updateGroup(String oldGroup, String newGroup);

    @Update
    void updateDevice(Device device);
}
