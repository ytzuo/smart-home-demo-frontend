package com.SmartHome.SmartHomeDemo.fragments.LogFragment;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import java.util.ArrayList;
import java.util.List;

public class LogViewModel extends ViewModel {
    private MutableLiveData<List<LogItem>> logListLiveData;
    private List<LogItem> logList;

    public LogViewModel() {
        logList = new ArrayList<>();
        logListLiveData = new MutableLiveData<>(logList);

        // 初始化测试数据
        logList.add(new LogItem("INFO", "2025-8-28 19:53", "log01", "测试日志1"));
        logList.add(new LogItem("WARN", "2025-8-28 19:53", "log02", "测试日志2"));
        logList.add(new LogItem("ALERT", "2025-8-28 19:53", "log03", "测试日志3"));
        logListLiveData.setValue(logList);
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
}
