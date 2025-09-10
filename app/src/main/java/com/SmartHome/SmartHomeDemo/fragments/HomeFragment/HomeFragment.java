package com.SmartHome.SmartHomeDemo.fragments.HomeFragment;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.Observer;
import androidx.viewpager2.widget.ViewPager2;

import com.SmartHome.SmartHomeDemo.R;
import com.SmartHome.SmartHomeDemo.application.SmartHomeApplication;
import com.SmartHome.SmartHomeDemo.database.AppDatabase;
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
    // SharedPreferences的键名
    private static final String GROUP_PREFS_NAME = "GroupNames";
    private static final String GROUP_1_KEY = "group_1_name";
    private static final String GROUP_2_KEY = "group_2_name";
    private static final String GROUP_3_KEY = "group_3_name";
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

        // 为TabLayout中的标签设置双击编辑功能
        setupTabDoubleClickEdit();

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
                String type = item.getDeviceType();
                switch (type) {
                    case "light" :
                        item.setImageResource(R.drawable.icon_light);
                        break;
                    case "air_conditioner" :
                        item.setImageResource(R.drawable.icon_air_conditioner);
                        break;
                    case "ac" :
                        item.setImageResource(R.drawable.icon_air_conditioner);
                        break;
                }
                String itemGroup = item.getDeviceGroup();
                //Log.d("HomeFragment", "Processing item: " + item.getDeviceId() + " with group: " + itemGroup);

                // 查找设备组在分类列表中的索引
                int categoryIndex = -1;
                if (itemGroup != null && !itemGroup.isEmpty()) {
                    categoryIndex = categories.indexOf(itemGroup);
                }

                // 如果找到了对应的分组，就添加到该分组中
                if (categoryIndex >= 0) {
                    categorizedFurniture.get(categoryIndex).add(item);
                    //Log.d("HomeFragment", "Added item to category: " + categories.get(categoryIndex));
                } else {
                    // 如果没有找到分组或者分组为空，默认添加到第一个默认分组
                    // 从SharedPreferences获取第一个默认分组的名称
                    SharedPreferences prefs = getActivity().getSharedPreferences(GROUP_PREFS_NAME, Context.MODE_PRIVATE);
                    String firstGroupName = prefs.getString(GROUP_1_KEY, "默认分组1");
                    int defaultGroupIndex = categories.indexOf(firstGroupName);
                    if (defaultGroupIndex >= 0 && defaultGroupIndex < categorizedFurniture.size()) {
                        categorizedFurniture.get(defaultGroupIndex).add(item);
                        //Log.d("HomeFragment", "Added item to default group: " + firstGroupName);
                    }
                }
            }
        }

        // 记录每个分类的项目数量
        for (int i = 0; i < categories.size() && i < categorizedFurniture.size(); i++) {
            //og.d("HomeFragment", "Category " + categories.get(i) + " has " + categorizedFurniture.get(i).size() + " items");
        }

        // 通知适配器数据已更改，使用post方法避免生命周期冲突
        if (viewPagerAdapter != null) {
            if (viewPager2 != null) {
                viewPager2.post(() -> {
                    if (viewPagerAdapter != null) {
                        viewPagerAdapter.updateData(categories, categorizedFurniture);
                        //Log.d("HomeFragment", "Notified adapter of data change");
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

            // 确保默认分组存在且在正确位置，并从SharedPreferences恢复名称
            restoreGroupNames();

            Log.d("HomeFragment", "Ensured default categories are in place");
        }
    }



    // 添加更新HomeStatus的方法
    public void handleHomeStatus(HomeStatus homeStatus) {
        if (homeViewModel != null) {
            //Log.i("HomeFragment", "handleHomeStatus");
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

                    // 获取当前家具列表的副本
                    List<FurnitureItem> currentItems = new ArrayList<>();
                    if (homeViewModel.getFurnitureLiveData().getValue() != null) {
                        currentItems.addAll(homeViewModel.getFurnitureLiveData().getValue());
                    }

                    // 创建deviceId到FurnitureItem的映射，便于快速查找和更新
                    Map<String, FurnitureItem> currentItemMap = new HashMap<>();
                    for (FurnitureItem item : currentItems) {
                        currentItemMap.put(item.getDeviceId(), item);
                    }

                    // 处理HomeStatus数据，只更新相关家具项
                    int len = homeStatus.deviceIds.length();
                    for(int i = 0; i < len; i++) {
                        String deviceId = homeStatus.deviceIds.get_at(i);

                        // 从映射中查找设备，时间复杂度O(1)
                        Device existingDevice = deviceMap.get(deviceId);

                        // 只处理数据库中存在的设备
                        if (existingDevice != null) {
                            // 检查此设备是否已经在当前列表中
                            FurnitureItem existingItem = currentItemMap.get(deviceId);
                            FurnitureItem newItem;

                            if (existingItem != null) {
                                // 如果已存在，创建一个副本进行更新
                                newItem = new FurnitureItem(
                                        existingItem.getDeviceId(),
                                        existingItem.getDeviceType(),
                                        existingItem.getWorkingStatus(),
                                        existingItem.getStatus(),
                                        existingItem.getTime(),
                                        existingItem.getImageResource(),
                                        existingItem.getAcTemp(),
                                        existingItem.getSwitchStatus(),
                                        existingItem.getLightPercent()
                                );
                                newItem.setDeviceGroup(existingItem.getDeviceGroup());
                                newItem.setFurnitureDataPack(existingItem.getFurnitureDataPack());
                            } else {
                                // 如果不存在，创建新项目
                                newItem = new FurnitureItem();
                                newItem.setDeviceId(deviceId);
                                newItem.setDeviceType(homeStatus.deviceTypes.get_at(i));
                                newItem.setDeviceGroup(existingDevice.getDeviceGroup()); // 从数据库获取分组信息

                                switch(newItem.getDeviceType()) {
                                    case "light":
                                        newItem.setImageResource(R.drawable.icon_light);
                                        break;
                                    case "air_conditioner":
                                        newItem.setImageResource(R.drawable.icon_air_conditioner);
                                        break;
                                }
                            }

                            // 更新时间戳
                            newItem.setTime(homeStatus.timeStamp);

                            // 更新数据
                            //Log.i("newItem", "开始读取JSON");
//                            if(newItem.receiveDataJson(homeStatus.deviceStatus.get_at(i))) {
//                                Log.i("newItem", newItem.getFurnitureDataPack().toString());
//                            } else {
//                                Log.i("newItem", "JSON转换失败/更新失败 " + homeStatus.deviceStatus.get_at(i));
//                            }

                            // 更新映射中的项目
                            currentItemMap.put(deviceId, newItem);
                        }
                    }

                    // 将更新后的项目转换为列表
                    List<FurnitureItem> updatedItems = new ArrayList<>(currentItemMap.values());

                    // 在主线程中更新UI
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            if (homeViewModel != null) {
                                homeViewModel.updateFurnitureList(updatedItems);
                            }
                        });
                    }
                } catch (Exception e) {
                    Log.e("HomeFragment", "处理HomeStatus时出错", e);
                    // 在主线程中处理错误情况
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            // 不要清空列表，保持现有数据
                            // 如果需要错误提示可以在这里添加
                        });
                    }
                }
            });
        }
    }


    public HomeViewModel getHomeViewModel() {
        return homeViewModel;
    }

    public void hideAllFragments() {
        if (viewPager2 != null) {
            viewPager2.setVisibility(View.GONE);
        }
        if (tabLayout != null) {
            tabLayout.setVisibility(View.GONE);
        }
        if(test_btn != null) {
            test_btn.setVisibility(View.GONE);
        }
    }

    public void showAllFragments() {
        if (viewPager2 != null) {
            viewPager2.setVisibility(View.VISIBLE);
        }
        if (tabLayout != null) {
            tabLayout.setVisibility(View.VISIBLE);
        }
        if(test_btn != null) {
            test_btn.setVisibility(View.VISIBLE);
        }
    }

    private void setupTabDoubleClickEdit() {
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                // 不需要实现
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                // 不需要实现
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                // 当重新选择标签时（可以视为双击），启用编辑模式
                int position = tab.getPosition();
                // 只有默认分组（位置1, 2, 3）可以编辑
                if (position >= 1 && position <= 3) {
                    enableGroupEditMode(position);
                }
            }
        });
    }

    private void enableGroupEditMode(final int position) {
        // 保存修改前的分组名
        final String oldGroupName = categories.get(position);
        // 创建一个 EditText 对话框
        final EditText editText = new EditText(this.getContext());
        editText.setText(categories.get(position));

        AlertDialog.Builder builder = new AlertDialog.Builder(this.getContext());
        builder.setTitle("修改分组名称");
        builder.setView(editText);

        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String newName = editText.getText().toString();
                if (!newName.isEmpty()) {
                    // 更新UI
                    categories.set(position, newName);
                    // 通知适配器数据已更改
                    if (viewPagerAdapter != null) {
                        viewPagerAdapter.notifyDataSetChanged();
                    }
                    // 保存到SharedPreferences
                    saveGroupName(position, newName);
                    // 更新数据库中对应设备的分组名
                    updateDeviceGroupInDatabase(oldGroupName, newName);
                }
            }
        });

        builder.setNegativeButton("取消", null);
        builder.show();
    }
    // 保存分组名称到SharedPreferences
    private void saveGroupName(int position, String name) {
        SharedPreferences prefs = getActivity().getSharedPreferences(GROUP_PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        String key = "";
        switch (position) {
            case 1:
                key = GROUP_1_KEY;
                break;
            case 2:
                key = GROUP_2_KEY;
                break;
            case 3:
                key = GROUP_3_KEY;
                break;
        }
        if (!key.isEmpty()) {
            editor.putString(key, name);
            editor.apply();
        }
    }

    // 从SharedPreferences恢复分组名称
    private void restoreGroupNames() {
        SharedPreferences prefs = getActivity().getSharedPreferences(GROUP_PREFS_NAME, Context.MODE_PRIVATE);
        // 恢复默认分组名称，如果没有保存的名称则使用默认值
        if (categories.size() > 1) {
            categories.set(1, prefs.getString(GROUP_1_KEY, "默认分组1"));
        }
        if (categories.size() > 2) {
            categories.set(2, prefs.getString(GROUP_2_KEY, "默认分组2"));
        }
        if (categories.size() > 3) {
            categories.set(3, prefs.getString(GROUP_3_KEY, "默认分组3"));
        }
    }

    private void updateDeviceGroupInDatabase(String oldGroupName, String newGroupName) {
        if (app != null && app.getDatabase() != null) {
            AppDatabase.databaseWriteExecutor.execute(() -> {
                try {
                    // 更新数据库中所有属于旧分组名的设备
                    app.getDatabase().deviceDao().updateGroup(oldGroupName, newGroupName);

                    Log.i("HomeFragment", "已更新数据库中分组名: " + oldGroupName + " -> " + newGroupName);
                } catch (Exception e) {
                    Log.e("HomeFragment", "更新数据库分组名时出错", e);
                }
            });
        }
    }

    // 添加处理夜间模式切换的方法
    public void updateGroupBarTheme() {
        if (tabLayout != null && getActivity() != null) {
            boolean isNightMode = (getActivity().getResources().getConfiguration().uiMode
                    & android.content.res.Configuration.UI_MODE_NIGHT_MASK)
                    == android.content.res.Configuration.UI_MODE_NIGHT_YES;

            // 设置TabLayout背景颜色
            int backgroundColor = isNightMode ?
                    R.color.card_background_dark : R.color.card_background_light;
            tabLayout.setBackgroundColor(getActivity().getResources().getColor(backgroundColor));

            // 设置Tab文本颜色
            int textColor = isNightMode ?
                    R.color.text_color_dark : R.color.text_color_light;

            // 使用ColorStateList设置Tab文本颜色（适用于选中和未选中状态）
            ColorStateList colorStateList = ColorStateList.valueOf(
                    getActivity().getResources().getColor(textColor)
            );
            tabLayout.setTabTextColors(colorStateList);
        }
    }
    // 重写onConfigurationChanged方法
    @Override
    public void onConfigurationChanged(android.content.res.Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        // 更新分组栏主题
        updateGroupBarTheme();

        // 更新ViewPager中的内容
        if (viewPagerAdapter != null) {
            viewPagerAdapter.notifyDataSetChanged();
        }
    }

    public void refreshData() {
        Log.i("refresh", "开始刷新数据");
        if (getActivity() != null && homeViewModel != null) {
            AppDatabase database = ((SmartHomeApplication) getActivity().getApplication()).getDatabase();
            if (database != null) {
                Executors.newSingleThreadExecutor().execute(() -> {
                    try {
                        List<Device> devices = database.deviceDao().getAllDevices();
                        List<FurnitureItem> furnitureItems = new ArrayList<>();

                        for (Device dev : devices) {
                            if ("light".equals(dev.getDeviceType())
                                    || "air_conditioner".equals(dev.getDeviceType())
                                    || "ac".equals(dev.getDeviceType())) {
                                FurnitureItem item = new FurnitureItem();
                                item.setDeviceId(dev.getDeviceId());
                                item.setDeviceType(dev.getDeviceType());
                                item.setDeviceGroup(dev.getDeviceGroup());
                                item.setWorkingStatus("未连接");
                                item.setStatus("未连接");
                                item.setTime("默认时间");

                                // 设置图片资源
                                if ("light".equals(dev.getDeviceType())) {
                                    item.setImageResource(R.drawable.icon_light);
                                } else if ("air_conditioner".equals(dev.getDeviceType()) || "ac".equals(dev.getDeviceType())) {
                                    item.setImageResource(R.drawable.icon_air_conditioner);
                                }

                                furnitureItems.add(item);
                            }
                        }

                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                homeViewModel.updateFurnitureList(furnitureItems);
                            });
                        }
                    } catch (Exception e) {
                        Log.e("HomeFragment", "刷新数据时出错", e);
                    }
                });
            }
        }
    }
}
