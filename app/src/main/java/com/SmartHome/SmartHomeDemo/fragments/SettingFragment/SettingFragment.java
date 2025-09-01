package com.SmartHome.SmartHomeDemo.fragments.SettingFragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;

import androidx.fragment.app.Fragment;

import com.SmartHome.SmartHomeDemo.R;

public class SettingFragment extends Fragment {
    private Switch darkModeSwitch;
    public SettingFragment(){}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_setting, container, false);
    }

    private void initialization(View view){
        darkModeSwitch = (Switch) view.findViewById(android.R.id.switch_widget);
        if(darkModeSwitch != null) {

        }
    }


}
