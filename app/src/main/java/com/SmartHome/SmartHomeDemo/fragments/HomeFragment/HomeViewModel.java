package com.SmartHome.SmartHomeDemo.fragments.HomeFragment;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.SmartHome.SmartHomeDemo.R;
import com.SmartHome.SmartHomeDemo.application.SmartHomeApplication;
import com.SmartHome.SmartHomeDemo.database.AppDatabase;
import com.SmartHome.SmartHomeDemo.database.Device;
import com.SmartHome.SmartHomeDemo.fragments.LogFragment.LogItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class HomeViewModel extends AndroidViewModel {
    private MutableLiveData<List<FurnitureItem>> furnitureListLiveData;
    private List<FurnitureItem> furnitureList;
    private AppDatabase database;

    public HomeViewModel(Application application) {
        super(application);
        furnitureList = new ArrayList<>();
        furnitureListLiveData = new MutableLiveData<>(furnitureList);

        //初始化测试数据
        furnitureList.add(new FurnitureItem(
                "智能灯具1", "light", "工作中", "开启", "今天 14:30", R.drawable.icon_light,
                0, "0000", true, 80));
        furnitureList.add(new FurnitureItem(
                "智能空调1", "air_conditioner", "工作中", "制冷 24℃", "今天 10:15", R.drawable.icon_air_conditioner,
                27, "1001", false, 0));
        furnitureList.add(new FurnitureItem(
                "智能灯具2", "light", "待机中", "关闭", "昨天 18:20", R.drawable.icon_light,
                0, "0000", false, 70));
        furnitureList.add(new FurnitureItem(
                "智能空调2", "air_conditioner", "工作中", "播放中", "今天 09:45", R.drawable.icon_air_conditioner,
                25, "1011", false, 0));
        furnitureList.add(new FurnitureItem(
                "智能灯具2", "light", "待机中", "关闭", "昨天 22:30", R.drawable.icon_light,
                0, "0000", false, 0));
        furnitureListLiveData.setValue(furnitureList);

//        //实测数据库时再启用
//        database = ((SmartHomeApplication) application).getDatabase();
//
//         //从数据库加载日志
//        loadFurnitureFromDatabase();
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

    private void loadFurnitureFromDatabase() {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            List<Device> devices = database.deviceDao().getAllDevices();
            List<FurnitureItem> FurnitureItems = new ArrayList<>();

            for(Device device : devices) {
                int icon = R.drawable.icon_air_conditioner;
                if(Objects.equals(device.getDeviceType(), "light"))
                    icon = R.drawable.icon_light;
                else if (Objects.equals(device.getDeviceType(), "air_conditioner"))
                    icon = R.drawable.icon_air_conditioner;
                FurnitureItem item = new FurnitureItem(
                        device.getDeviceId(),
                        device.getDeviceType(),
                        "未连接",
                        "未连接",
                        "默认时间",
                        icon,
                        0,
                        "0000",
                        false,
                        0
                );
                FurnitureItems.add(item);
            }
            furnitureListLiveData.postValue(FurnitureItems);
        });
    }
}
