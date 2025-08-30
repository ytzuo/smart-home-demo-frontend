package com.SmartHome.SmartHomeDemo.application;

import android.app.Application;
import com.SmartHome.SmartHomeDemo.database.AppDatabase;
import com.SmartHome.SmartHomeDemo.dds.BaseDdsManager;
import com.SmartHome.SmartHomeDemo.dds.CommandDdsManager;
import com.SmartHome.SmartHomeDemo.dds.AlertDdsManager;
import com.SmartHome.SmartHomeDemo.dds.HomeStatusDdsManager;


/*
    统一管理数据库和所有DDS相关
    统一管理数据库和所有DDS相关
    统一管理数据库和所有DDS相关
 */
public class SmartHomeApplication extends Application {
    private AppDatabase database;
    private BaseDdsManager baseDdsManager;
    private CommandDdsManager commandDdsManager;
    private AlertDdsManager alertDdsManager;
    private HomeStatusDdsManager homeStatusDdsManager;

    @Override
    public void onCreate() {
        super.onCreate();

        // 初始化数据库
        database = AppDatabase.getDatabase(this);

        // 初始化DDS
        initializeDDS();
    }

    private void initializeDDS() {
        baseDdsManager = BaseDdsManager.getInstance(this);
        baseDdsManager.initialize();

        commandDdsManager = new CommandDdsManager();
        commandDdsManager.initialize(baseDdsManager);

        alertDdsManager = new AlertDdsManager();
        alertDdsManager.initialize(baseDdsManager);

        homeStatusDdsManager = new HomeStatusDdsManager();
        homeStatusDdsManager.initialize(baseDdsManager);
    }

    /*
        // 在Activity或Fragment中获取DDS管理器
        SmartHomeApplication app = (SmartHomeApplication) getApplication();
        CommandDdsManager commandManager = app.getCommandDdsManager();
        AlertDdsManager alertManager = app.getAlertDdsManager();
        // 使用这些管理器进行发布或订阅操作
     */
    public AppDatabase getDatabase() {
        return database;
    }

    public CommandDdsManager getCommandDdsManager() {
        return commandDdsManager;
    }

    public AlertDdsManager getAlertDdsManager() {
        return alertDdsManager;
    }

    public HomeStatusDdsManager getHomeStatusDdsManager() {
        return homeStatusDdsManager;
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        // 清理DDS资源
        if (baseDdsManager != null) {
            baseDdsManager.cleanup();
        }
    }
}
