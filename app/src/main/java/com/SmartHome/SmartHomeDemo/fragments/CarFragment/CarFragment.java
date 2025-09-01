package com.SmartHome.SmartHomeDemo.fragments.CarFragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.LiveData;

import com.SmartHome.SmartHomeDemo.R;
import com.google.android.material.button.MaterialButton;

import idl.SmartDemo03.VehicleStatus;

public class CarFragment extends Fragment {
    private CarViewModel carViewModel;
    private TextView carNameText;
    private TextView engineStatusText;
    private TextView fuelStatusText;
    private TextView mileStatusText;
    private MaterialButton engineButton;
    private MaterialButton lockButton;
    private MaterialButton acButton;

    public CarFragment(){}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_car, container, false);

        // 初始化ViewModel
        carViewModel = new ViewModelProvider(this).get(CarViewModel.class);

        // 获取UI元素引用
        carNameText = view.findViewById(R.id.car_name);
        engineStatusText = view.findViewById(R.id.car_engine_status);
        fuelStatusText = view.findViewById(R.id.car_fuel_status);
        mileStatusText = view.findViewById(R.id.car_mile_status);
        engineButton = view.findViewById(R.id.control_car_engine);
        lockButton = view.findViewById(R.id.control_car_lock);
        acButton = view.findViewById(R.id.control_car_ac);

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

        return view;
    }

    public void updateVehicleStatus(VehicleStatus vehicleStatus) {
        if (carViewModel != null) {
            // 根据VehicleStatus创建新的CarItem
            CarItem carItem = new CarItem(
                    "我的汽车",  // 汽车名称
                    "车辆名称："+(vehicleStatus.engineOn ? "运行中" : "已熄火"),  // 发动机状态
                    "剩余油量："+String.format("%.1f%%", vehicleStatus.fuelPercent),  // 燃油百分比
                    vehicleStatus.location,
                    vehicleStatus.doorsLocked,
                    vehicleStatus.engineOn,  // 发动机状态
                    false   // 空调状态无法从VehicleStatus获取
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
        } else {
            engineButton.setText(R.string.car_engine_on);
            // 设置图标为灰色表示关闭状态
            engineButton.setIconTint(getResources().getColorStateList(R.color.gray, null));
        }

        if (carItem.isLock()) {
            lockButton.setText(R.string.car_unlock);
            // 设置图标为橙色表示锁定状态
            lockButton.setIconTint(getResources().getColorStateList(R.color.orange, null));
        } else {
            lockButton.setText(R.string.car_lock);
            // 设置图标为灰色表示未锁定状态
            lockButton.setIconTint(getResources().getColorStateList(R.color.gray, null));
        }

        if (carItem.isAcOn()) {
            acButton.setText(R.string.car_ac_pff);
            // 设置图标为蓝色表示开启状态
            acButton.setIconTint(getResources().getColorStateList(R.color.blue, null));
        } else {
            acButton.setText(R.string.car_ac_on);
            // 设置图标为灰色表示关闭状态
            acButton.setIconTint(getResources().getColorStateList(R.color.gray, null));
        }
    }

}
