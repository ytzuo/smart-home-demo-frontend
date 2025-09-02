package com.SmartHome.SmartHomeDemo.fragments.SettingFragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;

import com.SmartHome.SmartHomeDemo.R;

public class SettingFragment extends Fragment {
    private Switch darkModeSwitch;
    private Switch NoticeSwitch;
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
        NoticeSwitch = view.findViewById(R.id.notification_allow_switch); // 修复：正确获取通知开关组件
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
    }
}
