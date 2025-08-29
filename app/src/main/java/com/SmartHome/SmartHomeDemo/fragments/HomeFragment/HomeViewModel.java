package com.SmartHome.SmartHomeDemo.fragments.HomeFragment;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.SmartHome.SmartHomeDemo.R;
import com.SmartHome.SmartHomeDemo.fragments.LogFragment.LogItem;

import java.util.ArrayList;
import java.util.List;

public class HomeViewModel extends ViewModel {
    private MutableLiveData<List<FurnitureItem>> furnitureListLiveData;
    private List<FurnitureItem> furnitureList;

    public HomeViewModel() {
        furnitureList = new ArrayList<>();
        furnitureListLiveData = new MutableLiveData<>(furnitureList);

        //初始化测试数据
        furnitureList.add(new FurnitureItem("智能灯具", "工作中", "开启", "今天 14:30", R.drawable.ic_launcher_foreground));
        furnitureList.add(new FurnitureItem("智能空调", "工作中", "制冷 24℃", "今天 10:15", R.drawable.ic_launcher_foreground));
        furnitureList.add(new FurnitureItem("智能窗帘", "待机中", "关闭", "昨天 18:20", R.drawable.ic_launcher_foreground));
        furnitureList.add(new FurnitureItem("智能音响", "工作中", "播放中", "今天 09:45", R.drawable.ic_launcher_foreground));
        furnitureList.add(new FurnitureItem("智能电视", "待机中", "关闭", "昨天 22:30", R.drawable.ic_launcher_foreground));
        furnitureListLiveData.setValue(furnitureList);
    }

    public LiveData<List<FurnitureItem>> getFurnitureLiveData() {
        return furnitureListLiveData;
    }

    public void addFurnitureItem(FurnitureItem furnitureItem) {
        furnitureList.add(0, furnitureItem); // 添加到列表开头
        furnitureListLiveData.setValue(furnitureList);
    }

    public void updateFurnitureList(List<FurnitureItem> newFurnitureList) {
        furnitureList.clear();
        furnitureList.addAll(newFurnitureList);
        furnitureListLiveData.setValue(furnitureList);
    }

    public void removeFurnitureItem(int position) {
        if (position >= 0 && position < furnitureList.size()) {
            furnitureList.remove(position);
            furnitureListLiveData.setValue(furnitureList);
        }
    }

}
