package com.SmartHome.SmartHomeDemo;


import android.os.Bundle;


import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.SmartHome.SmartHomeDemo.application.SmartHomeApplication;
import com.SmartHome.SmartHomeDemo.database.AppDatabase;
import com.SmartHome.SmartHomeDemo.database.DeviceDao;
import com.SmartHome.SmartHomeDemo.dds.AlertDdsManager;
import com.SmartHome.SmartHomeDemo.fragments.CarFragment.CarFragment;
import com.SmartHome.SmartHomeDemo.fragments.HomeFragment.HomeFragment;
import com.SmartHome.SmartHomeDemo.fragments.LogFragment.LogFragment;
import com.SmartHome.SmartHomeDemo.fragments.LogFragment.LogItem;
import com.SmartHome.SmartHomeDemo.fragments.SettingFragment.SettingFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import idl.SmartDemo03.Alert;


public class MainActivity extends AppCompatActivity {

    private AppDatabase database;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // 获取数据库实例
        SmartHomeApplication app = (SmartHomeApplication) getApplication();
        database = app.getDatabase();
        // 使用数据库
        DeviceDao deviceDao = database.deviceDao();

        // 设置Alert监听器
        AlertDdsManager alertDdsManager = app.getAlertDdsManager();
        alertDdsManager.setOnAlertReceivedListener(new AlertDdsManager.OnAlertReceivedListener() {
            @Override
            public void onAlertReceived(Alert alert) {
                addAlertToLog(alert);
            }
        });

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);

        // 只有在savedInstanceState为null时才加载初始Fragment
        // 避免旋转屏幕等配置更改时重复加载
        if (savedInstanceState == null) {
            loadFragment(new CarFragment());
        }

        bottomNav.setOnNavigationItemSelectedListener(item -> {
            Fragment selected = null;
            int id = item.getItemId();
            if (id == R.id.nav_car) {
                selected = new CarFragment();
            } else if (id == R.id.nav_home) {
                selected = new HomeFragment();
            } else if (id == R.id.nav_log) {
                selected = new LogFragment();
            } else if (id == R.id.nav_setting) {
                selected = new SettingFragment();
            }
            return loadFragment(selected);
        });
    }

    private boolean loadFragment(Fragment fragment) {
        if (fragment != null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .commit();
            return true;
        }
        return false;
    }

    // 提供获取数据库实例的方法
    public AppDatabase getDatabase() {
        return database;
    }

    // 添加alert到日志
    private void addAlertToLog(Alert alert) {
        // 创建时间戳
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        String currentTime = sdf.format(new Date());

        // 创建LogItem
        LogItem logItem = new LogItem(
                alert.level,                    // 日志类型 (INFO/WARN/ALERT)
                currentTime,                    // 当前时间
                "ID: " + alert.alert_id,        // Alert ID
                alert.description               // Alert 描述
        );

        // 直接通过FragmentManager找到当前的LogFragment（如果存在）
        LogFragment logFragment = (LogFragment) getSupportFragmentManager().findFragmentByTag("LogFragment");
        if (logFragment != null && logFragment.isVisible()) {
            logFragment.addLogItem(logItem);
        }

        // 注意：如果LogFragment当前未加载，数据会在下次加载时显示，
        // 因为LogViewModel会保持数据状态
    }
}
