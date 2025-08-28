package com.SmartHome.SmartHomeDemo.fragments.CarFragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.SmartHome.SmartHomeDemo.R;

public class CarFragment extends Fragment {
    public CarFragment(){}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_car, container, false);
    }
}
