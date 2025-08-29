package com.SmartHome.SmartHomeDemo.fragments.HomeFragment;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.*;

import com.SmartHome.SmartHomeDemo.R;

public class HomeFurnitureSpecific extends AppCompatActivity {

    private FurnitureItem furnitureItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_furniture_specific);
//        Intent intent = getIntent();
//        furnitureItem = (FurnitureItem) intent.getSerializableExtra("furniture_item");

        furnitureItem = new FurnitureItem("智能空调1", "air_conditioner", "工作中",
                "制冷 24℃", "今天 10:15", R.drawable.icon_air_conditioner,24,"10100000",false,50);

        initialization();
        setupEventListeners();
        refreshUI();
    }

    private void initialization(){

        TextView temp;

        switch (furnitureItem.getDeviceType()) {
            case "air_conditioner":
                //设置图标
                ImageView tempImage = findViewById(R.id.iv_furniture);
                tempImage.setImageResource(R.drawable.icon_air_conditioner);

                //设置参数项文本
                temp = findViewById(R.id.arg1);
                temp.setText("状态");
                temp = findViewById(R.id.arg2);
                temp.setText("当前温度");

                //设置开关项文本
                temp = findViewById(R.id.switch_label_11);
                temp.setText("开关");
                temp = findViewById(R.id.switch_label_12);
                temp.setText("制冷");
                temp = findViewById(R.id.switch_label_13);
                temp.setText("扫风");
                temp = findViewById(R.id.switch_label_14);
                temp.setText("除湿");

                // 设置滑块项文本
                temp = findViewById(R.id.seekbar_label_1);
                temp.setText("温度");
//                temp = findViewById(R.id.seekbar_label_2);
//                temp.setText("风速");

                hideUnusedItems();

                break;

            case "light":
                //设置图标
                tempImage = findViewById(R.id.iv_furniture);
                tempImage.setImageResource(R.drawable.icon_light);

                //设置参数项文本
                temp = findViewById(R.id.arg1);
                temp.setText("状态");
                temp = findViewById(R.id.arg2);
                temp.setText("亮度");

                //设置开关项文本
                temp = findViewById(R.id.switch_label_11);
                temp.setText("开关");

                // 设置滑块项文本
                temp = findViewById(R.id.seekbar_label_1);
                temp.setText("亮度");

            default:
                // 默认情况下隐藏所有未使用的设置项
                hideUnusedItems();
                break;
        }

    }

    private void hideUnusedItems() {
        // 隐藏未使用的开关项
        for(int i = 1; i <= 2; i++) {
            for(int j = 1; j <= 4; j++) {
                // 构造开关标签ID
                int labelId = getResources().getIdentifier("switch_label_" + i + j, "id", getPackageName());

                // 检查ID是否存在
                if(labelId != 0) {
                    TextView label = findViewById(labelId);
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
            int labelId = getResources().getIdentifier("seekbar_label_" + i, "id", getPackageName());

            // 检查ID是否存在
            if(labelId != 0) {
                TextView label = findViewById(labelId);
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
            int labelId = getResources().getIdentifier("arg" + i, "id", getPackageName());

            // 检查ID是否存在
            if(labelId != 0) {
                TextView label = findViewById(labelId);
                if(label != null) {
                    // 如果标签文本包含"滑块"，则隐藏该项
                    if(label.getText().toString().contains("参数项")) {
                        label.setVisibility(View.GONE);
                    }
                }
            }
        }
    }


    private void setupEventListeners() {
        // 为所有开关设置监听器
        for(int i = 1; i <= 2; i++) {
            for(int j = 1; j <= 4; j++) {
                int switchId = getResources().getIdentifier("switch_" + i + j, "id", getPackageName());
                Switch switchView = findViewById(switchId);

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
            int seekBarId = getResources().getIdentifier("seekbar_" + i, "id", getPackageName());
            SeekBar seekBar = findViewById(seekBarId);

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
    }

    private void handleSwitchChange(CompoundButton switchView, boolean isChecked) {
        // 处理开关状态变化
        String switchName = getResources().getResourceEntryName(switchView.getId());
        // 这里可以添加实际的设备控制逻辑
        Toast.makeText(this, switchName + " 状态: " + (isChecked ? "开启" : "关闭"), Toast.LENGTH_SHORT).show();
    }

    private void handleSeekBarChange(SeekBar seekBar, int progress) {
        // 处理滑块值变化
        String seekBarName = getResources().getResourceEntryName(seekBar.getId());
        // 这里可以添加实际的设备控制逻辑
        Toast.makeText(this, seekBarName + " 值: " + progress, Toast.LENGTH_SHORT).show();
    }
    private void refreshUI(){
        // 根据FurnitureItem中的switchStatus更新开关状态
        if (furnitureItem != null && furnitureItem.getSwitchStatus() != null) {
            String switchStatus = furnitureItem.getSwitchStatus();

            // 确保switchStatus是8位长度
            if (switchStatus.length() == 8) {
                // 遍历所有开关并根据switchStatus设置状态
                for (int i = 1; i <= 2; i++) {
                    for (int j = 1; j <= 4; j++) {
                        int switchId = getResources().getIdentifier("switch_" + i + j, "id", getPackageName());
                        Switch switchView = findViewById(switchId);

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
                // 根据acTemp调整滑块位置，acTemp范围为15-30
                SeekBar tempSeekBar = findViewById(R.id.seekbar_1);
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
                // 根据lightPercent调整滑块位置，lightPercent范围为10-100
                SeekBar brightnessSeekBar = findViewById(R.id.seekbar_1);
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

    private void sendRequestSwitch(){

    }

    private void sendRequestSeekbar(){

    }
}
