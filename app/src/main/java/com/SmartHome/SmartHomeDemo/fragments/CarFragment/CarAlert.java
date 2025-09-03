package com.SmartHome.SmartHomeDemo.fragments.CarFragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;

import com.SmartHome.SmartHomeDemo.R;
import com.google.android.material.button.MaterialButton;

public class CarAlert extends DialogFragment {

    private TextView carPartTextView;
    private TextView carLocationTextView;

    private String carPart = "";
    private String carLocation = "";


    // 按钮点击监听器
    public interface OnButtonClickListener {
        void onConfirmClick();
    }

    private OnButtonClickListener buttonClickListener;

    public CarAlert() {
    }

    public static CarAlert newInstance(String carPart, String carLocation) {
        CarAlert fragment = new CarAlert();
        Bundle args = new Bundle();
        args.putString("car_part", carPart);
        args.putString("car_location", carLocation);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            carPart = getArguments().getString("car_part", "");
            carLocation = getArguments().getString("car_location", "");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.window_car_alert, container, false);

        // 初始化视图组件
        carPartTextView = view.findViewById(R.id.alert_car_part);
        carLocationTextView = view.findViewById(R.id.alert_car_location);
        MaterialButton confirmButton = view.findViewById(R.id.alert_car_confirm);

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

        // 更新报警部件和车辆位置显示
        updateCarPart(carPart);
        updateCarLocation(carLocation);

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
     * 更新报警部件显示
     * @param carPart 报警部件
     */
    public void updateCarPart(String carPart) {
        this.carPart = carPart;
        if (carPartTextView != null) {
            carPartTextView.setText("报警部件: " + carPart);
        }
    }

    /**
     * 更新车辆位置显示
     * @param carLocation 车辆位置
     */
    public void updateCarLocation(String carLocation) {
        this.carLocation = carLocation;
        if (carLocationTextView != null) {
            carLocationTextView.setText("车辆位置: " + carLocation);
        }
    }
}
