package com.SmartHome.SmartHomeDemo.fragments.SettingFragment;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.SmartHome.SmartHomeDemo.R;
import com.SmartHome.SmartHomeDemo.application.SmartHomeApplication;
import com.SmartHome.SmartHomeDemo.database.AppDatabase;
import com.SmartHome.SmartHomeDemo.database.Device;
import com.SmartHome.SmartHomeDemo.dds.CommandDdsManager;
import com.SmartHome.SmartHomeDemo.fragments.CarFragment.CarAlert;
import com.SmartHome.SmartHomeDemo.fragments.HomeFragment.FurnitureAlert;
import com.SmartHome.SmartHomeDemo.fragments.HomeFragment.FurnitureDataPack;
import com.SmartHome.SmartHomeDemo.fragments.HomeFragment.FurnitureItem;
import com.SmartHome.SmartHomeDemo.utils.ToastUtil;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import idl.SmartDemo03.Command;

public class SettingFragment extends Fragment {
    private String TAG = "SettingFragment";
    private TextView darkText;
    private TextView notificationText;
    private Switch darkModeSwitch;
    private Switch NoticeSwitch;
    private Button testSceneMode;

    // 添加三个情景模式TextView的引用
    private TextView sceneMode1Name;
    private TextView sceneMode2Name;
    private TextView sceneMode3Name;
    // 添加三个情景模式开关的引用
    private Switch sceneMode1Switch;
    private Switch sceneMode2Switch;
    private Switch sceneMode3Switch;
    private Button sceneMode1Customize;
    private Button sceneMode2Customize;
    private Button sceneMode3Customize;
    private Button testChart;

    // SharedPreferences的键名
    private static final String PREFS_NAME = "SceneModeNames";
    private static final String SCENE_MODE_1_KEY = "scene_mode_1_name";
    private static final String SCENE_MODE_2_KEY = "scene_mode_2_name";
    private static final String SCENE_MODE_3_KEY = "scene_mode_3_name";
    // 情景模式设备选择的键名
    private static final String SCENE_MODE_1_DEVICES_KEY = "scene_mode_1_devices";
    private static final String SCENE_MODE_2_DEVICES_KEY = "scene_mode_2_devices";
    private static final String SCENE_MODE_3_DEVICES_KEY = "scene_mode_3_devices";
    // 新增：情景模式设备类型选择的键名
    private static final String SCENE_MODE_1_DEVICE_TYPES_KEY = "scene_mode_1_devices_types";
    private static final String SCENE_MODE_2_DEVICE_TYPES_KEY = "scene_mode_2_devices_types";
    private static final String SCENE_MODE_3_DEVICE_TYPES_KEY = "scene_mode_3_devices_types";


    private AppDatabase database;
    private CommandDdsManager commandDdsManager;


