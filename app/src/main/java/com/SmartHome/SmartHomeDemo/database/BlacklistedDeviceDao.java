package com.SmartHome.SmartHomeDemo.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import java.util.List;

@Dao
public interface BlacklistedDeviceDao {
    @Query("SELECT * FROM blacklisted_devices")
    List<BlacklistedDevice> getAllBlacklistedDevices();

    @Query("SELECT * FROM blacklisted_devices WHERE device_id = :deviceId")
    BlacklistedDevice getBlacklistedDeviceById(String deviceId);

    @Insert
    void insert(BlacklistedDevice device);

    @Delete
    void delete(BlacklistedDevice device);

    @Query("DELETE FROM blacklisted_devices WHERE device_id = :deviceId")
    void deleteByDeviceId(String deviceId);

    @Query("DELETE FROM blacklisted_devices")
    void deleteAll();
}
