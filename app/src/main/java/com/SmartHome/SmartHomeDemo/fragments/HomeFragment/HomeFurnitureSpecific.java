package com.SmartHome.SmartHomeDemo.fragments.HomeFragment;

import static java.security.AccessController.getContext;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import com.SmartHome.SmartHomeDemo.R;
import com.SmartHome.SmartHomeDemo.utils.ToastUtil;

import idl.SmartDemo03.Presence;

public class HomeFurnitureSpecific extends Fragment {

    private FurnitureItem furnitureItem;

    public HomeFurnitureSpecific() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_furniture_specific, container, false);

        // 获取传递的参数
        if (getArguments() != null) {
            furnitureItem = (FurnitureItem) getArguments().getSerializable("furniture_item");
        }

        initialization(view);
        setupEventListeners(view);
        refreshUI(view);

        return view;
    }

    private void initialization(View view){
        TextView temp;
        temp = view.findViewById(R.id.Title);
        temp.setText(furnitureItem.getDeviceId());

        switch (furnitureItem.getDeviceType()) {
            case "air_conditioner":
                //设置图标
                ImageView tempImage = view.findViewById(R.id.iv_furniture);
                tempImage.setImageResource(R.drawable.icon_air_conditioner);

                //设置参数项文本
                temp = view.findViewById(R.id.arg1);
                temp.setText("状态：");
                temp = view.findViewById(R.id.arg2);
                temp.setText("当前温度：");

                //设置开关项文本
                temp = view.findViewById(R.id.switch_label_11);
                temp.setText("开关");
                temp = view.findViewById(R.id.switch_label_12);
                temp.setText("制冷");
                temp = view.findViewById(R.id.switch_label_13);
                temp.setText("扫风");
                temp = view.findViewById(R.id.switch_label_14);
                temp.setText("除湿");

                // 设置滑块项文本
                temp = view.findViewById(R.id.seekbar_label_1);
                temp.setText("温度");

                hideUnusedItems(view);

                break;

            case "light":
                //设置图标
                tempImage = view.findViewById(R.id.iv_furniture);
                tempImage.setImageResource(R.drawable.icon_light);

                //设置参数项文本
                temp = view.findViewById(R.id.arg1);
                temp.setText("状态：");
                temp = view.findViewById(R.id.arg2);
                temp.setText("亮度：");

                //设置开关项文本
                temp = view.findViewById(R.id.switch_label_11);
                temp.setText("开关");

                // 设置滑块项文本
                temp = view.findViewById(R.id.seekbar_label_1);
                temp.setText("亮度");

            default:
                // 默认情况下隐藏所有未使用的设置项
                hideUnusedItems(view);
                break;
        }

    }
    private void refreshUI(View view){
        TextView arg1TextView = view.findViewById(R.id.arg1_item);
        Switch switch11 = view.findViewById(R.id.switch_11);

        if (switch11 != null && arg1TextView != null) {
            if (switch11.isChecked()) {
                arg1TextView.setText("正常运行");
            } else {
                arg1TextView.setText("停止工作");
            }
        }

        // 根据FurnitureItem中的switchStatus更新开关状态
        if (furnitureItem != null && furnitureItem.getSwitchStatus() != null) {
            String switchStatus = furnitureItem.getSwitchStatus();

            // 确保switchStatus是8位长度
            if (switchStatus.length() == 8) {
                // 遍历所有开关并根据switchStatus设置状态
                for (int i = 1; i <= 2; i++) {
                    for (int j = 1; j <= 4; j++) {
                        int switchId = getResources().getIdentifier("switch_" + i + j, "id", requireContext().getPackageName());
                        Switch switchView = view.findViewById(switchId);

                        // 计算在switchStatus中的位置 (从左到右)
                        int position = (i - 1) * 4 + (j - 1);

                        if (switchView != null && position < switchStatus.length()) {
                            char statusChar = switchStatus.charAt(position);
                            // 禁用监听器以避免在更新UI时触发事件
                            switchView.setOnCheckedChangeListener(null);
                            switchView.setChecked(statusChar == '1');
                            // 重新启用监听器
                            final int finalI = i;
                            final int finalJ = j;
                            switchView.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                                @Override
                                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                                    handleSwitchChange(buttonView, isChecked);
                                }
                            });
                        }
                    }
                }
            }
        }

        switch (furnitureItem.getDeviceType()) {
            case "air_conditioner":
                // 设置参数项2显示空调当前温度
                TextView arg2TextView = view.findViewById(R.id.arg2_item);
                if (arg2TextView != null) {
                    arg2TextView.setText(furnitureItem.getAcTemp() + "℃");
                }

                // 根据acTemp调整滑块位置，acTemp范围为15-30
                SeekBar tempSeekBar = view.findViewById(R.id.seekbar_1);
                if (tempSeekBar != null) {
                    // 移除监听器以避免触发事件
                    tempSeekBar.setOnSeekBarChangeListener(null);

                    // 将实际温度值(15-30)映射到滑块范围(0-100)
                    float acTemp = furnitureItem.getAcTemp();
                    int progress = Math.round((acTemp - 15) / (30 - 15) * 100);
                    // 确保进度值在有效范围内
                    progress = Math.max(0, Math.min(100, progress));
                    tempSeekBar.setProgress(progress);

                    // 重新设置监听器
                    tempSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                        @Override
                        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                            if(fromUser) {
                                handleSeekBarChange(seekBar, progress);
                            }
                        }

                        @Override
                        public void onStartTrackingTouch(SeekBar seekBar) {
                        }

                        @Override
                        public void onStopTrackingTouch(SeekBar seekBar) {
                        }
                    });
                }
                break;

            case "light":
                // 设置参数项2显示灯光亮度
                TextView lightArg2TextView = view.findViewById(R.id.arg2_item);
                if (lightArg2TextView != null) {
                    lightArg2TextView.setText((int)furnitureItem.getLightPercent() + "%");
                }

                // 根据lightPercent调整滑块位置，lightPercent范围为10-100
                SeekBar brightnessSeekBar = view.findViewById(R.id.seekbar_1);
                if (brightnessSeekBar != null) {
                    // 移除监听器以避免触发事件
                    brightnessSeekBar.setOnSeekBarChangeListener(null);

                    // 将实际亮度值(10-100)映射到滑块范围(0-100)
                    float lightPercent = furnitureItem.getLightPercent();
                    int progress = Math.round((lightPercent - 10) / (100 - 10) * 100);
                    // 确保进度值在有效范围内
                    progress = Math.max(0, Math.min(100, progress));
                    brightnessSeekBar.setProgress(progress);

                    // 重新设置监听器
                    brightnessSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                        @Override
                        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                            if(fromUser) {
                                handleSeekBarChange(seekBar, progress);
                            }
                        }

                        @Override
                        public void onStartTrackingTouch(SeekBar seekBar) {
                        }

                        @Override
                        public void onStopTrackingTouch(SeekBar seekBar) {
                        }
                    });
                }
                break;
        }
    }

    private void hideUnusedItems(View view) {
        // 隐藏未使用的开关项
        for(int i = 1; i <= 2; i++) {
            for(int j = 1; j <= 4; j++) {
                // 构造开关标签ID
                int labelId = getResources().getIdentifier("switch_label_" + i + j, "id", requireContext().getPackageName());

                // 检查ID是否存在
                if(labelId != 0) {
                    TextView label = view.findViewById(labelId);
                    if(label != null) {
                        // 如果标签文本包含"设置项"，则隐藏该项
                        if(label.getText().toString().contains("设置项")) {
                            // 获取包含TextView和Switch的LinearLayout并隐藏
                            LinearLayout parentLayout = (LinearLayout) label.getParent();
                            parentLayout.setVisibility(View.GONE);
                        }
                    }
                }
            }
        }

        // 隐藏未使用的滑块项
        for(int i = 1; i <= 3; i++) {
            // 构造滑块标签ID
            int labelId = getResources().getIdentifier("seekbar_label_" + i, "id", requireContext().getPackageName());

            // 检查ID是否存在
            if(labelId != 0) {
                TextView label = view.findViewById(labelId);
                if(label != null) {
                    // 如果标签文本包含"滑块"，则隐藏该项
                    if(label.getText().toString().contains("滑块")) {
                        // 获取包含TextView和SeekBar的LinearLayout并隐藏
                        LinearLayout parentLayout = (LinearLayout) label.getParent();
                        parentLayout.setVisibility(View.GONE);
                    }
                }
            }
        }

        //隐藏未使用的参数项
        for(int i = 1; i <= 4; i++) {
            // 构造滑块标签ID
            int labelId = getResources().getIdentifier("arg" + i, "id", requireContext().getPackageName());

            // 检查ID是否存在
            if(labelId != 0) {
                TextView label = view.findViewById(labelId);
                if(label != null) {
                    // 如果标签文本包含"滑块"，则隐藏该项
                    if(label.getText().toString().contains("参数项")) {
                        LinearLayout parentLayout = (LinearLayout) label.getParent();
                        parentLayout.setVisibility(View.GONE);
                    }
                }
            }
        }
    }

    private void setupEventListeners(View view) {
        // 为所有开关设置监听器
        for(int i = 1; i <= 2; i++) {
            for(int j = 1; j <= 4; j++) {
                int switchId = getResources().getIdentifier("switch_" + i + j, "id", requireContext().getPackageName());
                Switch switchView = view.findViewById(switchId);

                if(switchView != null) {
                    // 禁用监听器以避免在设置初始状态时触发事件
                    switchView.setOnCheckedChangeListener(null);
                    switchView.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                            handleSwitchChange(buttonView, isChecked);
                        }
                    });
                }
            }
        }

        // 为所有滑块设置监听器
        for(int i = 1; i <= 3; i++) {
            int seekBarId = getResources().getIdentifier("seekbar_" + i, "id", requireContext().getPackageName());
            SeekBar seekBar = view.findViewById(seekBarId);

            if(seekBar != null) {
                seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        if(fromUser) {
                            handleSeekBarChange(seekBar, progress);
                        }
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {
                        // 可选：处理开始拖动事件
                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                        // 可选：处理停止拖动事件
                    }
                });
            }
        }

        //为返回按钮添加监听器
        TextView backButton = view.findViewById(R.id.btn_back);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("HomeFurnitureSpecific", "返回按钮被点击");
                if (getFragmentManager() != null) {
                    getFragmentManager().popBackStack();
                }
            }
        });

        //为解绑按钮添加监听器
        Button unbindButton = view.findViewById(R.id.unbind_device);
        if (unbindButton != null) {
            unbindButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // 创建一个临时的Presence对象用于演示
                    // 在实际应用中，您需要从furnitureItem获取真实数据
                    Presence presence = new Presence();
                    presence.deviceId = furnitureItem.getDeviceId();
                    presence.deviceType = furnitureItem.getDeviceType();
                    showUnbindDialog(presence, view);
                }
            });
        }
    }

    private void handleSwitchChange(CompoundButton switchView, boolean isChecked) {
        // 处理开关状态变化
        String switchName = getResources().getResourceEntryName(switchView.getId());
        // 这里可以添加实际的设备控制逻辑
        if (isAdded()) {
            ToastUtil.showToast(requireContext(), switchName + " 状态: " + (isChecked ? "开启" : "关闭"), Toast.LENGTH_SHORT);
        }
    }

    private void handleSeekBarChange(SeekBar seekBar, int progress) {
        // 处理滑块值变化
        String seekBarName = getResources().getResourceEntryName(seekBar.getId());
        // 这里可以添加实际的设备控制逻辑
        if (isAdded()) {
            ToastUtil.showToast(requireContext(), seekBarName + " 值: " + progress, Toast.LENGTH_SHORT);
        }
    }

    public void triggerAlarm() {
        if (getView() != null) {
            TextView arg1TextView = getView().findViewById(R.id.arg1);
            if (arg1TextView != null) {
                arg1TextView.setText("状态：报警");
            }

            // 可以在这里添加其他报警相关的UI更新
            if (isAdded()) {
                ToastUtil.showToast(requireContext(), "设备报警！", Toast.LENGTH_SHORT);
            }
        }
    }

    private void sendRequestSwitch(){

    }

    private void sendRequestSeekbar(){

    }

    private void showUnbindDialog(Presence presence, View view) {
        // 加载对话框布局
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        View dialogView = inflater.inflate(R.layout.window_unbind_confirm, null);

        // 更新对话框中的文本
        TextView deviceIdText = dialogView.findViewById(R.id.unbind_device_id);
        TextView deviceTypeText = dialogView.findViewById(R.id.unbind_device_type);

        if (deviceIdText != null) {
            deviceIdText.setText(getString(R.string.device_id) + ": " + presence.deviceId);
        }

        if (deviceTypeText != null) {
            deviceTypeText.setText(getString(R.string.device_type) + ": " + presence.deviceType);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();

        // 设置按钮点击事件
        if (dialogView.findViewById(R.id.unbind_done) != null) {
            dialogView.findViewById(R.id.unbind_done).setOnClickListener(v -> {
                //TODO 用户点击确认，将设备从数据库删除
                dialog.dismiss();
            });
        }

        if (dialogView.findViewById(R.id.unbind_cancel) != null) {
            dialogView.findViewById(R.id.unbind_cancel).setOnClickListener(v -> {
                // 用户点击取消
                dialog.dismiss();
            });
        }

        dialog.show();
    }
}
