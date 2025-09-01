package com.SmartHome.SmartHomeDemo;


import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;


import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.SmartHome.SmartHomeDemo.application.SmartHomeApplication;
import com.SmartHome.SmartHomeDemo.database.AppDatabase;
import com.SmartHome.SmartHomeDemo.database.Device;
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
import java.util.concurrent.Executors;

import idl.SmartDemo03.Alert;
import idl.SmartDemo03.Presence;


public class MainActivity extends AppCompatActivity {

    private AppDatabase database;
    private SmartHomeApplication app;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // 获取数据库实例
        app = (SmartHomeApplication) getApplication();
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

        // 设置未知设备监听器
        app.setOnUnknownDeviceListener(new SmartHomeApplication.OnUnknownDeviceListener() {
            @Override
            public void onUnknownDeviceDetected(Presence presence) {
                showNewDeviceDialog(presence);
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

    private void showNewDeviceDialog(Presence presence) {
        // 加载对话框布局
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.window_match, null);

        // 更新对话框中的文本
        TextView deviceIdText = dialogView.findViewById(R.id.match_device_id);
        TextView deviceTypeText = dialogView.findViewById(R.id.match_device_type);

        if (deviceIdText != null) {
            deviceIdText.setText(getString(R.string.device_id) + ": " + presence.deviceId);
        }

        if (deviceTypeText != null) {
            deviceTypeText.setText(getString(R.string.device_type) + ": " + presence.deviceType);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();

        // 设置按钮点击事件
        if (dialogView.findViewById(R.id.add_new_done) != null) {
            dialogView.findViewById(R.id.add_new_done).setOnClickListener(v -> {
                // 用户点击确认，将设备插入数据库
                addDeviceToDatabase(presence);
                dialog.dismiss();
            });
        }

        if (dialogView.findViewById(R.id.add_new_cancel) != null) {
            dialogView.findViewById(R.id.add_new_cancel).setOnClickListener(v -> {
                // 用户点击取消
                dialog.dismiss();
            });
        }

        dialog.show();
    }

    private void addDeviceToDatabase(Presence presence) {
        // 在后台线程中插入数据库
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                // 创建设备实体并插入数据库
                 Device device = new Device();
                 device.setDeviceId(presence.deviceId);
                 device.setDeviceType(presence.deviceType);
                 database.deviceDao().insertDevice(device);

                Log.i("MainActivity", "设备已添加到数据库: " + presence.deviceId);

                // 如果需要更新UI，切换到主线程
                runOnUiThread(() -> {
                    Toast.makeText(this, "设备已添加: " + presence.deviceId, Toast.LENGTH_SHORT).show();
                });
            } catch (Exception e) {
                Log.e("MainActivity", "插入数据库时出错", e);
                runOnUiThread(() -> {
                    Toast.makeText(this, "添加设备失败", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }
}
