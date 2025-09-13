package com.SmartHome.SmartHomeDemo.application;

import android.app.Application;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.core.net.ParseException;

import com.SmartHome.SmartHomeDemo.database.AppDatabase;
import com.SmartHome.SmartHomeDemo.database.Device;
import com.SmartHome.SmartHomeDemo.dds.BaseDdsManager;
import com.SmartHome.SmartHomeDemo.dds.CommandDdsManager;
import com.SmartHome.SmartHomeDemo.dds.AlertDdsManager;
import com.SmartHome.SmartHomeDemo.dds.EnergyRawDataDdsManager;
import com.SmartHome.SmartHomeDemo.dds.HomeStatusDdsManager;
import com.SmartHome.SmartHomeDemo.dds.MediaDdsManager;
import com.SmartHome.SmartHomeDemo.dds.PresenceDdsManager;
import com.SmartHome.SmartHomeDemo.dds.ReportMediaDdsManager;
import com.SmartHome.SmartHomeDemo.dds.VehicleStatusDdsManager;
import com.SmartHome.SmartHomeDemo.fragments.HomeFragment.FurnitureDataPack;
import com.SmartHome.SmartHomeDemo.fragments.HomeFragment.FurnitureItem;
import com.zrdds.infrastructure.FloatSeq;
import com.zrdds.infrastructure.StringSeq;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.function.ToDoubleBiFunction;

import idl.SmartDemo03.HomeStatus;
import idl.SmartDemo03.Presence;
import idl.SmartDemo03.VehicleStatus;

/*
    统一管理数据库和所有DDS相关
    统一管理数据库和所有DDS相关
    统一管理数据库和所有DDS相关
 */
public class SmartHomeApplication extends Application {
    private static final String TAG = "SmartHomeApplication";

    private Map<String, String> devicePresenceMap = new HashMap<>(); // 存储设备ID和最后收到presence的时间戳
    private Map<String, Boolean> deviceLanStatusMap = new HashMap<>(); // 存储设备ID和是否处于同一局域网的状态

    // 定时检测设备超时的Handler
    private Handler deviceTimeoutHandler = new Handler(Looper.getMainLooper());
    private Runnable deviceTimeoutRunnable = new Runnable() {
        @Override
        public void run() {
            checkDeviceTimeouts();
            deviceTimeoutHandler.postDelayed(this, 10000); // 每10秒检查一次
        }
    };

    // 设备超时时间（毫秒）
    private static final long DEVICE_TIMEOUT = 30000; // 30秒

    private AppDatabase database;
    private BaseDdsManager baseDdsManager;
    private CommandDdsManager commandDdsManager;
    private AlertDdsManager alertDdsManager;
    private HomeStatusDdsManager homeStatusDdsManager;
    private PresenceDdsManager presenceDdsManager;
    private VehicleStatusDdsManager vehicleStatusDdsManager;
    private MediaDdsManager mediaDdsManager; // 添加这一行
    private ReportMediaDdsManager reportMediaDdsManager;
    private EnergyRawDataDdsManager energyRawDataDdsManager;

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

    private AlertDdsManager.OnAlertReceivedListener alertReceivedListener;
    public void setOnAlertReceivedListener(AlertDdsManager.OnAlertReceivedListener listener) {
        this.alertDdsManager.setOnAlertReceivedListener(listener);
    }

    private OnVehicleStatusReceivedListener vehicleStatusReceivedListener;
    public interface OnVehicleStatusReceivedListener {
        void onVehicleStatusReceived(VehicleStatus vehicleStatus);
    }

