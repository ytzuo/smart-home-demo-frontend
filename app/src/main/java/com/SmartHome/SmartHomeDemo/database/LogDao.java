package com.SmartHome.SmartHomeDemo.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Delete;
import androidx.room.Update;

import java.util.List;

@Dao
public interface LogDao {
    @Query("SELECT * FROM logs ORDER BY timestamp DESC")
    List<Log> getAllLogs();

    @Query("SELECT * FROM logs WHERE id = :id")
    Log getLogById(int id);

    @Query("SELECT * FROM logs WHERE log_type = :logType ORDER BY timestamp DESC")
    List<Log> getLogsByType(String logType);

    @Query("SELECT * FROM logs ORDER BY timestamp DESC LIMIT :limit")
    List<Log> getLatestLogs(int limit);

    @Insert
    void insertLog(Log log);

    @Insert
    void insertLogs(List<Log> logs);

    @Update
    void updateLog(Log log);

    @Delete
    void deleteLog(Log log);

    @Query("DELETE FROM logs")
    void deleteAllLogs();
}
