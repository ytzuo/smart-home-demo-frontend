    package com.SmartHome.SmartHomeDemo.fragments.HomeFragment;

    import static java.security.AccessController.getContext;

    import androidx.annotation.Nullable;
    import androidx.appcompat.app.AlertDialog;
    import androidx.appcompat.widget.PopupMenu;
    import androidx.fragment.app.Fragment;
    import androidx.lifecycle.ViewModelProvider;

    import android.content.Intent;
    import android.os.Build;
    import android.os.Bundle;
    import android.util.Log;
    import android.view.LayoutInflater;
    import android.view.MenuItem;
    import android.view.View;
    import android.view.ViewGroup;
    import android.widget.*;

    import com.SmartHome.SmartHomeDemo.R;
    import com.SmartHome.SmartHomeDemo.application.SmartHomeApplication;
    import com.SmartHome.SmartHomeDemo.dds.BaseDdsManager;
    import com.SmartHome.SmartHomeDemo.dds.CommandDdsManager;
    import com.SmartHome.SmartHomeDemo.utils.ToastUtil;
    import com.google.android.material.button.MaterialButton;

    import java.time.LocalDateTime;

    import idl.SmartDemo03.Presence;

    public class HomeFurnitureSpecific extends Fragment {
        private String TAG = "HomeFurnitureSpecific";

        private FurnitureItem furnitureItem;

        private FurnitureItemViewModel viewModel;

        private SmartHomeApplication app;
        private CommandDdsManager commandDdsManager;

        public HomeFurnitureSpecific() {
            // Required empty public constructor
        }

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            // 初始化ViewModel
            viewModel = new ViewModelProvider(this).get(FurnitureItemViewModel.class);

            app = (SmartHomeApplication) getActivity().getApplication();
            commandDdsManager = app.getCommandDdsManager();
            Log.i(TAG, "commandDdsManager初始化完成");

            // 禁用底部导航栏
            if (getActivity() != null) {
                com.google.android.material.bottomnavigation.BottomNavigationView bottomNav =
                        getActivity().findViewById(R.id.bottom_navigation);
                if (bottomNav != null) {
                    bottomNav.setVisibility(View.GONE);
                }
            }
        }

        @Override
        public void onDestroy() {
            super.onDestroy();

            // 恢复底部导航栏的可见性
            if (getActivity() != null) {
                com.google.android.material.bottomnavigation.BottomNavigationView bottomNav =
                        getActivity().findViewById(R.id.bottom_navigation);
                if (bottomNav != null) {
                    bottomNav.setVisibility(View.VISIBLE);
                }
            }
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            // Inflate the layout for this fragment
            View view = inflater.inflate(R.layout.fragment_furniture_specific, container, false);

            // 获取传递的参数
            if (getArguments() != null) {
                furnitureItem = (FurnitureItem) getArguments().getSerializable("furniture_item");
                Log.i(TAG, furnitureItem.getStatus());
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


            ImageButton menuButton = view.findViewById(R.id.menu_button);
            menuButton.setOnClickListener(v -> showPopupMenu(v));

            switch (furnitureItem.getDeviceType()) {
                case "air_conditioner":
                    //设置图标
                    ImageView tempImage = view.findViewById(R.id.iv_furniture);
                    tempImage.setImageResource(R.drawable.icon_air_conditioner);
                    Log.i(TAG, furnitureItem.getStatus());
                    // 检查状态是否为null
                    String status = furnitureItem.getStatus();
                    if(status != null && status.length() > 0 && status.charAt(0) == '1') {
                        tempImage.setColorFilter(getResources().getColor(R.color.blue));
                    } else {
                        tempImage.setColorFilter(getResources().getColor(R.color.gray));
                    }

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
                    Log.i(TAG, furnitureItem.getStatus());
                    // 检查状态是否为null
                    status = furnitureItem.getStatus();
                    if(status != null && status.length() > 0 && status.charAt(0) == '1') {
                        tempImage.setColorFilter(getResources().getColor(R.color.WARN_text));
                    } else {
                        tempImage.setColorFilter(getResources().getColor(R.color.gray));
                    }
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

            if(furnitureItem.getSwitchStatus().charAt(0) == '1'){
                Switch mainSwitch = view.findViewById(R.id.switch_11);
                mainSwitch.setChecked(true);
            }
        }

        // 根据FurnitureItem中的switchStatus更新开关状态
        private void updateUISwitchStatueFromObject(View view) {

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
        }

        private void updateUISeekBarFromObject(View view) {
            switch (furnitureItem.getDeviceType()) {
                case "air_conditioner":
                    Log.d(TAG, "更新空调滑块，温度值: " + furnitureItem.getAcTemp());
                    // 根据acTemp调整滑块位置，acTemp范围为15-30
                    SeekBar tempSeekBar = view.findViewById(R.id.seekbar_1);
                    if (tempSeekBar != null) {
                        // 移除监听器以避免在更新UI时触发事件
                        // tempSeekBar.setOnSeekBarChangeListener(null);

                        // 将实际温度值(15-30)映射到滑块范围(0-100)
                        float acTemp = furnitureItem.getAcTemp();
                        // 确保温度值在有效范围内
                        if (acTemp >= 15 && acTemp <= 30) {
                            int progress = Math.round((acTemp - 15) / (30 - 15) * 100);
                            // 确保进度值在有效范围内
                            progress = Math.max(0, Math.min(100, progress));
                            Log.d(TAG, "空调滑块进度: " + progress);
                            tempSeekBar.setProgress(progress);
                        } else {
                            Log.w(TAG, "空调温度值超出范围: " + acTemp);
                        }
                    }
                    break;

                case "light":
                    Log.d(TAG, "更新灯光滑块，亮度值: " + furnitureItem.getLightPercent());
                    // 根据lightPercent调整滑块位置，lightPercent范围为10-100
                    SeekBar brightnessSeekBar = view.findViewById(R.id.seekbar_1);
                    if (brightnessSeekBar != null) {
                        // 移除监听器以避免在更新UI时触发事件
                        // brightnessSeekBar.setOnSeekBarChangeListener(null);

                        // 将实际亮度值(10-100)映射到滑块范围(0-100)
                        float lightPercent = furnitureItem.getLightPercent();
                        // 确保亮度值在有效范围内
                        if (lightPercent >= 10 && lightPercent <= 100) {
                            int progress = Math.round((lightPercent - 10) / (100 - 10) * 100);
                            // 确保进度值在有效范围内
                            progress = Math.max(0, Math.min(100, progress));
                            Log.d(TAG, "灯光滑块进度: " + progress);
                            brightnessSeekBar.setProgress(progress);
                        } else {
                            Log.w(TAG, "灯光亮度值超出范围: " + lightPercent);
                        }

                        // 重新设置监听器

                    }
                    break;
            }
        }
        private void refreshUI(View view){
            ImageView tempImage = view.findViewById(R.id.iv_furniture);
            FurnitureItemViewModel.UIState savedState = viewModel.getUIState(furnitureItem.getDeviceId());

            TextView arg1TextView = view.findViewById(R.id.arg1_item);
            Switch switch11 = view.findViewById(R.id.switch_11);

            // 根据FurnitureItem中的switchStatus更新开关状态
            updateUISwitchStatueFromObject(view);
            // 根据FurnitureItem中的参数更新滑块状态
            updateUISeekBarFromObject(view);
            // 根据主开关状态启用或禁用其他控件
            updateControlStates(view);

            if (switch11 != null && arg1TextView != null) {
                // 如果没有保存的状态，则根据开关状态设置默认文本
                if (savedState == null) {
                    if (switch11.isChecked()) {
                        arg1TextView.setText("正常运行");
                    } else {
                        arg1TextView.setText("停止工作");
                    }
                }
            }

            switch (furnitureItem.getDeviceType()) {
                case "air_conditioner":
                    if (switch11.isChecked()) {
                        tempImage.setColorFilter(getResources().getColor(R.color.blue));
                    } else {
                        tempImage.setColorFilter(getResources().getColor(R.color.gray));
                    }
                    // 设置参数项2显示空调当前温度
                    TextView arg2TextView = view.findViewById(R.id.arg2_item);
                    if (arg2TextView != null) {
                        // 如果没有保存的状态，则设置默认文本
                        if (savedState == null) {
                            arg2TextView.setText(furnitureItem.getAcTemp() + "℃");
                        }
                    }

                    // 根据acTemp调整滑块位置，acTemp范围为15-30
                    SeekBar tempSeekBar = view.findViewById(R.id.seekbar_1);
                    if (tempSeekBar != null) {
                        // 移除监听器以避免触发事件
                        // tempSeekBar.setOnSeekBarChangeListener(null);

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
                                sendRequestSeekbar(seekBar, seekBar.getProgress());
                                Log.d(TAG,"Place3");
                            }
                        });
                    }
                    break;

                case "light":
                    // 设置参数项2显示灯光亮度
                    if (switch11.isChecked()) {
                        tempImage.setColorFilter(getResources().getColor(R.color.WARN_text));
                    } else {
                        tempImage.setColorFilter(getResources().getColor(R.color.gray));
                    }
                    TextView lightArg2TextView = view.findViewById(R.id.arg2_item);
                    if (lightArg2TextView != null) {
                        // 如果没有保存的状态，则设置默认文本
                        if (savedState == null) {
                            lightArg2TextView.setText((int)furnitureItem.getLightPercent() + "%");
                        }
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
                                sendRequestSeekbar(seekBar, seekBar.getProgress());
                                Log.d(TAG,"Place4");
                            }
                        });
                    }
                    break;
            }
        }

        private void updateControlStates(View view) {
            Switch mainSwitch = view.findViewById(R.id.switch_11);
            boolean isEnabled = mainSwitch != null && mainSwitch.isChecked();

            // 禁用/启用其他开关 (除了主开关)
            for (int i = 1; i <= 2; i++) {
                for (int j = 1; j <= 4; j++) {
                    // 跳过主开关 (11)
                    if (i == 1 && j == 1) {
                        continue;
                    }

                    int switchId = getResources().getIdentifier("switch_" + i + j, "id", requireContext().getPackageName());
                    Switch switchView = view.findViewById(switchId);
                    if (switchView != null) {
                        switchView.setEnabled(isEnabled);
                        // 设置透明度以提供视觉反馈
                        switchView.setAlpha(isEnabled ? 1.0f : 0.5f);
                    }

                    // 同时禁用对应的标签
                    int labelId = getResources().getIdentifier("switch_label_" + i + j, "id", requireContext().getPackageName());
                    TextView labelView = view.findViewById(labelId);
                    if (labelView != null) {
                        labelView.setEnabled(isEnabled);
                        labelView.setAlpha(isEnabled ? 1.0f : 0.5f);
                    }
                }
            }

            // 禁用/启用滑块
            for (int i = 1; i <= 3; i++) {
                int seekBarId = getResources().getIdentifier("seekbar_" + i, "id", requireContext().getPackageName());
                SeekBar seekBar = view.findViewById(seekBarId);
                if (seekBar != null) {
                    seekBar.setEnabled(isEnabled);
                    seekBar.setAlpha(isEnabled ? 1.0f : 0.5f);
                }

                // 同时禁用对应的标签
                int labelId = getResources().getIdentifier("seekbar_label_" + i, "id", requireContext().getPackageName());
                TextView labelView = view.findViewById(labelId);
                if (labelView != null) {
                    labelView.setEnabled(isEnabled);
                    labelView.setAlpha(isEnabled ? 1.0f : 0.5f);
                }
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
        }

        private void showPopupMenu(View view) {
            PopupMenu popupMenu = new PopupMenu(requireContext(), view);
            popupMenu.getMenuInflater().inflate(R.menu.furniture_specific_settings, popupMenu.getMenu());

            popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    if (item.getItemId() == R.id.action_unbind) {
                        // 显示解绑确认对话框
                        showUnbindDialog(null, getView());
                        return true;
                    }
                    return false;
                }
            });

            popupMenu.show();
        }

        private void handleSwitchChange(CompoundButton switchView, boolean isChecked) {
            // 处理开关状态变化
            String switchName = getResources().getResourceEntryName(switchView.getId());
            // 这里可以添加实际的设备控制逻辑
            if (isAdded()) {
                ToastUtil.showToast(requireContext(), switchName + " 状态: " + (isChecked ? "开启" : "关闭"), Toast.LENGTH_SHORT);
            }
            sendRequestSwitch(switchView, isChecked);

            // 如果是主开关发生变化，需要更新其他控件的启用状态
            if (switchView.getId() == R.id.switch_11) {
                if (getView() != null) {
                    updateControlStates(getView());
                }
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

        private void handleSeekBarStopTracking(SeekBar seekBar) {
            // 当用户停止拖动滑块时发送命令
            int progress = seekBar.getProgress();
            sendRequestSeekbar(seekBar, progress);
        }

        public void AlarmUpdateUI() {
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


        //从UI更新开关状态到对象
        private void updateUISwitchStatusToObject(int position, boolean isChecked) {
            // 获取当前的switchStatus
            String currentSwitchStatus = furnitureItem.getSwitchStatus();

            // 确保switchStatus不为空且长度足够
            if (currentSwitchStatus != null && currentSwitchStatus.length() > position) {
                // 将字符串转换为字符数组以便修改
                char[] statusChars = currentSwitchStatus.toCharArray();

                // 更新指定位置的字符
                statusChars[position] = isChecked ? '1' : '0';

                // 将字符数组转换回字符串并更新furnitureItem
                furnitureItem.setSwitchStatus(new String(statusChars));
            } else if (currentSwitchStatus == null) {
                // 如果switchStatus为null，创建一个新的
                StringBuilder newSwitchStatus = new StringBuilder("00000000");
                newSwitchStatus.setCharAt(position, isChecked ? '1' : '0');
                furnitureItem.setSwitchStatus(newSwitchStatus.toString());
            } else {
                // 如果switchStatus长度不足，扩展它
                StringBuilder newSwitchStatus = new StringBuilder(currentSwitchStatus);
                while (newSwitchStatus.length() <= position) {
                    newSwitchStatus.append("0");
                }
                newSwitchStatus.setCharAt(position, isChecked ? '1' : '0');
                furnitureItem.setSwitchStatus(newSwitchStatus.toString());
            }
        }

        private void sendRequestSwitch(CompoundButton switchView, boolean isChecked) {
            // 获取开关的编号 (例如 switch_11 -> 11)
            String switchName = getResources().getResourceEntryName(switchView.getId());
            String switchNumber = switchName.substring(switchName.length() - 2);
            ImageView tempImage = this.getView().findViewById(R.id.iv_furniture);

            // 构建命令
            idl.SmartDemo03.Command command = new idl.SmartDemo03.Command();
            command.deviceId = furnitureItem.getDeviceId();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                command.timeStamp = LocalDateTime.now().toString();
            }

            // 根据设备类型和开关编号构建action
            switch (furnitureItem.getDeviceType()) {
                case "air_conditioner":
                    if ("11".equals(switchNumber)) {
                        // 主开关
                        command.action = "switch_" + command.deviceId + "_" +(isChecked ? "on" : "off");
                        command.deviceType = "ac";
                        // 更新furnitureItem中的switchStatus
                        updateUISwitchStatusToObject(0, isChecked);
                        if(isChecked) {
                            tempImage.setColorFilter(getResources().getColor(R.color.blue));
                        } else {
                            tempImage.setColorFilter(getResources().getColor(R.color.gray));
                        }

                    } else if ("12".equals(switchNumber)) {
                        // 制冷开关
                        command.action = "cool_" + command.deviceId + "_" +(isChecked ? "on" : "off");
                        command.deviceType = "ac";
                        updateUISwitchStatusToObject(1, isChecked);

                    } else if ("13".equals(switchNumber)) {
                        // 扫风开关
                        command.action = "fan_"+ command.deviceId + "_" +(isChecked ? "on" : "off");
                        command.deviceType = "ac";
                        updateUISwitchStatusToObject(2, isChecked);

                    } else if ("14".equals(switchNumber)) {
                        // 除湿开关
                        command.action = "dehumidify_"+ command.deviceId + "_" +(isChecked ? "on" : "off");
                        command.deviceType = "ac";
                        updateUISwitchStatusToObject(3, isChecked);
                    }
                    break;
                case "light":
                    if ("11".equals(switchNumber)) {
                        // 主开关
                        command.action = "switch_" + command.deviceId + "_" +(isChecked ? "on" : "off");
                        command.deviceType = "light";
                        updateUISwitchStatusToObject(0, isChecked);
                        if(isChecked) {
                            tempImage.setColorFilter(getResources().getColor(R.color.WARN_text));
                        } else {
                            tempImage.setColorFilter(getResources().getColor(R.color.gray));
                        }
                    }
                    break;
            }

            commandDdsManager.sendCommand(command);
            Log.i(TAG, "开关命令发送成功:" + command.action);

            // 只更新参数显示文本，不更新滑块进度
            if(this.getView() != null) {
                switch (furnitureItem.getDeviceType()) {
                    case "air_conditioner":
                        TextView arg2TextView = this.getView().findViewById(R.id.arg2_item);
                        if (arg2TextView != null) {
                            arg2TextView.setText(furnitureItem.getAcTemp() + "℃");
                        }
                        break;
                    case "light":
                        TextView lightArg2TextView = this.getView().findViewById(R.id.arg2_item);
                        if (lightArg2TextView != null) {
                            lightArg2TextView.setText((int)furnitureItem.getLightPercent() + "%");
                        }
                        break;
                }

                // 更新开关相关文本
                TextView arg1TextView = this.getView().findViewById(R.id.arg1_item);
                Switch switch11 = this.getView().findViewById(R.id.switch_11);
                if (switch11 != null && arg1TextView != null) {
                    if (switch11.isChecked()) {
                        arg1TextView.setText("正常运行");
                    } else {
                        arg1TextView.setText("停止工作");
                    }
                }
            }
        }

        private void sendRequestSeekbar(SeekBar seekBar, int progress) {
            // 构建命令
            idl.SmartDemo03.Command command = new idl.SmartDemo03.Command();
            command.deviceId = furnitureItem.getDeviceId();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                command.timeStamp = LocalDateTime.now().toString();
            }

            // 根据设备类型构建action
            switch (furnitureItem.getDeviceType()) {
                case "air_conditioner":
                    command.deviceType = "ac";
                    // 将进度(0-100)映射到温度范围(15-30)
                    float temp = 15 + (progress / 100.0f) * (30 - 15);
                    command.action = "temp_" + command.deviceId + "_"  + Math.round(temp);
                    command.value = temp;
                    // 更新furnitureItem中的温度值
                    furnitureItem.setAcTemp(temp);
                    break;
                case "light":
                    command.deviceType = "light";
                    // 将进度(0-100)映射到亮度范围(10-100)
                    int brightness = (int) (10 + (progress / 100.0f) * (100 - 10));
                    command.action = "brightness_" + command.deviceId + "_"  + brightness;
                    command.value = brightness;

                    // 更新furnitureItem中的亮度值
                    furnitureItem.setLightPercent(brightness);
                    break;
            }

            // 只更新参数显示文本，不更新滑块进度（因为滑块本身就是用户操作的控件）
            if(this.getView() != null) {
                switch (furnitureItem.getDeviceType()) {
                    case "air_conditioner":
                        TextView arg2TextView = this.getView().findViewById(R.id.arg2_item);
                        if (arg2TextView != null) {
                            arg2TextView.setText(furnitureItem.getAcTemp() + "℃");
                        }
                        break;
                    case "light":
                        TextView lightArg2TextView = this.getView().findViewById(R.id.arg2_item);
                        if (lightArg2TextView != null) {
                            lightArg2TextView.setText((int)furnitureItem.getLightPercent() + "%");
                        }
                        break;
                }
            }
            commandDdsManager.sendCommand(command);
        }

        private void showUnbindDialog(Presence presence, View view) {
            // 加载对话框布局
            LayoutInflater inflater = LayoutInflater.from(requireContext());
            View dialogView = inflater.inflate(R.layout.window_unbind_confirm, null);

            // 更新对话框中的文本
            TextView deviceIdText = dialogView.findViewById(R.id.unbind_device_id);
            TextView deviceTypeText = dialogView.findViewById(R.id.unbind_device_type);

            if (deviceIdText != null) {
                deviceIdText.setText(getString(R.string.device_id) + ": " + furnitureItem.getDeviceId());
            }

            if (deviceTypeText != null) {
                deviceTypeText.setText(getString(R.string.device_type) + ": " + furnitureItem.getDeviceType());
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
