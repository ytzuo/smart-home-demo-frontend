
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
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;

import idl.SmartDemo03.Alert;
import idl.SmartDemo03.HomeStatus;
import idl.SmartDemo03.Presence;
import idl.SmartDemo03.VehicleStatus;


public class MainActivity extends AppCompatActivity {

    private AppDatabase database;
    private SmartHomeApplication app;
    private CarFragment currentCarFragment;
    private HomeFragment currentHomeFragment;
    private LogFragment currentLogFragment;
    private SettingFragment currentSettingFragment;

    // 添加一个线程处理所有警报
    private Thread alertHandlingThread;
    private volatile boolean isAlertThreadRunning = true;

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
//        VehicleStatusDdsManager vehicleStatusDdsManager = app.getVehicleStatusDdsManager();
        // 设置VehicleStatus监听器
        app.setOnVehicleStatusReceivedListener(new SmartHomeApplication.OnVehicleStatusReceivedListener() {
            @Override
            public void onVehicleStatusReceived(VehicleStatus vehicleStatus) {
                updateCarFragmentUI(vehicleStatus);
            }
        });


        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);

        // 只有在savedInstanceState为null时才加载初始Fragment
        // 避免旋转屏幕等配置更改时重复加载
        // 只有在savedInstanceState为null时才加载初始Fragment
        // 避免旋转屏幕等配置更改时重复加载
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
            return showFragment(selected);
        });

        // 启动处理警报的线程
        //startAlertHandlingThread();
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


    private boolean showFragment(Fragment fragment) {
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
        Log.i("MainActivity", alert.deviceId+" "+alert.deviceType+" "+alert.level+" "+alert.description);

        // 直接通过FragmentManager找到当前的LogFragment（如果存在）
        LogFragment logFragment = (LogFragment) getSupportFragmentManager().findFragmentByTag("LogFragment");
        if (logFragment != null && logFragment.isVisible()) {
            logFragment.addLogItem(logItem);
        }

        // 注意：如果LogFragment当前未加载，数据会在下次加载时显示，
        // 因为LogViewModel会保持数据状态
        com.SmartHome.SmartHomeDemo.database.Log newLog = new com.SmartHome.SmartHomeDemo.database.Log();
        newLog.setLogType(alert.level);
        newLog.setLogId(""+alert.alert_id);
        newLog.setDescription(alert.description);
        newLog.setTimestamp(alert.timeStamp);
        // 在后台线程中执行数据库操作
        Executors.newSingleThreadExecutor().execute(new Runnable() {
            @Override
            public void run() {
                app.getDatabase().logDao().insertLog(newLog);
            }
        });
        Log.i("MainActivity", "插入Alert数据");
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

                    // 从数据库获取最新的设备列表
                    List<Device> devices = database.deviceDao().getAllDevices();

                    // 转换为FurnitureItem列表
                    List<FurnitureItem> furnitureItems = new ArrayList<>();
                    for (Device dev : devices) {
                        if ("light".equals(dev.getDeviceType()) || "air_conditioner".equals(dev.getDeviceType())) {
                            FurnitureItem item = new FurnitureItem();
                            item.setDeviceId(dev.getDeviceId());
                            item.setDeviceType(dev.getDeviceType());
                            item.setWorkingStatus("未连接");
                            item.setStatus("未连接");
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

                    // 如果需要更新UI，切换到主线程
                    runOnUiThread(() -> {
                        Toast.makeText(this, "设备已添加: " + presence.deviceId, Toast.LENGTH_SHORT).show();

                        // 更新HomeFragment中的设备列表
                        if (currentHomeFragment != null && currentHomeFragment.getHomeViewModel() != null) {
                            // 更新ViewModel数据
                            currentHomeFragment.getHomeViewModel().updateFurnitureList(furnitureItems);
                        }
                    });
                } catch (Exception e) {
                    Log.e("MainActivity", "插入数据库时出错", e);
                    runOnUiThread(() -> {
                        Toast.makeText(this, "添加设备失败", Toast.LENGTH_SHORT).show();
                    });
                }
            });
        }

    // 在MainActivity中
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 停止警报处理线程
        isAlertThreadRunning = false;
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
            runOnUiThread(() -> finalHomeFragment.handleHomeStatus(finalNewStatus));
        }
    }

    // 启动处理警报的线程
    private void startAlertHandlingThread() {
        // 启动警报处理线程
        alertHandlingThread = new Thread(new Runnable() {
            @Override
            public void run() {
                Log.d("MainActivity", "警报处理线程已启动");
                while (isAlertThreadRunning) {
                    try {
                        // 线程保持运行，实际处理在onAlertReceived中通过handleAlertByType分发
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        Log.d("MainActivity", "警报处理线程被中断");
                        break;
                    }
                }
                Log.d("MainActivity", "警报处理线程已停止");
            }
        });
        alertHandlingThread.start();
    }

    // 根据设备类型分发警报到相应处理逻辑
    private void handleAlertByType(Alert alert) {
        if ("car".equals(alert.deviceType)) {
            // 在主线程中显示车辆警报弹窗
            runOnUiThread(() -> showCarAlert(alert));
        } else if ("light".equals(alert.deviceType) || "air_conditioner".equals(alert.deviceType)) {
            // 家具类设备（灯或空调）在主线程中显示家具警报弹窗
            runOnUiThread(() -> showFurnitureAlert(alert));
        }
    }

    // 显示车辆警报弹窗
    private void showCarAlert(Alert alert) {
        CarAlert carAlert = CarAlert.newInstance(alert.deviceId, alert.deviceType, alert.description);
        carAlert.setOnButtonClickListener(new CarAlert.OnButtonClickListener() {
            @Override
            public void onConfirmClick() {
                // 处理确认按钮点击事件
                carAlert.dismiss();
            }
        });

        //if (!isFinishing() && !getSupportFragmentManager().isStateSaved()) {
            carAlert.show(getSupportFragmentManager(), "car_alert");
        //}
    }

    // 显示家具警报弹窗
    private void showFurnitureAlert(Alert alert) {
        FurnitureAlert furnitureAlert = FurnitureAlert.newInstance(alert.deviceId, alert.deviceType, alert.description);
        furnitureAlert.setOnButtonClickListener(new FurnitureAlert.OnButtonClickListener() {
            @Override
            public void onConfirmClick() {
                // 处理确认按钮点击事件
                furnitureAlert.dismiss();
            }

            @Override
            public void onViewAlertClick() {
                // 处理查看设备按钮点击事件
                furnitureAlert.dismiss();
                // 切换到HomeFragment查看设备
                showFragment(currentHomeFragment);
                BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
                bottomNav.setSelectedItemId(R.id.nav_home);
            }
        });

//        if (!isFinishing() && !getSupportFragmentManager().isStateSaved()) {
            furnitureAlert.show(getSupportFragmentManager(), "furniture_alert");
        //}
    }

}
