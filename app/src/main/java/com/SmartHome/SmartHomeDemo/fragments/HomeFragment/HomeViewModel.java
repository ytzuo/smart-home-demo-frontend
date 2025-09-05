package com.SmartHome.SmartHomeDemo.fragments.HomeFragment;

import android.app.Application;
import android.util.Log;

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
    String TAG = "HomeViewModel";
    private MutableLiveData<List<FurnitureItem>> furnitureListLiveData;
    private List<FurnitureItem> furnitureList;
    private AppDatabase database;

    public HomeViewModel(Application application) {
        super(application);
        furnitureList = new ArrayList<>();
        furnitureListLiveData = new MutableLiveData<>(furnitureList);

//        //初始化测试数据
//        furnitureList.add(new FurnitureItem(
//                "智能灯具1", "light", "工作中", "开启", "今天 14:30", R.drawable.icon_light,
//                0, "0000", 80));
//        furnitureList.add(new FurnitureItem(
//                "智能空调1", "air_conditioner", "工作中", "制冷 24℃", "今天 10:15", R.drawable.icon_air_conditioner,
//                27, "1001", 0));
//        furnitureList.add(new FurnitureItem(
//                "智能灯具2", "light", "待机中", "关闭", "昨天 18:20", R.drawable.icon_light,
//                0, "0000", 70));
//        furnitureList.add(new FurnitureItem(
//                "智能空调2", "air_conditioner", "工作中", "播放中", "今天 09:45", R.drawable.icon_air_conditioner,
//                25, "1011", 0));
//        furnitureList.add(new FurnitureItem(
//                "智能灯具3", "light", "待机中", "关闭", "昨天 22:30", R.drawable.icon_light,
//                0, "0000",0));
//        furnitureListLiveData.setValue(furnitureList);

        //实测数据库时再启用
        database = ((SmartHomeApplication) application).getDatabase();

        //这一段仅供测试数据使用
        //检查数据库是否为空，如果为空则插入测试数据
        AppDatabase.databaseWriteExecutor.execute(() -> {
            List<Device> devices = database.deviceDao().getAllDevices();
//            if (devices.isEmpty()) {
//                //仅在数据库为空时插入测试数据
//                String[] ids = {"智能灯具1", "智能空调1", "智能灯具2", "智能空调2", "智能灯具3"};
//                String[] types = {"light", "air_conditioner", "light", "air_conditioner", "light"};
//                for(int i = 0 ; i < 5; i++) {
//                    Device newDevice = new Device();
//                    newDevice.setDeviceId(ids[i]);
//                    newDevice.setDeviceType(types[i]);
//                    database.deviceDao().insertDevice(newDevice);
//                }
//            }

            //从数据库加载数据
            loadFurnitureFromDatabase();
        });
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
            Log.i(TAG, devices.toString());

            for(Device device : devices) {
                int icon = R.drawable.icon_air_conditioner;
                if(Objects.equals(device.getDeviceType(), "light")){
                    icon = R.drawable.icon_light;
                } else if (Objects.equals(device.getDeviceType(), "air_conditioner")){
                    icon = R.drawable.icon_air_conditioner;
                }
                FurnitureItem item = new FurnitureItem(
                        device.getDeviceId(),
                        device.getDeviceType(),
                        "未连接",
                        "未连接",
                        "默认时间",
                        icon,
                        0,
                        "00000000",
                        0
                );
                FurnitureItems.add(item);
            }
            furnitureListLiveData.postValue(FurnitureItems);
        });
    }
}
