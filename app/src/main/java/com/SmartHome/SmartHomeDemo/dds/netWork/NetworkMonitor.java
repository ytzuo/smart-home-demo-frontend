package com.SmartHome.SmartHomeDemo.dds.netWork;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;

import com.SmartHome.SmartHomeDemo.dds.PresenceDdsManager;

public class NetworkMonitor {
    private ConnectivityManager connectivityManager;
    private ConnectivityManager.NetworkCallback networkCallback;
    private PresenceDdsManager presenceManager;
    private String deviceId;
    private String deviceType;
    private boolean isConnected = false;

    public NetworkMonitor(Context context, PresenceDdsManager presenceManager,
                          String deviceId, String deviceType) {
        this.presenceManager = presenceManager;
        this.deviceId = deviceId;
        this.deviceType = deviceType;

        connectivityManager = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);

        setupNetworkCallback();
    }

    private void setupNetworkCallback() {
        networkCallback = new ConnectivityManager.NetworkCallback() {
            @Override
            public void onAvailable(Network network) {
                super.onAvailable(network);
                // 网络连接可用
                if (!isConnected) {
                    isConnected = true;
                    // 设备连接到网络，发布加入消息
                    presenceManager.onDeviceJoined(deviceId, deviceType);
                }
            }

            @Override
            public void onLost(Network network) {
                super.onLost(network);
                // 网络连接丢失
                if (isConnected) {
                    isConnected = false;
                    // 设备失去网络连接，发布离开消息
                    presenceManager.onDeviceLeft(deviceId, deviceType);
                }
            }

            @Override
            public void onCapabilitiesChanged(Network network, NetworkCapabilities networkCapabilities) {
                super.onCapabilitiesChanged(network, networkCapabilities);
                // 网络能力发生变化
                if (networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                        networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)) {
                    if (!isConnected) {
                        isConnected = true;
                        presenceManager.onDeviceJoined(deviceId, deviceType);
                    }
                }
            }
        };
    }

    public void startMonitoring() {
        NetworkRequest networkRequest = new NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
                .build();

        connectivityManager.registerNetworkCallback(networkRequest, networkCallback);
    }

    public void stopMonitoring() {
        if (networkCallback != null) {
            connectivityManager.unregisterNetworkCallback(networkCallback);
        }
    }
}
