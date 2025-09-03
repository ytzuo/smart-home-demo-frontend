package com.SmartHome.SmartHomeDemo.fragments.HomeFragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;

import com.SmartHome.SmartHomeDemo.R;
import com.google.android.material.button.MaterialButton;

public class FurnitureAlert extends DialogFragment {

    private TextView deviceIdTextView;
    private TextView deviceTypeTextView;

    private String deviceId = "";
    private String deviceType = "";


    // 按钮点击监听器
    public interface OnButtonClickListener {
        void onConfirmClick();
        void onViewAlertClick();
    }

    private OnButtonClickListener buttonClickListener;

    public FurnitureAlert() {
    }

    public static FurnitureAlert newInstance(String deviceId, String deviceType) {
        FurnitureAlert fragment = new FurnitureAlert();
        Bundle args = new Bundle();
        args.putString("device_id", deviceId);
        args.putString("device_type", deviceType);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            deviceId = getArguments().getString("device_id", "");
            deviceType = getArguments().getString("device_type", "");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // 注意：这里将attachToRoot设置为true，以确保布局正确显示
        View view = inflater.inflate(R.layout.window_furniture_alert, container, false);

        // 初始化视图组件
        deviceIdTextView = view.findViewById(R.id.alert_device_id);
        deviceTypeTextView = view.findViewById(R.id.alert_device_type);
        MaterialButton confirmButton = view.findViewById(R.id.alert_confirm);
        MaterialButton viewAlertButton = view.findViewById(R.id.view_alert);

        // 设置按钮点击事件
        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (buttonClickListener != null) {
                    buttonClickListener.onConfirmClick();
                } else {
                    dismiss(); // 默认关闭对话框
                }
            }
        });

        viewAlertButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (buttonClickListener != null) {
                    buttonClickListener.onViewAlertClick();
                } else {
                    dismiss(); // 默认关闭对话框
                }
            }
        });

        // 更新设备ID和设备类型显示
        updateDeviceId(deviceId);
        updateDeviceType(deviceType);

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        // 设置对话框的宽度，让它更宽一些
        if (getDialog() != null && getDialog().getWindow() != null) {
            int width = getResources().getDimensionPixelSize(R.dimen.alert_dialog_width);
            int height = ViewGroup.LayoutParams.WRAP_CONTENT;

            getDialog().getWindow().setLayout(width, height);
        }
    }

    /**
     * 设置按钮点击监听器
     * @param listener 监听器
     */
    public void setOnButtonClickListener(OnButtonClickListener listener) {
        this.buttonClickListener = listener;
    }

    /**
     * 更新设备ID显示
     * @param deviceId 设备ID
     */
    public void updateDeviceId(String deviceId) {
        this.deviceId = deviceId;
        if (deviceIdTextView != null) {
            deviceIdTextView.setText(getString(R.string.device_id) + ": " + deviceId);
        }
    }

    /**
     * 更新设备类型显示
     * @param deviceType 设备类型
     */
    public void updateDeviceType(String deviceType) {
        this.deviceType = deviceType;
        if (deviceTypeTextView != null) {
            deviceTypeTextView.setText(getString(R.string.device_type) + ": " + deviceType);
        }
    }
}
