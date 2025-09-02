package com.SmartHome.SmartHomeDemo.fragments.CarFragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.LiveData;

import com.SmartHome.SmartHomeDemo.R;
import com.SmartHome.SmartHomeDemo.application.SmartHomeApplication;
import com.SmartHome.SmartHomeDemo.dds.CommandDdsManager;
import com.google.android.material.button.MaterialButton;

import idl.SmartDemo03.Command;
import idl.SmartDemo03.VehicleStatus;

public class CarFragment extends Fragment {
    private String TAG = "CarFragment";
    private CarViewModel carViewModel;
    private TextView carNameText;
    private TextView engineStatusText;
    private TextView lockStatusText;
    private TextView acStatusText;
    private TextView fuelStatusText;
    private TextView mileStatusText;
    private MaterialButton engineButton;
    private MaterialButton lockButton;
    private MaterialButton acButton;

    private SmartHomeApplication app;

    public CarFragment(){}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 在onCreate中初始化SmartHomeApplication成员变量
        app = (SmartHomeApplication) getActivity().getApplication();
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_car, container, false);

        // 初始化ViewModel
        carViewModel = new ViewModelProvider(this).get(CarViewModel.class);

        // 获取UI元素引用
        carNameText      = view.findViewById(R.id.car_name);
        engineStatusText = view.findViewById(R.id.car_engine_status);
        lockStatusText   = view.findViewById(R.id.car_lock_status);
        acStatusText     = view.findViewById(R.id.car_ac_status);
        fuelStatusText   = view.findViewById(R.id.car_fuel_status);
        mileStatusText   = view.findViewById(R.id.car_mile_status);
        engineButton     = view.findViewById(R.id.control_car_engine);
        lockButton       = view.findViewById(R.id.control_car_lock);
        acButton         = view.findViewById(R.id.control_car_ac);

        // 观察LiveData变化并更新UI
        LiveData<CarItem> carLiveData = carViewModel.getCarLiveData();
        if (carLiveData != null) {
            carLiveData.observe(getViewLifecycleOwner(), carItem -> {
                if (carItem != null) {
                    carNameText.setText(carItem.getCarName());
                    engineStatusText.setText(carItem.getEngineStatus());
                    fuelStatusText.setText(carItem.getFuel());
                    mileStatusText.setText(carItem.getLocation());

                    // 可以根据状态更新按钮UI等其他操作
                    updateControlButtons(carItem);
                }
            });
        }

        setButtonClickListeners();

        return view;
    }

    public void updateVehicleStatus(VehicleStatus vehicleStatus) {
        if (carViewModel != null) {
            // 根据VehicleStatus创建新的CarItem
            CarItem carItem = new CarItem(
                    "我的汽车",  // 汽车名称
                    "剩余油量："+String.format("%.1f%%", vehicleStatus.fuelPercent),  // 燃油百分比
                    vehicleStatus.location,
                    vehicleStatus.doorsLocked, // 车门状态
                    vehicleStatus.engineOn,  // 发动机状态
                    vehicleStatus.acOn   // 空调状态
            );

            // 更新ViewModel中的数据
            carViewModel.updateCarItem(carItem);
        }
    }

    private void updateControlButtons(CarItem carItem) {
        // 根据carItem中的状态更新控制按钮的UI
        if (carItem.isEngineOn()) {
            engineButton.setText(R.string.car_engine_off);
            // 设置图标为绿色表示开启状态
            engineButton.setIconTint(getResources().getColorStateList(R.color.green, null));
            engineStatusText.setText(R.string.enginOn);
        } else {
            engineButton.setText(R.string.car_engine_on);
            // 设置图标为灰色表示关闭状态
            engineButton.setIconTint(getResources().getColorStateList(R.color.gray, null));
            engineStatusText.setText(R.string.enginOff);
        }

        if (carItem.isLock()) {
            lockButton.setText(R.string.car_unlock);
            // 设置图标为橙色表示锁定状态
            lockButton.setIconTint(getResources().getColorStateList(R.color.orange, null));
            lockStatusText.setText(R.string.lock);
        } else {
            lockButton.setText(R.string.car_lock);
            // 设置图标为灰色表示未锁定状态
            lockButton.setIconTint(getResources().getColorStateList(R.color.gray, null));
            lockStatusText.setText(R.string.unlock);
        }

        if (carItem.isAcOn()) {
            acButton.setText(R.string.car_ac_pff);
            // 设置图标为蓝色表示开启状态
            acButton.setIconTint(getResources().getColorStateList(R.color.blue, null));
            acStatusText.setText(R.string.acON);
        } else {
            acButton.setText(R.string.car_ac_on);
            // 设置图标为灰色表示关闭状态
            acButton.setIconTint(getResources().getColorStateList(R.color.gray, null));
            acStatusText.setText(R.string.acOFF);
        }
    }

    private void setButtonClickListeners() {
        engineButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 直接使用成员变量app
                CommandDdsManager commandDdsManager = app.getCommandDdsManager();

                // 发送控制发动机的命令
                Command command = new Command();
                command.deviceId = "My Car";
                command.deviceType = "car";
                if(engineButton.getText() == getString(R.string.car_engine_on)) {
                    command.action = "engine_on";
                    engineButton.setText(R.string.car_engine_off);
                    // 设置图标为绿色表示开启状态
                    engineButton.setIconTint(getResources().getColorStateList(R.color.green, null));
                    engineStatusText.setText(R.string.enginOn);
                } else {
                    command.action = "engine_off";
                    engineButton.setText(R.string.car_engine_on);
                    // 设置图标为灰色表示关闭状态
                    engineButton.setIconTint(getResources().getColorStateList(R.color.gray, null));
                    engineStatusText.setText(R.string.enginOff);
                }
                command.value = 0;
                command.timeStamp =  String.valueOf(System.currentTimeMillis());
                commandDdsManager.sendCommand(command);

                Log.i(TAG, "发送命令" + command.action);
                Toast.makeText(getContext(), "发动机控制按钮被点击", Toast.LENGTH_SHORT).show();
            }
        });

        lockButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 直接使用成员变量app
                CommandDdsManager commandDdsManager = app.getCommandDdsManager();

                // 发送控制门锁的命令
                Command command = new Command();
                command.deviceId = "My Car";
                command.deviceType = "car";
                if(lockButton.getText() == getString(R.string.car_lock)) {
                    command.action = "lock";
                    lockButton.setText(R.string.car_unlock);
                    // 设置图标为橙色表示锁定状态
                    lockButton.setIconTint(getResources().getColorStateList(R.color.orange, null));
                    lockStatusText.setText(R.string.lock);
                } else {
                    command.action = "unlock";
                    lockButton.setText(R.string.car_lock);
                    // 设置图标为灰色表示未锁定状态
                    lockButton.setIconTint(getResources().getColorStateList(R.color.gray, null));
                    lockStatusText.setText(R.string.unlock);
                }
                command.value = 0;
                command.timeStamp =  String.valueOf(System.currentTimeMillis());
                commandDdsManager.sendCommand(command);

                Log.i(TAG, "发送命令" + command.action);
                Toast.makeText(getContext(), "门锁控制按钮被点击", Toast.LENGTH_SHORT).show();
            }
        });

        acButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 直接使用成员变量app
                CommandDdsManager commandDdsManager = app.getCommandDdsManager();

                // TODO: 发送控制空调的命令 需要和后端指定的命令action统一
                Command command = new Command();
                command.deviceId = "My Car";
                command.deviceType = "car";
                if(acButton.getText() == getString(R.string.car_ac_on)) {
                    command.action = "ac_on";
                    acButton.setText(R.string.car_ac_pff);
                    // 设置图标为蓝色表示开启状态
                    acButton.setIconTint(getResources().getColorStateList(R.color.blue, null));
                    acStatusText.setText(R.string.acON);
                } else {
                    command.action = "ac_off";
                    acButton.setText(R.string.car_ac_on);
                    // 设置图标为灰色表示关闭状态
                    acButton.setIconTint(getResources().getColorStateList(R.color.gray, null));
                    acStatusText.setText(R.string.acOFF);
                }
                command.value = 0;
                command.timeStamp =  String.valueOf(System.currentTimeMillis());
                commandDdsManager.sendCommand(command);

                Log.i(TAG, "发送命令" + command.action);
                Toast.makeText(getContext(), "空调控制按钮被点击", Toast.LENGTH_SHORT).show();
            }
        });
    }

}
