package com.SmartHome.SmartHomeDemo.application;

import android.app.Application;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.util.Log;

import com.SmartHome.SmartHomeDemo.database.AppDatabase;
import com.SmartHome.SmartHomeDemo.database.Device;
import com.SmartHome.SmartHomeDemo.dds.BaseDdsManager;
import com.SmartHome.SmartHomeDemo.dds.CommandDdsManager;
import com.SmartHome.SmartHomeDemo.dds.AlertDdsManager;
import com.SmartHome.SmartHomeDemo.dds.HomeStatusDdsManager;
import com.SmartHome.SmartHomeDemo.dds.PresenceDdsManager;
import com.SmartHome.SmartHomeDemo.dds.VehicleStatusDdsManager;
import com.zrdds.infrastructure.FloatSeq;
import com.zrdds.infrastructure.StringSeq;

import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.function.ToDoubleBiFunction;

import idl.SmartDemo03.HomeStatus;
import idl.SmartDemo03.Presence;

/*
    统一管理数据库和所有DDS相关
    统一管理数据库和所有DDS相关
    统一管理数据库和所有DDS相关
 */
public class SmartHomeApplication extends Application {
    private static final String TAG = "SmartHomeApplication";

    private AppDatabase database;
    private BaseDdsManager baseDdsManager;
    private CommandDdsManager commandDdsManager;
    private AlertDdsManager alertDdsManager;
    private HomeStatusDdsManager homeStatusDdsManager;
    private PresenceDdsManager presenceDdsManager;
    private VehicleStatusDdsManager vehicleStatusDdsManager;

    private ConnectivityManager.NetworkCallback networkCallback;
    private String deviceId;
    private boolean isDeviceConnected = false;
    private OnDevicePresenceListener devicePresenceListener;

    // 设备Presence监听器接口
    public interface OnDevicePresenceListener {
        void onDeviceJoined(String deviceId, String deviceType);
        void onDeviceLeft(String deviceId, String deviceType);
    }

    // 添加一个新的监听器接口
    public interface OnUnknownDeviceListener {
        void onUnknownDeviceDetected(Presence presence);
    }

    private OnUnknownDeviceListener unknownDeviceListener;
    // 添加设置监听器的方法
    public void setOnUnknownDeviceListener(OnUnknownDeviceListener listener) {
        this.unknownDeviceListener = listener;
    }


    @Override
    public void onCreate() {
        super.onCreate();

        // 加载ZRDDS库
        System.loadLibrary("ZRDDS_JAVA");

        // 初始化数据库
        database = AppDatabase.getDatabase(this);

        // 生成设备ID
        deviceId = generateDeviceId();

        // 初始化DDS
        initializeDDS();

        // 开始网络监听
        startNetworkMonitoring();

        // 设置Presence消息监听器
        setupPresenceListener();
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

        presenceDdsManager = new PresenceDdsManager();
        presenceDdsManager.initialize(baseDdsManager);

        vehicleStatusDdsManager = new VehicleStatusDdsManager();
        vehicleStatusDdsManager.initialize(baseDdsManager);
    }

    private void startNetworkMonitoring() {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkRequest networkRequest = new NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
                .build();

        networkCallback = new ConnectivityManager.NetworkCallback() {
            @Override
            public void onAvailable(Network network) {
                super.onAvailable(network);
                Log.i(TAG, "网络连接可用");
                handleNetworkConnected();
            }

            @Override
            public void onLost(Network network) {
                super.onLost(network);
                Log.i(TAG, "网络连接丢失");
                handleNetworkDisconnected();
            }
        };

        connectivityManager.registerNetworkCallback(networkRequest, networkCallback);
    }

    private void setupPresenceListener() {
        presenceDdsManager.setOnPresenceReceivedListener(new PresenceDdsManager.OnPresenceReceivedListener() {
            @Override
            public void onPresenceReceived(Presence presence) {
                Log.d(TAG, "收到Presence消息: deviceId=" + presence.deviceId +
                        ", inRange=" + presence.inRange +
                        ", deviceType=" + presence.deviceType);

                // 处理设备Presence消息
                handleDevicePresence(presence);
            }
        });
    }