    public void setOnVehicleStatusReceivedListener(OnVehicleStatusReceivedListener listener) {
        this.vehicleStatusReceivedListener = listener;
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

        // 设置HomeStatus消息监听器
        setupHomeStatusListener();

        // 设置VehicleStatus消息监听器
        setupVehicleStatusListener();

        // 设置EnergyRawData消息监听器
        setupEnergyRawDataListener();

        // 从数据库初始化已配对设备
        initializePairedDevices();

        // 启动设备超时检测
        startDeviceTimeoutDetection();
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

        // 初始化媒体管理器
        mediaDdsManager = new MediaDdsManager();
        // 获取应用内部存储路径
        String savePath = getFilesDir().getAbsolutePath() + "/media";
        File mediaDir = new File(savePath);
        if (!mediaDir.exists()) {
            mediaDir.mkdirs();
        }
        mediaDdsManager.initialize(baseDdsManager, savePath);

        reportMediaDdsManager = new ReportMediaDdsManager();
        reportMediaDdsManager.initialize(baseDdsManager, "");

        energyRawDataDdsManager = new EnergyRawDataDdsManager();
        energyRawDataDdsManager.initialize(baseDdsManager);

        // 设置媒体接收监听器
        setupMediaListener();
        setupReportMediaListener();
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
        Log.i(TAG, "handleDevicePresence");
        // 更新设备Presence信息
        updateDevicePresence(presence);

        // 检查是否是本设备发送的消息，避免处理自己的消息
        if (this.deviceId.equals(presence.deviceId)) {
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



    // 添加HomeStatus监听器接口
    public interface OnHomeStatusReceivedListener {
        void onHomeStatusReceived(HomeStatus homeStatus);
    }

    private OnHomeStatusReceivedListener homeStatusListener;

    public void setOnHomeStatusReceivedListener(OnHomeStatusReceivedListener listener) {
        this.homeStatusListener = listener;
    }

    private void setupHomeStatusListener() {
        homeStatusDdsManager.setOnHomeStatusReceivedListener(new HomeStatusDdsManager.OnHomeStatusReceivedListener() {
            @Override
            public void onHomeStatusReceived(idl.SmartDemo03.HomeStatus homeStatus) {
//                Log.d(TAG, "收到HomeStatus消息: " + homeStatus.deviceIds.toString() +
//                        ", " + homeStatus.deviceTypes.toString() + homeStatus.deviceStatus.get_at(0));

                // 通知监听器
                if (homeStatusListener != null) {
                    new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> {
                        homeStatusListener.onHomeStatusReceived(homeStatus);
                    });
                }
            }
        });
    }



    private void setupVehicleStatusListener() {
        vehicleStatusDdsManager.setOnVehicleStatusReceivedListener(new VehicleStatusDdsManager.OnVehicleStatusReceivedListener() {
            @Override
            public void onVehicleStatusReceived(idl.SmartDemo03.VehicleStatus vehicleStatus) {
                Log.d(TAG, "收到VehicleStatus消息: engineOn=" + vehicleStatus.engineOn +
                        ", fuelPercent=" + vehicleStatus.fuelPercent);

                // 处理VehicleStatus消息
                handleVehicleStatus(vehicleStatus);
            }
        });
    }

    private void handleVehicleStatus(idl.SmartDemo03.VehicleStatus vehicleStatus) {
        // 在这里处理收到的VehicleStatus消息
        Log.i(TAG, "处理VehicleStatus消息: " + vehicleStatus.toString());
        // 可以添加更多处理逻辑，例如更新UI或存储数据等
        // 通知监听器
        if (vehicleStatusReceivedListener != null) {
            new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> {
                vehicleStatusReceivedListener.onVehicleStatusReceived(vehicleStatus);
            });
        }
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
        Log.d(TAG, "terminate");
        if (isDeviceConnected) {
            presenceDdsManager.onDeviceLeft(deviceId, "SmartHomeDevice");
            Log.d(TAG, "发送设备离开消息");
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
            // 在后台线程中执行删除操作
            Log.d(TAG, "开始删除设备数据");
            AppDatabase.databaseWriteExecutor.execute(() -> {
                // 删除前检查记录数
                int beforeCount = database.deviceDao().getAllDevices().size();
                Log.d(TAG, "删除前设备数量: " + beforeCount);

                // 执行删除
                database.deviceDao().deleteAll();

                // 删除后检查记录数
                int afterCount = database.deviceDao().getAllDevices().size();
                Log.d(TAG, "删除后设备数量: " + afterCount);
                Log.d(TAG, "设备数据删除完成");
            });
        }

        super.onTerminate();
    }

    // 添加媒体接收监听器接口
    public interface OnMediaReceivedListener {
        void onMediaReceived(int alertId, Bitmap bitmap, String deviceId, String deviceType);
    }

    private OnMediaReceivedListener mediaReceivedListener;

    public void setOnMediaReceivedListener(OnMediaReceivedListener listener) {
        this.mediaReceivedListener = listener;
    }

    public interface OnReportMediaReceivedListener {
        void onReportMediaReceived(String reportId, Bitmap bitmap, String deviceId, String deviceType);
    }
    private OnReportMediaReceivedListener reportMediaReceivedListener;
    public void setOnReportMediaReceivedListener(OnReportMediaReceivedListener listener) {
        this.reportMediaReceivedListener = listener;
    }

    // 添加EnergyRawData监听器接口
    public interface OnEnergyRawDataReceivedListener {
        void onEnergyRawDataReceived(idl.SmartDemo03.EnergyRawData energyRawData);
    }
    private OnEnergyRawDataReceivedListener energyRawDataReceivedListener;
    public void setOnEnergyRawDataReceivedListener(OnEnergyRawDataReceivedListener listener) {
        this.energyRawDataReceivedListener = listener;
    }

