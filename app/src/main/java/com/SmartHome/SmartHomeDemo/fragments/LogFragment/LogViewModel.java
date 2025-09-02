package com.SmartHome.SmartHomeDemo.fragments.LogFragment;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.SmartHome.SmartHomeDemo.application.SmartHomeApplication;
import com.SmartHome.SmartHomeDemo.database.AppDatabase;
import com.SmartHome.SmartHomeDemo.database.Log;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class LogViewModel extends AndroidViewModel {
    private MutableLiveData<List<LogItem>> logListLiveData;
    private List<LogItem> logList;
    private AppDatabase database;

    public LogViewModel(Application application) {
        super(application);
        logList = new ArrayList<>();
        logListLiveData = new MutableLiveData<>(logList);

        // 初始化测试数据
        logList.add(new LogItem("INFO", "2025-8-28 19:53", "log01", "测试日志1"));
        logList.add(new LogItem("WARN", "2025-8-28 19:53", "log02", "测试日志2"));
        logList.add(new LogItem("ALERT", "2025-8-28 19:53", "log03", "测试日志3"));
        logListLiveData.setValue(logList);

        //实测数据库时再启用
        //database = ((SmartHomeApplication) application).getDatabase();

        // 从数据库加载日志
        //loadLogsFromDatabase();
    }

    public LiveData<List<LogItem>> getLogListLiveData() {
        return logListLiveData;
    }

    public void addLogItem(LogItem logItem) {
        logList.add(0, logItem); // 添加到列表开头
        logListLiveData.setValue(logList);
    }

    public void updateLogList(List<LogItem> newLogList) {
        logList.clear();
        logList.addAll(newLogList);
        logListLiveData.setValue(logList);
    }

    public void removeLogItem(int position) {
        if (position >= 0 && position < logList.size()) {
            logList.remove(position);
            logListLiveData.setValue(logList);
        }
    }

    private void loadLogsFromDatabase() {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            List<Log> logs = database.logDao().getAllLogs();
            List<LogItem> logItems = new ArrayList<>();

            for (Log log : logs) {
                String time = log.getTimestamp();
                LogItem item = new LogItem(
                        log.getLogType(),
                        time,
                        log.getLogId(),
                        log.getDescription()
                );
                logItems.add(item);
            }
            logListLiveData.postValue(logItems);
        });
    }
}
