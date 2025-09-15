
package com.SmartHome.SmartHomeDemo;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;


import androidx.activity.EdgeToEdge;
import androidx.annotation.UiThread;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.Fragment;

import com.SmartHome.SmartHomeDemo.application.SmartHomeApplication;
import com.SmartHome.SmartHomeDemo.database.AppDatabase;
import com.SmartHome.SmartHomeDemo.database.BlacklistedDevice;
import com.SmartHome.SmartHomeDemo.database.Device;
import com.SmartHome.SmartHomeDemo.database.DeviceDao;
import com.SmartHome.SmartHomeDemo.dds.AlertDdsManager;
import com.SmartHome.SmartHomeDemo.dds.HomeStatusDdsManager;
import com.SmartHome.SmartHomeDemo.dds.VehicleStatusDdsManager;
import com.SmartHome.SmartHomeDemo.fragments.CarFragment.CarAlert;
import com.SmartHome.SmartHomeDemo.fragments.CarFragment.CarFragment;
import com.SmartHome.SmartHomeDemo.fragments.HomeFragment.FurnitureAlert;
import com.SmartHome.SmartHomeDemo.fragments.HomeFragment.FurnitureItem;
import com.SmartHome.SmartHomeDemo.fragments.HomeFragment.HomeFragment;
import com.SmartHome.SmartHomeDemo.fragments.LogFragment.LogFragment;
import com.SmartHome.SmartHomeDemo.fragments.LogFragment.LogItem;
import com.SmartHome.SmartHomeDemo.fragments.SettingFragment.SettingFragment;
import com.SmartHome.SmartHomeDemo.utils.ToastUtil;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;

import idl.SmartDemo03.Alert;
import idl.SmartDemo03.EnergyRawData;
import idl.SmartDemo03.HomeStatus;
import idl.SmartDemo03.Presence;
import idl.SmartDemo03.VehicleStatus;

public class MainActivity extends AppCompatActivity {
    private static final String GROUP_PREFS_NAME = "GroupNames";
    private static final String GROUP_1_KEY = "group_1_name";
    private static final String GROUP_2_KEY = "group_2_name";
    private static final String GROUP_3_KEY = "group_3_name";
    private AppDatabase database;
    private SmartHomeApplication app;
    private CarFragment currentCarFragment;
    private HomeFragment currentHomeFragment;
    private LogFragment currentLogFragment;
    private SettingFragment currentSettingFragment;
    volatile FurnitureAlert currentFurnitureAlert;
    volatile CarAlert currentCarAlert;
    // 用于暂存收到的媒体数据，直到对应的alert记录被插入数据库
    private Queue<PendingMediaData> pendingMediaQueue = new ConcurrentLinkedQueue<>();
    // 内部类：用于存储待处理的媒体数据
    private static class PendingMediaData {
        int alertId;
        Bitmap bitmap;
        Alert alert; // 添加alert字段以保存设备信息
        long timestamp;

        PendingMediaData(int alertId, Bitmap bitmap) {
            this.alertId = alertId;
            this.bitmap = bitmap;
            this.timestamp = System.currentTimeMillis();
        }

        PendingMediaData(int alertId, Bitmap bitmap, Alert alert) {
            this.alertId = alertId;
            this.bitmap = bitmap;
            this.alert = alert; // 保存alert对象
            this.timestamp = System.currentTimeMillis();
        }
    }
    // 用于跟踪已插入数据库的alert ID
    private Map<Integer, Boolean> insertedAlerts = new HashMap<>();

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
        app.setOnAlertReceivedListener(new AlertDdsManager.OnAlertReceivedListener() {
            @Override
            public void onAlertReceived(Alert alert) {
                Log.i("MainActivity", "收到警报：" + alert.deviceId + " " + alert.deviceType + " " + alert.description);
                addAlertToLog(alert);
                // 根据deviceType分发到不同的处理逻辑
                handleAlertByType(alert);
            }
        });

        // 设置未知设备监听器
        app.setOnUnknownDeviceListener(new SmartHomeApplication.OnUnknownDeviceListener() {
            @Override
            public void onUnknownDeviceDetected(Presence presence) {
                showNewDeviceDialog(presence);
            }
        });

        // 设置HomeStatus监听器
        app.setOnHomeStatusReceivedListener(new SmartHomeApplication.OnHomeStatusReceivedListener() {
            @Override
            public void onHomeStatusReceived(HomeStatus homeStatus) {
                updateHomeFragmentUI(homeStatus);
            }
        });



        // 设置VehicleStatus监听器
        //VehicleStatusDdsManager vehicleStatusDdsManager = app.getVehicleStatusDdsManager();
        // 设置VehicleStatus监听器
        app.setOnVehicleStatusReceivedListener(new SmartHomeApplication.OnVehicleStatusReceivedListener() {
            @Override
            public void onVehicleStatusReceived(VehicleStatus vehicleStatus) {
                updateCarFragmentUI(vehicleStatus);
            }
        });

        // 设置媒体接收监听器
        app.setOnMediaReceivedListener(new SmartHomeApplication.OnMediaReceivedListener() {
            @Override
            public void onMediaReceived(int alertId, Bitmap bitmap, String deviceId, String deviceType) {
                Log.i("MainActivity", "收到AlertMedia数据: alertId=" + alertId + ", deviceId=" + deviceId + ", deviceType=" + deviceType);
                handleReceivedAlertMedia(alertId, bitmap, deviceId, deviceType);
            }
        });