    private void handleDevicePresence(Presence presence) {
        // 检查是否是本设备发送的消息，避免处理自己的消息
        if (deviceId.equals(presence.deviceId)) {
            return;
        }

        if (presence.inRange) {
            // 设备加入网络
            Log.i(TAG, "检测到设备加入网络: " + presence.deviceId);

            // 在后台线程中查询数据库，检查设备是否存在
            Executors.newSingleThreadExecutor().execute(() -> {
                try {
                    // 这里需要根据您的实际数据库结构来查询设备
                    // 假设有一个deviceDao()方法和getDeviceById()方法
                     Device device = database.deviceDao().getDeviceByDeviceId(presence.deviceId);

                     if (device != null) {
                         // 设备存在于数据库中
                         Log.i(TAG, "设备存在于数据库中: " + presence.deviceId);

                         // 通知监听器
                         if (devicePresenceListener != null) {
                             new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> {
                                 devicePresenceListener.onDeviceJoined(presence.deviceId, presence.deviceType);
                             });
                         }
                     } else {
                         // 设备不存在于数据库中，通知Activity显示对话框
                         if (unknownDeviceListener != null) {
                             new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> {
                                 unknownDeviceListener.onUnknownDeviceDetected(presence);
                             });
                         }
                     }

                    // 通知监听器（暂时不检查数据库）
                    if (devicePresenceListener != null) {
                        new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> {
                            devicePresenceListener.onDeviceJoined(presence.deviceId, presence.deviceType);
                        });
                    }
                } catch (Exception e) {
                    Log.e(TAG, "查询设备信息时出错", e);
                }
            });
        } else {
            // 设备离开网络
            Log.i(TAG, "检测到设备离开网络: " + presence.deviceId);

            // 通知监听器
            if (devicePresenceListener != null) {
                new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> {
                    devicePresenceListener.onDeviceLeft(presence.deviceId, presence.deviceType);
                });
            }
        }
    }

    //对于本设备联网状态的处理
    private void handleNetworkConnected() {
        if (!isDeviceConnected) {
            isDeviceConnected = true;
            // 设备连接到网络，发布加入消息
            presenceDdsManager.onDeviceJoined(deviceId, "SmartHomeDevice");
            Log.i(TAG, "设备加入网络: " + deviceId);
        }
    }

    //对于本设备联网状态的处理
    private void handleNetworkDisconnected() {
        if (isDeviceConnected) {
            isDeviceConnected = false;
            // 设备失去网络连接，发布离开消息
            presenceDdsManager.onDeviceLeft(deviceId, "SmartHomeDevice");
            Log.i(TAG, "设备离开网络: " + deviceId);
        }
    }

    private String generateDeviceId() {
        // 使用Android ID或生成UUID作为设备ID
        return "device_" + UUID.randomUUID().toString().substring(0, 8);
    }



    private void setupHomeStatusListener() {
        homeStatusDdsManager.setOnHomeStatusReceivedListener(new HomeStatusDdsManager.OnHomeStatusReceivedListener() {
            @Override
            public void onHomeStatusReceived(idl.SmartDemo03.HomeStatus homeStatus) {
                Log.d(TAG, "收到HomeStatus消息: deviceId=" + homeStatus.deviceIds.toString() +
                        ", status=" + homeStatus.deviceTypes.toString());

                // 处理HomeStatus消息
                handleHomeStatus(homeStatus);
            }
        });
    }

    private void handleHomeStatus(idl.SmartDemo03.HomeStatus homeStatus) {
        // 在这里处理收到的HomeStatus消息
        Log.i(TAG, "处理HomeStatus消息: " + homeStatus.toString());
        // 可以添加更多处理逻辑，例如更新UI或存储数据等
        //TODO 逻辑待完善, 可能需要修改HomeStatus
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

    public PresenceDdsManager getPresenceDdsManager() {
        return presenceDdsManager;
    }

    public VehicleStatusDdsManager getVehicleStatusDdsManager() {
        return vehicleStatusDdsManager;
    }

    public String getId() {
        return deviceId;
    }

    public void setOnDevicePresenceListener(OnDevicePresenceListener listener) {
        this.devicePresenceListener = listener;
    }

    @Override
    public void onTerminate() {
        // 应用终止前发送设备离开消息
        if (isDeviceConnected) {
            presenceDdsManager.onDeviceLeft(deviceId, "SmartHomeDevice");
        }

        // 停止网络监听
        ConnectivityManager connectivityManager =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (networkCallback != null) {
            connectivityManager.unregisterNetworkCallback(networkCallback);
        }

        // 清理DDS资源
        if (baseDdsManager != null) {
            baseDdsManager.cleanup();
        }

        //仅供测试时使用!!!
        if(database != null) {
            database.deviceDao().deleteAll();
        }

        super.onTerminate();
    }
}
