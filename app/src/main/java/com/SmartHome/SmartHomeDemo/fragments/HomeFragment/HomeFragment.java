package com.SmartHome.SmartHomeDemo.fragments.HomeFragment;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.Observer;
import androidx.viewpager2.widget.ViewPager2;

import com.SmartHome.SmartHomeDemo.R;
import com.SmartHome.SmartHomeDemo.application.SmartHomeApplication;
import com.SmartHome.SmartHomeDemo.database.Device;
import com.SmartHome.SmartHomeDemo.utils.ToastUtil;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.HashSet;
import java.util.Set;

import idl.SmartDemo03.HomeStatus;

public class HomeFragment extends Fragment {
    public HomeFragment(){}
    private ViewPager2 viewPager2;
    private TabLayout tabLayout;
    private FurnitureViewPagerAdapter viewPagerAdapter;
    private HomeViewModel homeViewModel;

    //测试用按钮, 用于清空数据库
    private Button test_btn;
    private SmartHomeApplication app;
    private ExecutorService executorService = Executors.newSingleThreadExecutor();

    // 分类数据
    private List<String> categories = new ArrayList<>(Arrays.asList("全部", "默认分组1", "默认分组2", "默认分组3"));
    private List<List<FurnitureItem>> categorizedFurniture = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_furniture, container, false);

        // 初始化ViewModel
        homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);

        // 初始化分类列表
        setupCategories();
        categorizedFurniture.clear();
        for (int i = 0; i < categories.size(); i++) {
            categorizedFurniture.add(new ArrayList<>());
        }

        // 初始化ViewPager2和TabLayout
        viewPager2 = view.findViewById(R.id.viewPager2);
        tabLayout = view.findViewById(R.id.tab_layout);

        // 设置适配器
        viewPagerAdapter = new FurnitureViewPagerAdapter(getActivity(), categories, categorizedFurniture);
        viewPager2.setAdapter(viewPagerAdapter);

        // 连接TabLayout和ViewPager2
        new TabLayoutMediator(tabLayout, viewPager2,
                (tab, position) -> tab.setText(viewPagerAdapter.getPageTitle(position))
        ).attach();

        //测试用按钮, 用于清空数据库
        test_btn = view.findViewById(R.id.btn_test_del_all);
        app = (SmartHomeApplication) getActivity().getApplication();
        test_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                executorService.execute(new Runnable() {
                    @Override
                    public void run() {
                        Log.i("HomeFragment", "删除按钮被点击");
                        app.getDatabase().deviceDao().deleteAll();
                        // 删除完成后，在主线程更新UI
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    // 方式1: 通过ViewModel更新LiveData数据
                                    if (homeViewModel != null) {
                                        homeViewModel.updateFurnitureList(new ArrayList<FurnitureItem>());
                                    }
                                    // 方式2: 显示提示信息
                                    ToastUtil.showToast(getContext(), "数据已清空", Toast.LENGTH_SHORT);

                                    // 更新所有分类数据
                                    for (List<FurnitureItem> list : categorizedFurniture) {
                                        list.clear();
                                    }
                                    if (viewPagerAdapter != null) {
                                        viewPagerAdapter.notifyDataSetChanged();
                                    }
                                }
                            });
                        }
                    }
                });
            }
        });

        // 观察家具数据变化
        homeViewModel.getFurnitureLiveData().observe(getViewLifecycleOwner(), new Observer<List<FurnitureItem>>() {
            @Override
            public void onChanged(List<FurnitureItem> furnitureItems) {
                Log.d("HomeFragment", "Furniture live data changed, updating categorized furniture");
                updateCategorizedFurniture(furnitureItems);
            }
        });

        return view;
    }

    private void updateCategorizedFurniture(List<FurnitureItem> furnitureItems) {
        Log.d("HomeFragment", "updateCategorizedFurniture called with " + (furnitureItems != null ? furnitureItems.size() : 0) + " items");

        // 确保分类结构正确
        setupCategories();

        // 清空所有分类列表
        for (List<FurnitureItem> list : categorizedFurniture) {
            list.clear();
        }

        // 确保有足够的分类列表
        while (categorizedFurniture.size() < categories.size()) {
            categorizedFurniture.add(new ArrayList<>());
        }

        // 将所有家具项添加到"全部"分类
        if (categorizedFurniture.size() > 0 && furnitureItems != null) {
            categorizedFurniture.get(0).addAll(furnitureItems);
            Log.d("HomeFragment", "Added " + furnitureItems.size() + " items to '全部' category");
        }

        // 将家具项按组分类到对应的分组
        if (furnitureItems != null) {
            for (FurnitureItem item : furnitureItems) {
                String itemGroup = item.getDeviceGroup();
                Log.d("HomeFragment", "Processing item: " + item.getDeviceId() + " with group: " + itemGroup);

                // 查找设备组在分类列表中的索引
                int categoryIndex = -1;
                if (itemGroup != null && !itemGroup.isEmpty()) {
                    categoryIndex = categories.indexOf(itemGroup);
                }

                // 如果找到了对应的分组，就添加到该分组中
                if (categoryIndex >= 0) {
                    categorizedFurniture.get(categoryIndex).add(item);
                    Log.d("HomeFragment", "Added item to category: " + categories.get(categoryIndex));
                } else {
                    // 如果没有找到分组或者分组为空，默认添加到默认分组1
                    int defaultGroupIndex = categories.indexOf("默认分组1");
                    if (defaultGroupIndex >= 0 && defaultGroupIndex < categorizedFurniture.size()) {
                        categorizedFurniture.get(defaultGroupIndex).add(item);
                        Log.d("HomeFragment", "Added item to default group: 默认分组1");
                    }
                }
            }
        }

        // 记录每个分类的项目数量
        for (int i = 0; i < categories.size() && i < categorizedFurniture.size(); i++) {
            Log.d("HomeFragment", "Category " + categories.get(i) + " has " + categorizedFurniture.get(i).size() + " items");
        }

        // 通知适配器数据已更改，使用post方法避免生命周期冲突
        if (viewPagerAdapter != null) {
            if (viewPager2 != null) {
                viewPager2.post(() -> {
                    if (viewPagerAdapter != null) {
                        viewPagerAdapter.updateData(categories, categorizedFurniture);
                        Log.d("HomeFragment", "Notified adapter of data change");
                    }
                });
            }
        }
    }


    private void setupCategories() {
        // 确保分类列表结构正确
        if (categories.isEmpty()) {
            categories.add("全部");
            categories.add("默认分组1");
            categories.add("默认分组2");
            categories.add("默认分组3");
            Log.d("HomeFragment", "Initialized categories");
        } else {
            // 确保"全部"在第一位
            if (!"全部".equals(categories.get(0))) {
                categories.add(0, "全部");
                Log.d("HomeFragment", "Added '全部' to beginning of categories");
            }

            // 确保默认分组存在且在正确位置
            if (categories.size() < 2) categories.add("默认分组1");
            else categories.set(1, "默认分组1");

            if (categories.size() < 3) categories.add("默认分组2");
            else categories.set(2, "默认分组2");

            if (categories.size() < 4) categories.add("默认分组3");
            else categories.set(3, "默认分组3");

            Log.d("HomeFragment", "Ensured default categories are in place");
        }
    }



    // 添加更新HomeStatus的方法
    public void handleHomeStatus(HomeStatus homeStatus) {
        if (homeViewModel != null) {
            // 在后台线程中处理数据库操作
            Executors.newSingleThreadExecutor().execute(() -> {
                try {
                    // 从数据库获取现有设备列表（仅执行一次）
                    List<Device> devicesInDatabase = new ArrayList<>();
                    if (app != null && app.getDatabase() != null) {
                        devicesInDatabase = app.getDatabase().deviceDao().getAllDevices();
                    }

                    // 创建设备ID到设备的映射，提高查找效率
                    Map<String, Device> deviceMap = new HashMap<>();
                    for (Device device : devicesInDatabase) {
                        deviceMap.put(device.getDeviceId(), device);
                    }

                    // 处理HomeStatus数据
                    List<FurnitureItem> items = new ArrayList<>();
                    int len = homeStatus.deviceIds.length();

                    for(int i = 0; i < len; i++) {
                        String deviceId = homeStatus.deviceIds.get_at(i);

                        // 从映射中查找设备，时间复杂度O(1)
                        Device existingDevice = deviceMap.get(deviceId);

                        // 只处理数据库中存在的设备
                        if (existingDevice != null) {
                            FurnitureItem newItem = new FurnitureItem();
                            newItem.setDeviceId(deviceId);
                            newItem.setDeviceType(homeStatus.deviceTypes.get_at(i));
                            newItem.setTime(homeStatus.timeStamp);
                            newItem.setDeviceGroup(existingDevice.getDeviceGroup()); // 从数据库获取分组信息

                            switch(newItem.getDeviceType()) {
                                case "light":
                                    newItem.setImageResource(R.drawable.icon_light);
                                    break;
                                case "air_conditioner":
                                    newItem.setImageResource(R.drawable.icon_air_conditioner);
                                    break;
                            }

                            Log.i("newItem", "开始读取JSON");
                            if(newItem.receiveDataJson(homeStatus.deviceStatus.get_at(i))) {
                                Log.i("newItem", newItem.getFurnitureDataPack().toString());
                            } else {
                                Log.i("newItem", "JSON转换失败/更新失败 "
                                        + homeStatus.deviceStatus.get_at(i));
                            }
                            items.add(newItem);
                        }
                    }

                    // 在主线程中更新UI
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            if (homeViewModel != null) {
                                homeViewModel.updateFurnitureList(items);
                            }
                        });
                    }
                } catch (Exception e) {
                    Log.e("HomeFragment", "处理HomeStatus时出错", e);
                    // 在主线程中处理错误情况
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            if (homeViewModel != null) {
                                homeViewModel.updateFurnitureList(new ArrayList<>());
                            }
                        });
                    }
                }
            });
        }
    }



    public HomeViewModel getHomeViewModel() {
        return homeViewModel;
    }
}