        // 设置VehicleMedia接收监听器
        app.setOnVehicleMediaReceivedListener(new SmartHomeApplication.OnVehicleMediaReceivedListener() {
            @Override
            public void onVehicleMediaReceived(int alertId, Bitmap bitmap, String deviceId, String deviceType) {
                handleReceivedVehicleMedia(alertId, bitmap, deviceId, deviceType);
                Log.i("MainActivity", "收到VehicleMedia: alertId=" + alertId + ", deviceId=" + deviceId + ", deviceType=" + deviceType);
            }
        });

        // 设置ReportMedia接收监听器
        app.setOnReportMediaReceivedListener(new SmartHomeApplication.OnReportMediaReceivedListener() {
            @Override
            public void onReportMediaReceived(String reportId, Bitmap bitmap, String deviceId, String deviceType) {
                Log.i("MainActivity", "收到ReportMedia: reportId=" + reportId + ", deviceId=" + deviceId + ", deviceType=" + deviceType);
                handleReceivedReportMedia(reportId, bitmap, deviceId, deviceType);
            }
        });

        app.setOnEnergyRawDataReceivedListener(new SmartHomeApplication.OnEnergyRawDataReceivedListener() {
            @Override
            public void onEnergyRawDataReceived(EnergyRawData energyRawData) {
                // TODO: 处理接收到的能源数据
                Log.i("MainActivity", "收到EnergyRawData");
                showChartWindow(energyRawData);
            }
        });


        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);

        if (savedInstanceState == null) {
            currentCarFragment = new CarFragment();
            currentHomeFragment = new HomeFragment();
            currentLogFragment = new LogFragment();
            currentSettingFragment = new SettingFragment();

            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragment_container, currentCarFragment, "CarFragment")
                    .add(R.id.fragment_container, currentHomeFragment, "HomeFragment")
                    .add(R.id.fragment_container, currentLogFragment, "LogFragment")
                    .add(R.id.fragment_container, currentSettingFragment, "SettingFragment")
                    .hide(currentHomeFragment)
                    .hide(currentLogFragment)
                    .hide(currentSettingFragment)
                    .commit();
        } else {
            // 从savedInstanceState恢复Fragment引用
            currentCarFragment = (CarFragment) getSupportFragmentManager().findFragmentByTag("CarFragment");
            currentHomeFragment = (HomeFragment) getSupportFragmentManager().findFragmentByTag("HomeFragment");
            currentLogFragment = (LogFragment) getSupportFragmentManager().findFragmentByTag("LogFragment");
            currentSettingFragment = (SettingFragment) getSupportFragmentManager().findFragmentByTag("SettingFragment");
        }


        bottomNav.setOnNavigationItemSelectedListener(item -> {
            Fragment selected = null;
            int id = item.getItemId();
            if (id == R.id.nav_car) {
                selected = currentCarFragment;
            } else if (id == R.id.nav_home) {
                selected = currentHomeFragment;
            } else if (id == R.id.nav_log) {
                selected = currentLogFragment;
            } else if (id == R.id.nav_setting) {
                selected = currentSettingFragment;
            }
            // 启动时刷新初始Fragment
            refreshFragmentUI(currentCarFragment);
            return showFragment(selected);
        });
        // 启动处理警报的线程
        //startAlertHandlingThread();
    }

    // 处理接收到的定时发送的 车辆媒体数据
    private void handleReceivedVehicleMedia(int alertId, Bitmap bitmap, String deviceId, String deviceType) {
        Log.i("MainActivity", "handleReceivedVehicleMedia");

        // 更新车辆界面中的图片
        if (currentCarFragment != null && bitmap != null) {
            Log.i("MainActivity", "已更新车辆界面中的图片");
            runOnUiThread(() -> {
                currentCarFragment.updateCarImage(bitmap);
            });
        }
    }

    /**
     * 刷新Fragment UI的方法
     * @param fragment 需要刷新的Fragment
     */
    private void refreshFragmentUI(Fragment fragment) {
        Log.i("refresh", "调用refreshFragmentUI");
        if (fragment == null) return;

        if (fragment instanceof HomeFragment) {
            // 刷新HomeFragment，重新从数据库加载家具数据
            ((HomeFragment) fragment).refreshData();
        } else if (fragment instanceof CarFragment) {
            // 刷新CarFragment，请求最新的车辆状态
            ((CarFragment) fragment).refreshData();
        } else if (fragment instanceof LogFragment) {
            // 刷新LogFragment，重新加载日志数据
            ((LogFragment) fragment).refreshData();
        } else if (fragment instanceof SettingFragment) {
            // 刷新SettingFragment
            ((SettingFragment) fragment).refreshData();
        }
    }


    private void updateCarFragmentUI(VehicleStatus newStatus) {
        // 更新CarFragment UI
        if (currentCarFragment != null) {
            // 在主线程中更新UI
            runOnUiThread(() -> {
                // 可以通过接口或ViewModel等方式通知CarFragment更新UI
                // 这里使用广播方式通知CarFragment更新
                // 或者通过接口回调方式实现
                currentCarFragment.updateVehicleStatus(newStatus);
            });
        }
    }


    public boolean showFragment(Fragment fragment) {
        if (fragment != null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .hide(currentCarFragment)
                    .hide(currentHomeFragment)
                    .hide(currentLogFragment)
                    .hide(currentSettingFragment)
                    .show(fragment)
                    .commit();
            return true;
        }
        return false;
    }
    public void hideAllFragmentsExcept(Fragment exception) {
        getSupportFragmentManager()
                .beginTransaction()
                .hide(currentCarFragment)
                .hide(currentHomeFragment)
                .hide(currentLogFragment)
                .hide(currentSettingFragment)
                .commit();
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
                ""+alert.alert_id,                 // Alert ID
                alert.description,              // Alert 描述
                alert.deviceId
        );
        Log.i("MainActivity", alert.deviceId+" "+alert.deviceType+" "+alert.level+" "+alert.description);

        // 直接通过FragmentManager找到当前的LogFragment（如果存在）
        LogFragment logFragment = (LogFragment) getSupportFragmentManager().findFragmentByTag("LogFragment");
        if (logFragment != null) {
            // 不管是否可见都尝试更新数据
            logFragment.addLogItem(logItem);
        }

        // 注意：如果LogFragment当前未加载，数据会在下次加载时显示，
        // 因为LogViewModel会保持数据状态
        com.SmartHome.SmartHomeDemo.database.Log newLog = new com.SmartHome.SmartHomeDemo.database.Log();
        newLog.setLogType(alert.level);
        newLog.setLogId(""+alert.alert_id);
        newLog.setDescription(alert.description);
        newLog.setTimestamp(alert.timeStamp);
        newLog.setLogDevice(alert.deviceId);  // 设置设备ID
        // 在后台线程中执行数据库操作
        Executors.newSingleThreadExecutor().execute(new Runnable() {
            @Override
            public void run() {
                app.getDatabase().logDao().insertLog(newLog);
                Log.i("MainActivity", newLog.getLogId() + "已插入数据库");

                // 标记该alert已插入数据库
                synchronized (insertedAlerts) {
                    insertedAlerts.put(alert.alert_id, true);
                }

                // 检查是否有待处理的媒体数据
                processPendingMediaData();
            }
        });
        Log.i("MainActivity", "插入Alert数据, alertId: " +alert.alert_id);
    }

    private void showNewDeviceDialog(Presence presence) {
        // 加载对话框布局
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.window_match, null);

        // 更新对话框中的文本
        TextView deviceIdText = dialogView.findViewById(R.id.match_device_id);
        TextView deviceTypeText = dialogView.findViewById(R.id.match_device_type);
        CheckBox addToBlacklistCheckBox = dialogView.findViewById(R.id.checkBox_add_to_blacklist);

        if (deviceIdText != null) {
            deviceIdText.setText(getString(R.string.device_id) + ": " + presence.deviceId);
        }

        if (deviceTypeText != null) {
            deviceTypeText.setText(getString(R.string.device_type) + ": " + presence.deviceType);
        }

        // 获取分组相关的UI组件
        Spinner groupSpinner = dialogView.findViewById(R.id.spinner_device_group);

        // 创建三个默认分组列表
        List<String> groups = new ArrayList<>(Arrays.asList("默认分组1", "默认分组2", "默认分组3"));

        // 从SharedPreferences获取用户自定义的分组名称
        SharedPreferences prefs = getSharedPreferences("GroupNames", MODE_PRIVATE);
        String group1 = prefs.getString(GROUP_1_KEY, "默认分组1");
        String group2 = prefs.getString(GROUP_2_KEY, "默认分组2");
        String group3 = prefs.getString(GROUP_3_KEY, "默认分组3");

        groups.set(0, group1);
        groups.set(1, group2);
        groups.set(2, group3);

        // 创建适配器并设置Spinner
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, groups);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        groupSpinner.setAdapter(adapter);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();
        // 设置对话框不可通过点击外部区域取消
        dialog.setCanceledOnTouchOutside(false);

        // 设置按钮点击事件
        if (dialogView.findViewById(R.id.add_new_done) != null) {
            dialogView.findViewById(R.id.add_new_done).setOnClickListener(v -> {
                // 获取选中的分组
                String selectedGroup = (String) groupSpinner.getSelectedItem();
                if (selectedGroup == null) {
                    // 如果没有选中任何分组，使用第一个默认分组
                    selectedGroup = group1;
                }

                // 用户点击确认，将设备插入数据库
                addDeviceToDatabase(presence, selectedGroup);
                //Log.i("MainActivity", "设备存入数据库: "+);
                dialog.dismiss();
            });
        }

        if (dialogView.findViewById(R.id.add_new_cancel) != null) {
            dialogView.findViewById(R.id.add_new_cancel).setOnClickListener(v -> {
                if (addToBlacklistCheckBox.isChecked()) {
                    // 用户选择将设备添加到黑名单
                    addDeviceToBlacklist(presence);
                }
                // 用户点击取消
                dialog.dismiss();
            });
        }
        dialog.show();
    }

    private void addDeviceToDatabase(Presence presence, String deviceGroup) {
        // 在后台线程中插入数据库
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                // 创建设备实体并插入数据库
                Device device = new Device();
                device.setDeviceId(presence.deviceId);
                device.setDeviceType(presence.deviceType);
                device.setDeviceGroup(deviceGroup); // 设置设备分组
                database.deviceDao().insertDevice(device);

                Log.i("MainActivity", "设备已添加到数据库: " + presence.deviceId + " 分组: " + deviceGroup);

                // 从数据库获取最新的设备列表
                List<Device> devices = database.deviceDao().getAllDevices();
                Log.i("MainActivity", "数据库现在包含 " + devices.size() + " 个设备");

                // 转换为FurnitureItem列表
                List<FurnitureItem> furnitureItems = new ArrayList<>();
                for (Device dev : devices) {
                    if ("light".equals(dev.getDeviceType())
                            || "air_conditioner".equals(dev.getDeviceType())
                            || "ac".equals(dev.getDeviceType())) {
                        FurnitureItem item = new FurnitureItem();
                        item.setDeviceId(dev.getDeviceId());
                        item.setDeviceType(dev.getDeviceType());
                        item.setDeviceGroup(dev.getDeviceGroup()); // 设置设备分组
                        item.setWorkingStatus("未连接");
                        item.setStatus("00000000");
                        item.setTime("默认时间");

                        // 设置图片资源
                        if ("light".equals(dev.getDeviceType())) {
                            item.setImageResource(R.drawable.icon_light);
                        } else if ("air_conditioner".equals(dev.getDeviceType())) {
                            item.setImageResource(R.drawable.icon_air_conditioner);
                        }

                        furnitureItems.add(item);
                    }
                }

                Log.i("MainActivity", "转换后得到 " + furnitureItems.size() + " 个家具项");

                // 如果需要更新UI，切换到主线程
                runOnUiThread(() -> {
                    ToastUtil.showToast(this, "设备已添加: " + presence.deviceId + " 分组: " + deviceGroup, Toast.LENGTH_SHORT);

                    // 更新HomeFragment中的设备列表
                    if (currentHomeFragment != null && currentHomeFragment.getHomeViewModel() != null) {
                        // 更新ViewModel数据
                        Log.i("MainActivity", "更新HomeFragment数据，包含 " + furnitureItems.size() + " 个项目");
                        currentHomeFragment.getHomeViewModel().updateFurnitureList(furnitureItems);
                    } else {
                        Log.w("MainActivity", "无法更新HomeFragment数据，currentHomeFragment或viewModel为null");
                    }
                });
            } catch (Exception e) {
                Log.e("MainActivity", "插入数据库时出错", e);
                runOnUiThread(() -> {
                    ToastUtil.showToast(this, "添加设备失败", Toast.LENGTH_SHORT);
                });
            }
        });
    }




    // 在MainActivity中
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 清理资源
        pendingMediaQueue.clear();
        insertedAlerts.clear();

        // 注意：Activity的onDestroy()也不一定总被调用
        if (isFinishing()) {
            // 如果是正常结束，执行清理操作
            SmartHomeApplication app = (SmartHomeApplication) getApplication();
            AppDatabase database = app.getDatabase();
            if (database != null) {
                AppDatabase.databaseWriteExecutor.execute(() -> {
                    database.deviceDao().deleteAll();
                    Log.d("MainActivity", "应用关闭时删除设备数据");
                });
            }
        }
    }

    private void updateHomeFragmentUI(HomeStatus newStatus) {
        // 更新HomeFragment UI
        HomeFragment homeFragment = (HomeFragment) getSupportFragmentManager().findFragmentByTag("HomeFragment");
        if (homeFragment == null) {
            // 如果通过tag找不到，尝试通过当前显示的fragment判断
            Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
            if (currentFragment instanceof HomeFragment) {
                homeFragment = (HomeFragment) currentFragment;
            }
        }

        if (homeFragment != null) {
            // 创建final变量以在lambda表达式中使用
            final HomeFragment finalHomeFragment = homeFragment;
            final HomeStatus finalNewStatus = newStatus;
            // 在主线程中更新UI
            //int l = newStatus.deviceIds.length();
//            for(int i = 0; i < 1; i++) {
//                Log.i("HomeFragment", "Device: "+newStatus.deviceIds.get_at(i)+" "+newStatus.deviceTypes.get_at(i));
//            }
            runOnUiThread(() -> finalHomeFragment.handleHomeStatus(finalNewStatus));
        }
    }

    // 根据设备类型分发警报到相应处理逻辑
    private void handleAlertByType(Alert alert) {
        Log.i("MainActivity", " 开始处理警报类型: "+alert.deviceType);
        if ("car".equals(alert.deviceType)) {
            // 在主线程中显示车辆警报弹窗
            runOnUiThread(() -> showCarAlert(alert));
        } else if ("light".equals(alert.deviceType)
                || "air_conditioner".equals(alert.deviceType)
                || "ac".equals(alert.deviceType)) {
            // 家具类设备（灯或空调）在主线程中显示家具警报弹窗
            runOnUiThread(() -> showFurnitureAlert(alert));
        }
    }

    // 显示车辆警报弹窗
    private void showCarAlert(Alert alert) {
        // 显示车辆警报弹窗
        if (currentCarAlert != null) {
            currentCarAlert.dismiss();
            currentCarAlert = null;
        }

        currentCarAlert = CarAlert.newInstance(alert.deviceId, alert.deviceType, alert.description);
        currentCarAlert.setOnButtonClickListener(new CarAlert.OnButtonClickListener() {
            @Override
            public void onConfirmClick() {
                // 处理确认按钮点击事件
                currentCarAlert.dismiss();
                currentCarAlert = null;
            }
        });

        //if (!isFinishing() && !getSupportFragmentManager().isStateSaved()) {
        currentCarAlert.show(getSupportFragmentManager(), "car_alert");
        //}
    }
    // 显示家具警报弹窗
    private void showFurnitureAlert(Alert alert) {
        // 发送通知到状态栏
        sendFurnitureAlertNotification(alert);
        // 显示家具警报弹窗
        if (currentFurnitureAlert != null) {
            currentFurnitureAlert.dismiss();
            currentFurnitureAlert = null;
        }

        currentFurnitureAlert = FurnitureAlert.newInstance(alert.deviceId, alert.deviceType, alert.description);
        currentFurnitureAlert.setOnButtonClickListener(new FurnitureAlert.OnButtonClickListener() {
            @Override
            public void onConfirmClick() {
                // 处理确认按钮点击事件
                currentFurnitureAlert.dismiss();
                currentFurnitureAlert = null;

            }

            @Override
            public void onViewAlertClick() {
                // 处理查看设备按钮点击事件
                currentFurnitureAlert.dismiss();
                // 切换到HomeFragment查看设备
                showFragment(currentHomeFragment);
                currentFurnitureAlert = null;
                BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
                bottomNav.setSelectedItemId(R.id.nav_home);
            }
        });

        //currentFurnitureAlert.updateDeviceImage();
//        if (!isFinishing() && !getSupportFragmentManager().isStateSaved()) {
        currentFurnitureAlert.show(getSupportFragmentManager(), "furniture_alert");
        //}
    }

    public HomeFragment getCurrentHomeFragment() {
        return currentHomeFragment;
    }

    /**
     * 发送家具警报通知到状态栏
     * @param alert 警报信息
     */
    private void sendFurnitureAlertNotification(Alert alert) {
        // 检查是否允许发送通知
        SharedPreferences prefs = getSharedPreferences("settings", Context.MODE_PRIVATE);
        boolean isNoticeEnabled = prefs.getBoolean("notice_enabled", true);

        if (!isNoticeEnabled) {
            return; // 如果通知被禁用，则不发送通知
        }

        // 创建通知渠道 (Android 8.0+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "家具警报";
            String description = "家具设备警报通知";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel("furniture_alert_channel", name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }

        // 创建通知
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "furniture_alert_channel")
                .setSmallIcon(android.R.drawable.ic_dialog_alert) // 使用默认图标
                .setColor(getResources().getColor(android.R.color.holo_red_dark))
                .setContentTitle("家具警报: " + alert.deviceType)
                .setContentText("设备ID: " + alert.deviceId + " - " + alert.description)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setDefaults(NotificationCompat.DEFAULT_ALL);

        // 创建点击通知时的意图
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);
        builder.setContentIntent(pendingIntent);

        // 发送通知
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        try {
            notificationManager.notify(alert.deviceId.hashCode(), builder.build());
        } catch (SecurityException e) {
            Log.e("MainActivity", "发送通知失败: " + e.getMessage());
        }
    }


    private void handleReceivedAlertMedia(int alertId, Bitmap bitmap, String deviceId, String deviceType) {
        // 将媒体数据加入队列等待处理，同时保存设备信息
        Alert dummyAlert = new Alert();
        dummyAlert.alert_id = alertId;
        dummyAlert.deviceId = deviceId;
        dummyAlert.deviceType = deviceType;
        pendingMediaQueue.offer(new PendingMediaData(alertId, bitmap, dummyAlert));
        // 尝试处理队列中的媒体数据
        processPendingMediaData();

        //Log.i("MainActivity", "alertId: " + alertId + ", deviceId: " + deviceId + ", deviceType: " + deviceType);

        if(deviceType.equals("light") || deviceType.equals("air_conditioner") || deviceType.equals("ac")) {
            handleReceivedFurnitureAlertMedia(alertId, bitmap);
        } else if (deviceType.equals("car")) {
            handleReceivedCarAlertMedia(alertId, bitmap);
        }

    }
    // 处理接收到的媒体数据
    private void handleReceivedFurnitureAlertMedia(int alertId, Bitmap bitmap) {
        // 将媒体数据加入队列等待处理
        pendingMediaQueue.offer(new PendingMediaData(alertId, bitmap));
        // 尝试处理队列中的媒体数据
        processPendingMediaData();
        // 在后台线程等待currentFurnitureAlert创建完成
        new Thread(() -> {
            int attempts = 0;
            final int maxAttempts = 30; // 最多等待30秒
            while (currentFurnitureAlert == null && attempts < maxAttempts) {
                try {
                    Thread.sleep(1000);
                    attempts++;
                    Log.i("MainActivity", "等待家具警报窗口创建完成 (" + attempts + "/" + maxAttempts + ")");
                } catch (InterruptedException e) {
                    Log.e("MainActivity", "等待过程中被中断", e);
                    return;
                }
            }
            // 切换到主线程更新UI
            runOnUiThread(() -> {
                if (bitmap != null) {
                    Log.i("MainActivity", "更新家具警报图片");
                    currentFurnitureAlert.updateDeviceImage(bitmap);
                } else {
                    Log.i("MainActivity", "家具警报图片为空");
                }
            });

        }).start();
    }

    // 处理接收到的车辆媒体数据
    private void handleReceivedCarAlertMedia(int alertId, Bitmap bitmap) {
        Log.i("MainActivity", "handleReceiveCarMedia");
        // 将媒体数据加入队列等待处理
        pendingMediaQueue.offer(new PendingMediaData(alertId, bitmap));
        // 尝试处理队列中的媒体数据
        processPendingMediaData();
        // 在后台线程等待currentCarAlert创建完成
        new Thread(() -> {
            int attempts = 0;
            final int maxAttempts = 30; // 最多等待30秒
            while (currentCarAlert == null && attempts < maxAttempts) {
                try {
                    Thread.sleep(1000);
                    attempts++;
                    Log.i("MainActivity", "等待车辆警报窗口创建完成 (" + attempts + "/" + maxAttempts + ")");
                } catch (InterruptedException e) {
                    Log.e("MainActivity", "等待过程中被中断", e);
                    return;
                }
            }
            // 切换到主线程更新UI
            runOnUiThread(() -> {
                if (bitmap != null) {
                    Log.i("MainActivity", "更新车辆警报图片");
                    currentCarAlert.updateDeviceImage(bitmap);
                } else {
                    Log.i("MainActivity", "车辆警报图片为空");
                }
            });

        }).start();
    }

    // 保存媒体文件并与对应的Log记录关联
    private void saveMediaAndLinkToLog(int alertId, Bitmap bitmap) {
        try {
            // 创建文件名
            String fileName = "alert_" + alertId + "_" + System.currentTimeMillis() + ".jpg";
            String directoryPath = getExternalFilesDir(null) + "/media/";
            File directory = new File(directoryPath);
            if (!directory.exists()) {
                directory.mkdirs();
            }
            String filePath = directoryPath + fileName;

            // 保存图片到文件系统
            FileOutputStream out = new FileOutputStream(filePath);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
            out.flush();
            out.close();

            Log.i("MainActivity", "图片已保存到: " + filePath);

            // 更新数据库中对应的Log记录
            Executors.newSingleThreadExecutor().execute(() -> {
                try {
                    // 根据alertId查找对应的Log记录并更新image_path字段
                    String logId = String.valueOf(alertId); // 使用alertId作为logId
                    Log.i("MainActivity", logId + "更新报警图片为: "+filePath);
                    database.logDao().updateImagePathByLogId(logId, filePath);
                    Log.i("MainActivity", "已更新Log记录的图片路径: " + logId);
                } catch (Exception e) {
                    Log.e("MainActivity", "更新Log记录图片路径失败", e);
                }
            });
        } catch (Exception e) {
            Log.e("MainActivity", "保存媒体文件失败", e);
        }
    }

    // 显示媒体通知
    private void showMediaNotification(int alertId, Bitmap bitmap) {
        // 检查是否允许发送通知
        SharedPreferences prefs = getSharedPreferences("settings", Context.MODE_PRIVATE);
        boolean isNoticeEnabled = prefs.getBoolean("notice_enabled", true);

        if (!isNoticeEnabled) {
            return; // 如果通知被禁用，则不发送通知
        }

        // 创建通知渠道 (Android 8.0+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "媒体警报";
            String description = "设备发送的图片或视频通知";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel("media_alert_channel", name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }

        // 创建通知
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "media_alert_channel")
                .setSmallIcon(android.R.drawable.ic_menu_gallery)
                .setContentTitle("收到新图片")
                .setContentText("设备发送了新的图片，alertId: " + alertId)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setLargeIcon(bitmap); // 设置大图标为接收到的图片

        // 创建点击通知时的意图
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);
        builder.setContentIntent(pendingIntent);

        // 发送通知
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        try {
            notificationManager.notify(alertId, builder.build());
        } catch (SecurityException e) {
            Log.e("MainActivity", "发送通知失败: " + e.getMessage());
        }
    }

    // 处理待处理的媒体数据
    private void processPendingMediaData() {
        PendingMediaData mediaData;
        while ((mediaData = pendingMediaQueue.peek()) != null) {
            boolean isAlertInserted;
            synchronized (insertedAlerts) {
                isAlertInserted = insertedAlerts.getOrDefault(mediaData.alertId, false);
            }

            // 如果对应的alert已经插入数据库，或者等待时间超过5秒（可能是孤儿数据），则处理该媒体数据
            if (isAlertInserted || (System.currentTimeMillis() - mediaData.timestamp) > 5000 || mediaData.alert != null) {
                // 从队列中移除
                pendingMediaQueue.poll();

                // 如果alert已插入数据库，则更新图片路径
                if (isAlertInserted) {
                    saveMediaAndLinkToLog(mediaData.alertId, mediaData.bitmap);
                } else if (mediaData.alert != null) {
                    // 如果有alert对象，即使未插入数据库也处理媒体数据
                    //addAlertToLog(mediaData.alert);
                    saveMediaAndLinkToLog(mediaData.alertId, mediaData.bitmap);

                    // 标记该alert已处理
                    synchronized (insertedAlerts) {
                        insertedAlerts.put(mediaData.alertId, true);
                    }
                }
            } else {
                // 如果对应的alert还未插入数据库，则等待下次处理
                break;
            }
        }
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // 处理夜间模式切换
        Log.i("MainActivity", "onConfigurationChanged " + newConfig.toString());
        getDelegate().applyDayNight();

        // 手动更新所有视图的主题
        updateViewsTheme();

        // 更新所有Fragment的UI
        if (currentCarFragment != null) {
            currentCarFragment.onConfigurationChanged(newConfig);
        }
        if (currentHomeFragment != null) {
            currentHomeFragment.onConfigurationChanged(newConfig);
        }
        if (currentLogFragment != null) {
            currentLogFragment.onConfigurationChanged(newConfig);
        }
        if (currentSettingFragment != null) {
            currentSettingFragment.onConfigurationChanged(newConfig);
        }
        Log.i("MainActivity", "finishOnConfigurationChanged");
    }

    // 手动更新视图主题的方法
    private void updateViewsTheme() {
        // 更新根视图主题
        View rootView = findViewById(R.id.fragment_container);
        if (rootView != null) {
            int backgroundColor = (getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES ?
                    R.color.card_background_dark : R.color.card_background_light;
            rootView.setBackgroundColor(getResources().getColor(backgroundColor));
        }

        // 更新底部导航栏主题
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        if (bottomNav != null) {
            // 设置背景颜色
            int backgroundColor = (getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES ?
                    R.color.card_background_dark : R.color.card_background_light;
            bottomNav.setBackgroundColor(getResources().getColor(backgroundColor));

            // 更新菜单项颜色
            int textColor = (getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES ?
                    R.color.text_color_dark : R.color.text_color_light;

            // 更新所有Tab的文本和图标颜色
            for (int i = 0; i < bottomNav.getMenu().size(); i++) {
                bottomNav.getMenu().getItem(i).getIcon().setTintList(
                        getResources().getColorStateList(textColor));
            }
        }

        // 通知HomeFragment更新分组栏主题
        if (currentHomeFragment != null) {
            currentHomeFragment.updateGroupBarTheme();
        }
    }

    // 添加异常处理
    @Override
    protected void onResume() {
        try {
            super.onResume();
        } catch (Exception e) {
            Log.e("Lifecycle", "Error in MainActivity.onResume", e);
        }
    }
    private void handleReceivedReportMedia(String reportId, Bitmap bitmap, String deviceId, String deviceType) {
        // 如果是汽车相关的ReportMedia，更新CarFragment中的图片
        //if ("car".equals(deviceType)) {
            runOnUiThread(() -> {
                if (currentCarFragment != null && bitmap != null) {
                    currentCarFragment.updateCarImage(bitmap);
                }
            });
        //}
    }

    private void showChartWindow(EnergyRawData energyRawData) {
        // 加载window_energy_trend布局
        View trendView = getLayoutInflater().inflate(R.layout.window_energy_trend, null);

        // 获取图表实例
        LineChart energyChart = trendView.findViewById(R.id.trend_chart);

        // 创建数据集
        ArrayList<Entry> entries = new ArrayList<>();

        // 从EnergyRawData中提取数据
        com.zrdds.infrastructure.FloatSeq currentPowerSeq = energyRawData.currentPowerSeq;
        com.zrdds.infrastructure.StringSeq timeSeq = energyRawData.timeSeq;

        Log.d("ChartDebug", "接收到数据: 功率数据长度=" + currentPowerSeq.length() + ", 时间数据长度=" + timeSeq.length());

        // 检查数据是否有效
        if (currentPowerSeq.length() != timeSeq.length()) {
            Log.e("MainActivity", "数据长度不匹配: currentPowerSeq长度=" + currentPowerSeq.length() +
                    ", timeSeq长度=" + timeSeq.length());
            return;
        }

        // 如果数据为空，直接返回
        if (currentPowerSeq.length() == 0) {
            Log.w("MainActivity", "接收到空的能源数据");
            // 显示一个提示对话框
            new AlertDialog.Builder(this)
                    .setTitle("无数据")
                    .setMessage("当前设备没有能耗数据可供显示")
                    .setPositiveButton("关闭", null)
                    .show();
            return;
        }

        // 使用索引作为横坐标
        for (int i = 0; i < currentPowerSeq.length(); i++) {
            float powerValue = currentPowerSeq.get_at(i);
            // 使用索引作为X轴值
            entries.add(new Entry(i, powerValue));

            // 打印前几个数据点用于调试
            if (i < 5) {
                Log.d("ChartDebug", "数据点[" + i + "] 功率: " + powerValue);
            }
        }

        Log.d("ChartDebug", "总共添加了 " + entries.size() + " 个数据点");

        // 检查是否有有效数据
        if (entries.isEmpty()) {
            Log.w("MainActivity", "没有有效的数据点可以显示");
            new AlertDialog.Builder(this)
                    .setTitle("无有效数据")
                    .setMessage("无法处理接收到的能耗数据")
                    .setPositiveButton("关闭", null)
                    .show();
            return;
        }

        // 创建数据集
        LineDataSet dataSet = new LineDataSet(entries, "能耗 (" + energyRawData.deviceId + ")");
        dataSet.setColor(ColorTemplate.MATERIAL_COLORS[0]);
        dataSet.setValueTextColor(ColorTemplate.MATERIAL_COLORS[1]);
        dataSet.setLineWidth(3f); // 设置线条宽度为3像素
        dataSet.setCircleRadius(4f); // 设置数据点圆圈半径
        dataSet.setDrawValues(true); // 显示数值
        dataSet.setDrawCircles(true); // 确保绘制圆点
        dataSet.setDrawFilled(true); // 绘制填充区域

        // 设置数据到图表
        LineData lineData = new LineData(dataSet);
        energyChart.setData(lineData);

        // 设置X轴标签（使用索引）
        energyChart.getXAxis().setPosition(com.github.mikephil.charting.components.XAxis.XAxisPosition.BOTTOM);
        energyChart.getXAxis().setGranularity(1f); // 设置最小间隔为1
        energyChart.getXAxis().setValueFormatter(new com.github.mikephil.charting.formatter.ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                // 直接显示索引值
                return String.valueOf((int) value);
            }
        });

        // 设置Y轴标签
        energyChart.getAxisLeft().setValueFormatter(new com.github.mikephil.charting.formatter.ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                // 根据数值大小选择合适的格式
                if (value < 1) {
                    return String.format("%.2f", value) + "kWh";
                } else if (value < 10) {
                    return String.format("%.1f", value) + "kWh";
                } else {
                    return String.format("%.0f", value) + "kWh";
                }
            }
        });

        // 隐藏右边的Y轴
        energyChart.getAxisRight().setEnabled(false);

        // 设置图表描述
        energyChart.getDescription().setEnabled(false);

        // 启用触摸手势
        energyChart.setTouchEnabled(true);
        energyChart.setDragEnabled(true);
        energyChart.setScaleEnabled(true);

        // 设置图例
        energyChart.getLegend().setEnabled(true);

        // 刷新图表
        energyChart.invalidate();

        Log.d("ChartDebug", "图表已刷新，数据集包含 " + dataSet.getEntryCount() + " 个条目");

        // 显示图表弹窗
        new AlertDialog.Builder(this)
                .setView(trendView)
                .setTitle("设备能耗趋势 - " + energyRawData.deviceType)
                .setPositiveButton("关闭", null)
                .show();
    }

    private void addDeviceToBlacklist(Presence presence) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            try {
                // 创建黑名单设备实体并插入数据库
                BlacklistedDevice blacklistedDevice = new BlacklistedDevice();
                blacklistedDevice.setDeviceId(presence.deviceId);
                blacklistedDevice.setDeviceType(presence.deviceType);

                // 设置当前时间戳
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                String currentTime = sdf.format(new Date());
                blacklistedDevice.setTimestamp(currentTime);

                app.getDatabase().blacklistedDeviceDao().insert(blacklistedDevice);

                // 在主线程中更新UI
                runOnUiThread(() -> {
                    ToastUtil.showToast(this, "设备已添加到黑名单: " + presence.deviceId, Toast.LENGTH_SHORT);

                    // 从HomeFragment的家具列表中移除该设备
                    if (currentHomeFragment != null && currentHomeFragment.getHomeViewModel() != null) {
                        List<FurnitureItem> currentList = currentHomeFragment.getHomeViewModel().getFurnitureLiveData().getValue();
                        if (currentList != null) {
                            List<FurnitureItem> newList = new ArrayList<>();
                            for (FurnitureItem item : currentList) {
                                if (!item.getDeviceId().equals(presence.deviceId)) {
                                    newList.add(item);
                                }
                            }
                            currentHomeFragment.getHomeViewModel().updateFurnitureList(newList);
                        }
                    }
                });

                Log.i("MainActivity", "设备已添加到黑名单: " + presence.deviceId);
            } catch (Exception e) {
                Log.e("MainActivity", "添加设备到黑名单时出错: " + e.getMessage());
            }
        });
    }
    private boolean isDeviceBlacklisted(String deviceId) {
        try {
            BlacklistedDevice blacklistedDevice = app.getDatabase().blacklistedDeviceDao().getBlacklistedDeviceById(deviceId);
            return blacklistedDevice != null;
        } catch (Exception e) {
            Log.e("MainActivity", "检查设备是否在黑名单时出错: " + e.getMessage());
            return false;
        }
    }
    private void handleNewDevice(Presence presence) {
        // 检查设备是否在黑名单中
        if (isDeviceBlacklisted(presence.deviceId)) {
            Log.i("MainActivity", "设备在黑名单中，忽略: " + presence.deviceId);
            return;
        }

        // 检查设备是否已经配对
        Device existingDevice = database.deviceDao().getDeviceByDeviceId(presence.deviceId);
        if (existingDevice == null) {
            // 设备未配对，显示配对对话框
            showNewDeviceDialog(presence);
        }
    }

}