    private void setupEnergyRawDataListener() {
        energyRawDataDdsManager.setOnEnergyRawDataReceivedListener(new EnergyRawDataDdsManager.OnEnergyRawDataReceivedListener() {
            @Override
            public void onEnergyRawDataReceived(idl.SmartDemo03.EnergyRawData energyRawData) {
                Log.d(TAG, "收到EnergyRawData消息");

                // 通知监听器
                if (energyRawDataReceivedListener != null) {
                    new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> {
                        energyRawDataReceivedListener.onEnergyRawDataReceived(energyRawData);
                    });
                }
            }
        });
    }

    private void setupMediaListener() {
        mediaDdsManager.setOnMediaReceivedListener(new MediaDdsManager.OnMediaReceivedListener() {
            @Override
            public void onMediaReceived(int alertId, Bitmap bitmap, String deviceId, String deviceType) {
                Log.d(TAG, "收到媒体数据: alertId=" + alertId + ", deviceId=" + deviceId + ", deviceType=" + deviceType);

                // 通知监听器
                if (mediaReceivedListener != null) {
                    mediaReceivedListener.onMediaReceived(alertId, bitmap, deviceId, deviceType);
                }
            }
        });
    }
    public void setupReportMediaListener() {
        reportMediaDdsManager.setOnMediaReceivedListener(new ReportMediaDdsManager.OnMediaReceivedListener() {
            @Override
            public void onMediaReceived(String reportId, Bitmap bitmap, String deviceId, String deviceType) {
                Log.d(TAG, "收到媒体数据: reportId: "+reportId+", deviceId= "+deviceId + ", deviceType=" + deviceType);
                if(reportMediaReceivedListener != null) {
                    reportMediaReceivedListener.onReportMediaReceived(reportId, bitmap, deviceId, deviceType);
                }
            }
        });
    }


    /**
     * 更新设备Presence信息和局域网状态
     * @param presence 设备Presence信息
     */
    private void updateDevicePresence(Presence presence) {
        if (presence.inRange) {
            // 设备在线，更新最后收到presence的时间和局域网状态
            devicePresenceMap.put(presence.deviceId, presence.timeStamp);
            deviceLanStatusMap.put(presence.deviceId, true);
            Log.d(TAG, "更新设备状态 - 设备在线: " + presence.deviceId + ", 时间戳: " + presence.timeStamp);
        } else {
            // 设备离线，更新局域网状态为false，但保留最后收到presence的时间用于超时检测
            deviceLanStatusMap.put(presence.deviceId, false);
            Log.d(TAG, "更新设备状态 - 设备离线: " + presence.deviceId);
        }
    }

    /**
     * 启动设备超时检测
     */
    private void startDeviceTimeoutDetection() {
        deviceTimeoutHandler.postDelayed(deviceTimeoutRunnable, 30000); // 30秒后开始检测
        Log.d(TAG, "启动设备超时检测");
    }

    /**
     * 检查设备是否超时
     */
    private void checkDeviceTimeouts() {
        Log.i(TAG, "checkDeviceTimeouts");
        long currentTime = System.currentTimeMillis();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        Iterator<Map.Entry<String, String>> iterator = devicePresenceMap.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<String, String> entry = iterator.next();
            String deviceId = entry.getKey();
            String presenceTimeStr = entry.getValue();

            try {
                // 将时间戳字符串转换为毫秒数
                Date presenceTime = format.parse(presenceTimeStr);

                // 如果距离上次收到presence消息超过30秒，则将局域网状态设为false
                if (currentTime - presenceTime.getTime() > DEVICE_TIMEOUT) {
                    deviceLanStatusMap.put(deviceId, false);
                    Log.d(TAG, "设备超时，局域网状态设为false: " + deviceId);
                }
            } catch (java.text.ParseException e) {
                Log.e(TAG, "解析时间戳失败: " + presenceTimeStr, e);
            }
        }
    }

    /**
     * 获取设备的局域网状态
     * @param deviceId 设备ID
     * @return 是否处于同一局域网
     */
    public boolean isDeviceInLan(String deviceId) {
        return deviceLanStatusMap.getOrDefault(deviceId, false);
    }

    /**
     * 获取所有配对设备的列表
     * @return 配对设备ID列表
     */
    public List<String> getPairedDevices() {
        return new ArrayList<>(devicePresenceMap.keySet());
    }
    /**
    * 在需要检查设备局域网状态的地方，可以通过以下方式获取：
    *SmartHomeApplication app = (SmartHomeApplication) getApplication();
     * boolean isInLan = app.isDeviceInLan(deviceId);
     * */


    /**
     * 从数据库初始化已配对的设备
     */
    private void initializePairedDevices() {
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                List<Device> pairedDevices = database.deviceDao().getAllDevices();
                for (Device device : pairedDevices) {
                    String deviceId = device.getDeviceId();
                    // 初始化两个哈希表，初始值设为默认值
                    devicePresenceMap.put(deviceId, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(0))); // 设置为初始时间
                    deviceLanStatusMap.put(deviceId, false); // 默认设置为不在线
                }
                Log.i(TAG, "从数据库初始化了 " + pairedDevices.size() + " 个已配对设备");
            } catch (Exception e) {
                Log.e(TAG, "从数据库初始化已配对设备时出错", e);
            }
        });
    }
}
