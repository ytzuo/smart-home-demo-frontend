package com.SmartHome.SmartHomeDemo.fragments.SettingFragment;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;

import com.SmartHome.SmartHomeDemo.R;
import com.SmartHome.SmartHomeDemo.fragments.CarFragment.CarAlert;
import com.SmartHome.SmartHomeDemo.fragments.HomeFragment.FurnitureAlert;

public class SettingFragment extends Fragment {
    private String TAG = "SettingFragment";
    private Switch darkModeSwitch;
    private Switch NoticeSwitch;
    private Button testAlertFurniture;

    private Button testAlertCar;

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

    // SharedPreferences的键名
    private static final String PREFS_NAME = "SceneModeNames";
    private static final String SCENE_MODE_1_KEY = "scene_mode_1_name";
    private static final String SCENE_MODE_2_KEY = "scene_mode_2_name";
    private static final String SCENE_MODE_3_KEY = "scene_mode_3_name";
//    // 情景模式开关状态的键名
//    private static final String SCENE_MODE_1_SWITCH_KEY = "scene_mode_1_switch";
//    private static final String SCENE_MODE_2_SWITCH_KEY = "scene_mode_2_switch";
//    private static final String SCENE_MODE_3_SWITCH_KEY = "scene_mode_3_switch";


    public SettingFragment(){}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_setting, container, false);
        initialization(view);
        return view;
    }

    private void initialization(View view){
        darkModeSwitch = view.findViewById(R.id.dark_mode_switch);
        NoticeSwitch = view.findViewById(R.id.notification_allow_switch);
        testAlertFurniture = view.findViewById(R.id.test_alert_furniture);
        testAlertCar = view.findViewById(R.id.test_alert_car);

        if(darkModeSwitch != null) {
            // 设置开关的初始状态
            int currentNightMode = AppCompatDelegate.getDefaultNightMode();
            darkModeSwitch.setChecked(currentNightMode == AppCompatDelegate.MODE_NIGHT_YES);

            // 设置开关监听器
            darkModeSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                    } else {
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                    }
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

        if(testAlertFurniture != null){
            testAlertFurniture.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // 创建一个测试的报警对话框
                    testAlertFurniture.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            // 创建一个测试的报警对话框
                            FurnitureAlert furnitureAlert = FurnitureAlert.newInstance("TEST001", "AC", "");
                            furnitureAlert.setOnButtonClickListener(new FurnitureAlert.OnButtonClickListener() {
                                @Override
                                public void onConfirmClick() {
                                    // 处理确认按钮点击事件
                                    furnitureAlert.dismiss();
                                }

                                @Override
                                public void onViewAlertClick() {
                                    // 处理查看设备按钮点击事件
                                    furnitureAlert.dismiss();
                                }
                            });

                            furnitureAlert.show(getChildFragmentManager(), "furniture_alert_test");
                        }
                    });
                }
            });
        }

        if(testAlertCar != null){
            testAlertCar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // 创建一个测试的报警对话框
                    testAlertCar.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            // 创建一个测试的报警对话框
                            CarAlert carAlert = CarAlert.newInstance("TEST002", "100100", "");
                            carAlert.setOnButtonClickListener(new CarAlert.OnButtonClickListener() {
                                @Override
                                public void onConfirmClick() {
                                    // 处理确认按钮点击事件
                                    carAlert.dismiss();
                                }
                            });

                            carAlert.show(getChildFragmentManager(), "car_alert_test");
                        }
                    });
                }
            });
        }
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

    // 情景模式1开关状态变化处理接口
    private void onSceneMode1SwitchChanged(boolean isChecked) {
        // TODO: 在这里处理情景模式1开关状态变化
        Log.i(TAG, "SceneMode1SwitchChanged " + isChecked);
        // 例如：发送指令到设备、更新UI等
    }

    // 情景模式2开关状态变化处理接口
    private void onSceneMode2SwitchChanged(boolean isChecked) {
        // TODO: 在这里处理情景模式2开关状态变化
        Log.i(TAG, "SceneMode2SwitchChanged " + isChecked);
        // 例如：发送指令到设备、更新UI等
    }

    // 情景模式3开关状态变化处理接口
    private void onSceneMode3SwitchChanged(boolean isChecked) {
        // TODO: 在这里处理情景模式3开关状态变化
        Log.i(TAG, "SceneMode3SwitchChanged " + isChecked);
        // 例如：发送指令到设备、更新UI等
    }

    private void onSceneMode1Customize() {
        // TODO: 在这里写点击情景模式1自定义按钮的逻辑
    }
    private void onSceneMode2Customize() {
        // TODO: 在这里写点击情景模式2自定义按钮的逻辑
    }
    private void onSceneMode3Customize() {
        // TODO: 在这里写点击情景模式3自定义按钮的逻辑
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
}