    public SettingFragment(){}

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 获取数据库实例
        if (getActivity() != null) {
            SmartHomeApplication app = (SmartHomeApplication) getActivity().getApplication();
            database = app.getDatabase();
            commandDdsManager = app.getCommandDdsManager();
        }
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_setting, container, false);
        initialization(view);
        return view;
    }

    private void initialization(View view){
        darkModeSwitch     = view.findViewById(R.id.dark_mode_switch);
        NoticeSwitch       = view.findViewById(R.id.notification_allow_switch);
        testSceneMode      = view.findViewById(R.id.test_del_scene);
        darkText           = view.findViewById(R.id.dark_mode_text);
        notificationText   = view.findViewById(R.id.notification_allow_text);
        testChart          = view.findViewById(R.id.test_chart);

        if(darkModeSwitch != null) {
            // 设置开关的初始状态
            int currentNightMode = AppCompatDelegate.getDefaultNightMode();
            darkModeSwitch.setChecked(currentNightMode == AppCompatDelegate.MODE_NIGHT_YES);

            // 设置开关监听器
            darkModeSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    // 应用夜间模式
                    if (isChecked) {
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                    } else {
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                    }

//                    // 通知Activity更新UI而不重建
//                    if (getActivity() != null) {
//                        // 通过Configuration更新UI，避免recreate()
//                        Configuration newConfig = new Configuration(getActivity().getResources().getConfiguration());
//                        newConfig.uiMode &= ~Configuration.UI_MODE_NIGHT_MASK;
//                        newConfig.uiMode |= isChecked ? Configuration.UI_MODE_NIGHT_YES : Configuration.UI_MODE_NIGHT_NO;
//                        getActivity().getResources().updateConfiguration(newConfig, getActivity().getResources().getDisplayMetrics());
//                    }
                }
            });
        }

        if (NoticeSwitch != null) {
            // 获取SharedPreferences中保存的通知开关状态，默认为开启状态
            SharedPreferences prefs = getActivity().getSharedPreferences("settings", Context.MODE_PRIVATE);
            boolean isNoticeEnabled = prefs.getBoolean("notice_enabled", true);
            NoticeSwitch.setChecked(isNoticeEnabled);

            // 设置开关监听器
            NoticeSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    // 保存开关状态到SharedPreferences
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putBoolean("notice_enabled", isChecked);
                    editor.apply();
                }
            });
        }


        if(testSceneMode != null) {
            testSceneMode.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    clearSceneModeDevices(SCENE_MODE_1_DEVICES_KEY);
                    clearSceneModeDevices(SCENE_MODE_2_DEVICES_KEY);
                    clearSceneModeDevices(SCENE_MODE_3_DEVICES_KEY);
                }
            });
        }

        if(testChart != null) {
            testChart.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showBlacklistManagementDialog();
                }
            });
        }
    }
    private void showChart(ArrayList<Entry> entries) {
        // 加载window_energy_trend布局
        View trendView = getLayoutInflater().inflate(R.layout.window_energy_trend, null);

        // 获取图表实例
        LineChart energyChart = trendView.findViewById(R.id.trend_chart);

        // 创建示例数据集
        LineDataSet dataSet = new LineDataSet(entries, "设备能耗");
        dataSet.setColor(ColorTemplate.MATERIAL_COLORS[0]);
        dataSet.setValueTextColor(ColorTemplate.MATERIAL_COLORS[1]);

        // 设置线条加粗
        dataSet.setLineWidth(3f); // 设置线条宽度为3像素

        // 设置数据到图表
        energyChart.setData(new LineData(dataSet));
        // 设置X轴标签
        energyChart.getXAxis().setPosition(com.github.mikephil.charting.components.XAxis.XAxisPosition.BOTTOM);
        energyChart.getXAxis().setGranularity(1f);
        energyChart.getXAxis().setValueFormatter(new com.github.mikephil.charting.formatter.ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                // 将时间戳转换为日期格式
                long millis = (long) value;
                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("MM-dd");
                return sdf.format(new java.util.Date(millis));
            }
        });

        // 设置Y轴标签
        energyChart.getAxisLeft().setValueFormatter(new com.github.mikephil.charting.formatter.ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return value + "kWh";
            }
        });
        energyChart.invalidate(); // 刷新图表

        // 显示图表弹窗
        new AlertDialog.Builder(requireContext())
                .setView(trendView)
                .setTitle("设备能耗趋势")
                .setPositiveButton("关闭", null)
                .show();
    }



    // 在 Fragment 或 Activity 中添加以下代码
    private void setupDoubleClickToEdit(TextView textView, final String prefKey) {
        textView.setOnClickListener(new View.OnClickListener() {
            private long lastClickTime = 0;
            private static final long DOUBLE_CLICK_TIME_DELTA = 300; // 双击时间间隔阈值（毫秒）

            @Override
            public void onClick(View v) {
                long clickTime = System.currentTimeMillis();
                if (clickTime - lastClickTime < DOUBLE_CLICK_TIME_DELTA) {
                    // 双击事件处理
                    enableEditMode(textView, prefKey);
                    lastClickTime = 0; // 重置时间，避免连续点击触发多次
                } else {
                    lastClickTime = clickTime;
                }
            }
        });
    }

    private void enableEditMode(TextView textView, final String prefKey) {
        // 创建一个 EditText 对话框或替换 TextView 为 EditText
        final EditText editText = new EditText(this.getContext());
        editText.setText(textView.getText());

        AlertDialog.Builder builder = new AlertDialog.Builder(this.getContext());
        builder.setTitle("修改情景模式名称");
        builder.setView(editText);

        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String newName = editText.getText().toString();
                if (!newName.isEmpty()) {
                    textView.setText(newName);
                    // 保存到SharedPreferences
                    saveSceneModeName(prefKey, newName);
                }
            }
        });

        builder.setNegativeButton("取消", null);
        builder.show();
    }

    // 保存情景模式名称到SharedPreferences
    private void saveSceneModeName(String key, String name) {
        SharedPreferences prefs = getActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(key, name);
        editor.apply();
    }

    // 从SharedPreferences恢复情景模式名称
    private void restoreSceneModeNames() {
        SharedPreferences prefs = getActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        sceneMode1Name.setText(prefs.getString(SCENE_MODE_1_KEY, "情景模式1"));
        sceneMode2Name.setText(prefs.getString(SCENE_MODE_2_KEY, "情景模式2"));
        sceneMode3Name.setText(prefs.getString(SCENE_MODE_3_KEY, "情景模式3"));
    }

    private void showSceneModeCustomizeDialog(String sceneModeName, String sceneModeKey) {
        // 创建自定义对话框
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.window_scenemode_customize, null);
        builder.setView(dialogView);

        // 设置情景模式名称
        TextView titleText = dialogView.findViewById(R.id.scene_mode_name);
        titleText.setText(sceneModeName);

        // 初始化设备列表
        RecyclerView deviceRecyclerView = dialogView.findViewById(R.id.customize_device_recyclerView);
        deviceRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        // 设置按钮点击事件
        Button cancelButton = dialogView.findViewById(R.id.customize_cancel);
        Button doneButton = dialogView.findViewById(R.id.customize_done);

        AlertDialog dialog = builder.create();

        cancelButton.setOnClickListener(v -> dialog.dismiss());

        // 先显示对话框
        dialog.show();

        // 显示加载进度指示
        TextView loadingText = new TextView(requireContext());
        loadingText.setText("正在加载设备列表...");
        loadingText.setPadding(16, 16, 16, 16);
        deviceRecyclerView.setAdapter(new RecyclerView.Adapter() {
            @NonNull
            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                return new RecyclerView.ViewHolder(loadingText) {};
            }

            @Override
            public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
                // 不需要实现
            }

            @Override
            public int getItemCount() {
                return 1;
            }
        });

        // 在后台线程中获取设备列表
        if (database != null) {
            AppDatabase.databaseWriteExecutor.execute(() -> {
                // 在后台线程中查询数据库
                List<Device> deviceList = getDevicesFromDatabase();
                Log.i(TAG, deviceList.toString());

                // 切换到主线程更新UI
                requireActivity().runOnUiThread(() -> {
                    SceneModeFurnitureAdapter adapter = new SceneModeFurnitureAdapter(requireContext(), deviceList, sceneModeKey);
                    deviceRecyclerView.setAdapter(adapter);

                    doneButton.setOnClickListener(v -> {
                        // 保存选中的设备
                        adapter.saveSelectedDevices();
                        dialog.dismiss();
                    });
                });
            });
        } else {
            Log.e(TAG, "数据库不可用!");
            // 如果数据库不可用，使用示例数据
            List<Device> deviceList = createSampleDeviceList();
            SceneModeFurnitureAdapter adapter = new SceneModeFurnitureAdapter(requireContext(), deviceList, sceneModeKey);
            deviceRecyclerView.setAdapter(adapter);
            doneButton.setOnClickListener(v -> {
                // 保存选中的设备
                adapter.saveSelectedDevices();
                dialog.dismiss();
            });
        }
    }
    // 情景模式1开关状态变化处理接口
    private void onSceneMode1SwitchChanged(boolean isChecked) {
        Log.i(TAG, "SceneMode1SwitchChanged " + isChecked);
        List<String> selectedDeviceIds = getSelectedDevicesForSceneMode(SCENE_MODE_1_DEVICES_KEY);
        List<String> selectedDeviceTypes = getSelectedDeviceTypesForSceneMode(SCENE_MODE_1_DEVICE_TYPES_KEY);
        Log.i(TAG, "情景模式1选中的设备ID: " + selectedDeviceIds);
        Log.i(TAG, "情景模式1选中的设备类型: " + selectedDeviceTypes);
        int len = selectedDeviceIds.size();
        // 例如：发送指令到设备、更新UI等
        // 使用Handler实现延迟发送命令
        Handler handler = new Handler(Looper.getMainLooper());
        for(int i = 0; i < selectedDeviceIds.size(); i++) {
            final int index = i;
            handler.postDelayed(() -> {
                Command command = new Command();
                command.deviceId = selectedDeviceIds.get(index);
                command.deviceType = selectedDeviceTypes.get(index);
                if(selectedDeviceTypes.get(index).equals("air_conditioner"))
                    command.deviceType = "ac";
                command.action = "switch_" + command.deviceId + (isChecked ? "_on" : "_off");
                commandDdsManager.sendCommand(command);
            }, i * 100); // 每个命令间隔100毫秒
        }
    }

    // 情景模式2开关状态变化处理接口
    private void onSceneMode2SwitchChanged(boolean isChecked) {
        Log.i(TAG, "SceneMode2SwitchChanged " + isChecked);
        List<String> selectedDeviceIds = getSelectedDevicesForSceneMode(SCENE_MODE_2_DEVICES_KEY);
        List<String> selectedDeviceTypes = getSelectedDeviceTypesForSceneMode(SCENE_MODE_2_DEVICE_TYPES_KEY);
        Log.i(TAG, "情景模式2选中的设备ID: " + selectedDeviceIds);
        Log.i(TAG, "情景模式2选中的设备类型: " + selectedDeviceTypes);
        int len = selectedDeviceIds.size();
        // 例如：发送指令到设备、更新UI等
        // 使用Handler实现延迟发送命令
        Handler handler = new Handler(Looper.getMainLooper());
        for(int i = 0; i < selectedDeviceIds.size(); i++) {
            final int index = i;
            handler.postDelayed(() -> {
                Command command = new Command();
                command.deviceId = selectedDeviceIds.get(index);
                command.deviceType = selectedDeviceTypes.get(index);
                command.action = "switch_" + command.deviceId + (isChecked ? "_on" : "_off");
                commandDdsManager.sendCommand(command);
            }, i * 100); // 每个命令间隔100毫秒
        }
    }

    // 情景模式3开关状态变化处理接口
    private void onSceneMode3SwitchChanged(boolean isChecked) {
        Log.i(TAG, "SceneMode3SwitchChanged " + isChecked);
        List<String> selectedDeviceIds = getSelectedDevicesForSceneMode(SCENE_MODE_3_DEVICES_KEY);
        List<String> selectedDeviceTypes = getSelectedDeviceTypesForSceneMode(SCENE_MODE_3_DEVICE_TYPES_KEY);
        Log.i(TAG, "情景模式3选中的设备ID: " + selectedDeviceIds);
        Log.i(TAG, "情景模式3选中的设备类型: " + selectedDeviceTypes);
        int len = selectedDeviceIds.size();
        // 例如：发送指令到设备、更新UI等
        // 使用Handler实现延迟发送命令
        Handler handler = new Handler(Looper.getMainLooper());
        for(int i = 0; i < selectedDeviceIds.size(); i++) {
            final int index = i;
            handler.postDelayed(() -> {
                Command command = new Command();
                command.deviceId = selectedDeviceIds.get(index);
                command.deviceType = selectedDeviceTypes.get(index);
                command.action = "switch_" + command.deviceId + (isChecked ? "_on" : "_off");
                commandDdsManager.sendCommand(command);
            }, i * 100); // 每个命令间隔100毫秒
        }
    }

    // 根据情景模式键名获取选中的设备ID集合
    private List<String> getSelectedDevicesForSceneMode(String sceneModeKey) {
        SharedPreferences prefs = requireContext().getSharedPreferences("SceneModes", Context.MODE_PRIVATE);
        List<String> idList = new ArrayList<>();

        // 先尝试以字符串形式读取（新格式）
        String idsString = prefs.getString(sceneModeKey, "");
        if (!idsString.isEmpty()) {
            String[] idsArray = idsString.split(",");
            idList = new ArrayList<>(Arrays.asList(idsArray));
        } else {
            // 如果没有字符串格式的数据，尝试以Set形式读取（旧格式）
            Set<String> idSet = prefs.getStringSet(sceneModeKey, new HashSet<>());
            idList = new ArrayList<>(idSet);
        }

        // 移除空字符串
        idList.removeIf(String::isEmpty);
        return idList;
    }
    // 根据情景模式键名获取选中的设备类型列表
    private List<String> getSelectedDeviceTypesForSceneMode(String sceneModeTypesKey) {
        SharedPreferences prefs = requireContext().getSharedPreferences("SceneModes", Context.MODE_PRIVATE);
        String typesString = prefs.getString(sceneModeTypesKey, "");
        List<String> typesList = new ArrayList<>();
        if (!typesString.isEmpty()) {
            String[] typesArray = typesString.split(",");
            typesList = new ArrayList<>(Arrays.asList(typesArray));
        }
        typesList.removeIf(String::isEmpty);
        return typesList;
    }

    private void onSceneMode1Customize() {
        showSceneModeCustomizeDialog("情景模式1", SCENE_MODE_1_DEVICES_KEY);
    }
    private void onSceneMode2Customize() {
        showSceneModeCustomizeDialog("情景模式2", SCENE_MODE_2_DEVICES_KEY);
    }
    private void onSceneMode3Customize() {
        showSceneModeCustomizeDialog("情景模式3", SCENE_MODE_3_DEVICES_KEY);
    }
    // 在 onViewCreated 或 onCreate 方法中调用
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 为三个情景模式名称 TextView 设置双击编辑功能
        sceneMode1Name = view.findViewById(R.id.scene_mode_1_name);
        sceneMode2Name = view.findViewById(R.id.scene_mode_2_name);
        sceneMode3Name = view.findViewById(R.id.scene_mode_3_name);

        // 初始化三个情景模式开关
        sceneMode1Switch = view.findViewById(R.id.scene_mode_1_switch);
        sceneMode2Switch = view.findViewById(R.id.scene_mode_2_switch);
        sceneMode3Switch = view.findViewById(R.id.scene_mode_3_switch);

        sceneMode1Customize = view.findViewById(R.id.scene_mode_1_customize);
        sceneMode2Customize = view.findViewById(R.id.scene_mode_2_customize);
        sceneMode3Customize = view.findViewById(R.id.scene_mode_3_customize);

        setupDoubleClickToEdit(sceneMode1Name, SCENE_MODE_1_KEY);
        setupDoubleClickToEdit(sceneMode2Name, SCENE_MODE_2_KEY);
        setupDoubleClickToEdit(sceneMode3Name, SCENE_MODE_3_KEY);

        // 为三个情景模式开关设置监听器
        sceneMode1Switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // 保存开关状态
                //saveSceneModeSwitchState(SCENE_MODE_1_SWITCH_KEY, isChecked);
                // 调用预留的处理接口
                onSceneMode1SwitchChanged(isChecked);
            }
        });

        sceneMode2Switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // 保存开关状态
                //saveSceneModeSwitchState(SCENE_MODE_2_SWITCH_KEY, isChecked);
                // 调用预留的处理接口
                onSceneMode2SwitchChanged(isChecked);
            }
        });

        sceneMode3Switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // 保存开关状态
                //saveSceneModeSwitchState(SCENE_MODE_3_SWITCH_KEY, isChecked);
                // 调用预留的处理接口
                onSceneMode3SwitchChanged(isChecked);
            }
        });

        sceneMode1Customize.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onSceneMode1Customize();
            }
        });
        sceneMode2Customize.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onSceneMode2Customize();
            }
        });
        sceneMode3Customize.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onSceneMode3Customize();
            }
        });

        // 恢复保存的情景模式名称
        restoreSceneModeNames();

        // 其他初始化代码...
    }

    // 从数据库获取设备列表
    private List<Device> getDevicesFromDatabase() {
        List<Device> deviceList = new ArrayList<>();

        if (database != null) {
            // 在后台线程中查询数据库
            List<Device> devices = database.deviceDao().getAllDevices();

            // 筛选出家具类型的设备（light和air_conditioner）
            for (Device device : devices) {
                if ("light".equals(device.getDeviceType())
                        || "air_conditioner".equals(device.getDeviceType())
                        || "ac".equals(device.getDeviceType())) {
                    deviceList.add(device);
                }
            }
        }

        // 如果数据库不可用或没有设备，返回示例数据
        if (deviceList.isEmpty()) {
            //deviceList = createSampleDeviceList();
        }

        return deviceList;
    }
    // 创建示例设备列表用于演示
    private List<Device> createSampleDeviceList() {
        List<Device> deviceList = new ArrayList<>();

        // 在实际应用中，这些数据应该从主活动中获取
        // 这里只是创建一些示例数据用于演示
        for (int i = 1; i <= 5; i++) {
            Device device = new Device();
            device.setDeviceId("设备" + i);
            device.setDeviceType(i % 2 == 0 ? "light" : "air_conditioner");
            deviceList.add(device);
        }

        return deviceList;
    }

    private void clearSceneModeDevices(String sceneModeKey) {
        SharedPreferences prefs = requireContext().getSharedPreferences("SceneModes", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(sceneModeKey, ""); // 清空设备ID列表
        editor.putString(sceneModeKey + "_types", ""); // 清空设备类型列表
        // 同时清除旧格式的数据
        editor.remove(sceneModeKey);
        editor.apply();
    }

    // 添加处理夜间模式切换的方法
    public void onConfigurationChanged(android.content.res.Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // 在这里可以添加特定于SettingFragment的夜间模式处理逻辑
        updateTextColors();
    }

    // 更新文本颜色的方法
    private void updateTextColors() {
        if (getActivity() != null) {
            boolean isNightMode = (getActivity().getResources().getConfiguration().uiMode
                    & android.content.res.Configuration.UI_MODE_NIGHT_MASK)
                    == android.content.res.Configuration.UI_MODE_NIGHT_YES;

            int textColor = isNightMode ?
                    R.color.text_color_dark : R.color.text_color_light;

            // 更新所有TextView的颜色
            if (sceneMode1Name != null) {
                sceneMode1Name.setTextColor(getActivity().getResources().getColor(textColor));
            }
            if (sceneMode2Name != null) {
                sceneMode2Name.setTextColor(getActivity().getResources().getColor(textColor));
            }
            if (sceneMode3Name != null) {
                sceneMode3Name.setTextColor(getActivity().getResources().getColor(textColor));
            }
            if (darkText != null) {
                darkText.setTextColor(getActivity().getResources().getColor(textColor));
            }
            if (notificationText != null) {
                notificationText.setTextColor(getActivity().getResources().getColor(textColor));
            }
        }
    }

    /**
     * 刷新数据的方法
     * 重新加载设置选项的状态
     */
    public void refreshData() {
        if (getActivity() != null) {
            // 刷新夜间模式开关状态
            if (darkModeSwitch != null) {
                int currentNightMode = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
                darkModeSwitch.setChecked(currentNightMode == Configuration.UI_MODE_NIGHT_YES);
            }

            // 刷新通知开关状态
            if (NoticeSwitch != null) {
                SharedPreferences prefs = getActivity().getSharedPreferences("settings", Context.MODE_PRIVATE);
                boolean isNoticeEnabled = prefs.getBoolean("notice_enabled", true);
                NoticeSwitch.setChecked(isNoticeEnabled);
            }
        }
    }

    private void showBlacklistManagementDialog() {
        // 创建自定义对话框
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.window_blacklist_manage, null);
        builder.setView(dialogView);

        // 设置标题
        TextView titleText = dialogView.findViewById(R.id.blacklist_title);
        titleText.setText("黑名单管理");

        // 初始化设备列表
        RecyclerView deviceRecyclerView = dialogView.findViewById(R.id.blacklist_device_recyclerView);
        deviceRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        // 设置按钮点击事件
        Button cancelButton = dialogView.findViewById(R.id.blacklist_cancel);
        Button doneButton = dialogView.findViewById(R.id.blacklist_done);

        AlertDialog dialog = builder.create();

        cancelButton.setOnClickListener(v -> dialog.dismiss());

        // 先显示对话框
        dialog.show();

        // 显示加载进度指示
        TextView loadingText = new TextView(requireContext());
        loadingText.setText("正在加载黑名单设备列表...");
        loadingText.setPadding(16, 16, 16, 16);
        deviceRecyclerView.setAdapter(new RecyclerView.Adapter() {
            @NonNull
            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                return new RecyclerView.ViewHolder(loadingText) {};
            }

            @Override
            public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
                // 不需要实现
            }

            @Override
            public int getItemCount() {
                return 1;
            }
        });

        // 在后台线程中获取黑名单设备列表
        if (database != null) {
            AppDatabase.databaseWriteExecutor.execute(() -> {
                // 在后台线程中查询数据库
                List<com.SmartHome.SmartHomeDemo.database.BlacklistedDevice> blacklistedDevices =
                        database.blacklistedDeviceDao().getAllBlacklistedDevices();
                Log.i(TAG, "黑名单设备数量: " + blacklistedDevices.size());

                // 切换到主线程更新UI
                requireActivity().runOnUiThread(() -> {
                    BlacklistDeviceAdapter adapter = new BlacklistDeviceAdapter(requireContext(), blacklistedDevices);
                    deviceRecyclerView.setAdapter(adapter);

                    doneButton.setOnClickListener(v -> {
                        // 从黑名单中移除选中的设备
                        List<com.SmartHome.SmartHomeDemo.database.BlacklistedDevice> selectedDevices =
                                adapter.getSelectedDevicesForRemoval();

                        if (!selectedDevices.isEmpty()) {
                            // 在后台线程中从数据库删除选中的设备
                            AppDatabase.databaseWriteExecutor.execute(() -> {
                                for (com.SmartHome.SmartHomeDemo.database.BlacklistedDevice device : selectedDevices) {
                                    database.blacklistedDeviceDao().delete(device);
                                    Log.i(TAG, "已从黑名单中移除设备: " + device.getDeviceId());
                                }

                                // 更新UI
                                requireActivity().runOnUiThread(() -> {
                                    adapter.removeSelectedDevices();
                                    ToastUtil.showToast(requireContext(), "已从黑名单中移除 " + selectedDevices.size() + " 个设备",
                                            Toast.LENGTH_SHORT);
                                });
                            });
                        }

                        dialog.dismiss();
                    });
                });
            });
        } else {
            Log.e(TAG, "数据库不可用!");
            ToastUtil.showToast(requireContext(), "数据库不可用", Toast.LENGTH_SHORT);
            dialog.dismiss();
        }
    }

}
