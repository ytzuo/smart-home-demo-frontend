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

public class CarFragment extends Fragment {
    private CarViewModel carViewModel;
    private TextView carNameText;
    private TextView engineStatusText;
    private TextView fuelStatusText;
    private TextView mileStatusText;

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

        // 观察LiveData变化并更新UI
        LiveData<CarItem> carLiveData = carViewModel.getCarLiveData();
        if (carLiveData != null) {
            carLiveData.observe(getViewLifecycleOwner(), carItem -> {
                if (carItem != null) {
                    carNameText.setText(carItem.getCarName());
                    engineStatusText.setText(carItem.getEngineStatus());
                    fuelStatusText.setText(carItem.getFuel());
                    mileStatusText.setText(carItem.getMile());

                    // 可以根据状态更新按钮UI等其他操作
                    updateControlButtons(carItem);
                }
            });
        }

        return view;
    }

    private void updateControlButtons(CarItem carItem) {
        // 根据carItem中的状态更新控制按钮的UI
        // 这里可以添加具体的按钮状态更新逻辑
    }
}
