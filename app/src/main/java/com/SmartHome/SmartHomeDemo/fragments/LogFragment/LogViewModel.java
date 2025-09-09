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
import java.util.concurrent.atomic.AtomicBoolean;

public class LogViewModel extends AndroidViewModel {
    private MutableLiveData<List<LogItem>> logListLiveData;
    private List<LogItem> logList;
    private AppDatabase database;
    private AtomicBoolean isDataLoaded = new AtomicBoolean(false);

    public LogViewModel(Application application) {
        super(application);
        logList = new ArrayList<>();
        logListLiveData = new MutableLiveData<>(logList);
        //实测数据库时再启用
        database = ((SmartHomeApplication) application).getDatabase();


//        AppDatabase.databaseWriteExecutor.execute(() -> {
//            List<Log> logs = database.logDao().getAllLogs();
//            if(logs.isEmpty()) {
//                String[] types = {"INFO", "WARN", "ALERT"};
//                String[] times = {"2025-8-28 19:53", "2025-8-28 19:53", "2025-8-28 19:53"};
//                String[] ids = {"log01", "log02", "log03"};
//                String[] msg = {"测试日志1", "测试日志2", "测试日志3"};
//                for(int i = 0; i < 3; i++) {
//                    Log newLog = new Log();
//                    newLog.setLogType(types[i]);
//                    newLog.setTimestamp(times[i]);
//                    newLog.setLogId(ids[i]);
//                    newLog.setDescription(msg[i]);
//                    database.logDao().insertLog(newLog);
//                }
//            }
//        });
//        // 初始化测试数据
//        logList.add(new LogItem("INFO", "2025-8-28 19:53", "log01", "测试日志1"));
//        logList.add(new LogItem("WARN", "2025-8-28 19:53", "log02", "测试日志2"));
//        logList.add(new LogItem("ALERT", "2025-8-28 19:53", "log03", "测试日志3"));
//        logListLiveData.setValue(logList);

        // 从数据库加载日志
        loadLogsFromDatabase();
    }

    public LiveData<List<LogItem>> getLogListLiveData() {
        return logListLiveData;
    }

    public void addLogItem(LogItem logItem) {
        // 确保在主线程中操作
        if (isDataLoaded.get()) {
            // 数据已加载，直接添加
            logList.add(0, logItem);
            logListLiveData.postValue(logList);
        } else {
            // 数据尚未加载完成，等待加载完成后再添加
            AppDatabase.databaseWriteExecutor.execute(() -> {
                // 等待数据加载完成
                while (!isDataLoaded.get()) {
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        return;
                    }
                }

                // 切换到主线程更新UI
                logList.add(0, logItem);
                logListLiveData.postValue(logList);
            });
        }
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
                        log.getDescription(),
                        log.getLogDevice()
                );
                logItems.add(item);
            }
            // 将数据库中的日志添加到当前列表的末尾
            logList.addAll(logItems);
            logListLiveData.postValue(logList);
            isDataLoaded.set(true);
        });
    }
}
