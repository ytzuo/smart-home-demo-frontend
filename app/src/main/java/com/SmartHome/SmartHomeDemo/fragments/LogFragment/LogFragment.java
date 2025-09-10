package com.SmartHome.SmartHomeDemo.fragments.LogFragment;

import static androidx.core.app.NotificationCompat.getColor;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
//import android.util.Log;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.app.AlertDialog;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.SmartHome.SmartHomeDemo.R;
import com.SmartHome.SmartHomeDemo.application.SmartHomeApplication;
import com.SmartHome.SmartHomeDemo.database.AppDatabase;
import com.SmartHome.SmartHomeDemo.database.Device;
import com.SmartHome.SmartHomeDemo.fragments.HomeFragment.FurnitureItem;
import com.SmartHome.SmartHomeDemo.fragments.LogFragment.LogAdapter;
import com.SmartHome.SmartHomeDemo.fragments.LogFragment.LogItem;
import com.SmartHome.SmartHomeDemo.fragments.LogFragment.LogViewModel;
import com.SmartHome.SmartHomeDemo.utils.ToastUtil;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LogFragment extends Fragment {
    public LogFragment(){}
    private RecyclerView recyclerView;
    private LogAdapter adapter;
    private LogViewModel logViewModel;
    private Button test_btn;
    private Button filter_btn;
    private SmartHomeApplication app;
    private AppDatabase database;
    private ExecutorService executorService = Executors.newSingleThreadExecutor();
    private List<String> filteredDeviceIds = new ArrayList<>(); // 用于存储筛选条件

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_log, container, false);
        // 初始化ViewModel
        logViewModel = new ViewModelProvider(this).get(LogViewModel.class);

        // 初始化RecyclerView
        recyclerView = view.findViewById(R.id.log_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // 设置适配器
        adapter = new LogAdapter(new ArrayList<LogItem>());
        recyclerView.setAdapter(adapter);

        //测试用按钮, 用于清空数据库
        test_btn = view.findViewById(R.id.log_btn_test_del_all);
        app = (SmartHomeApplication) getActivity().getApplication();
        database = app.getDatabase();
        test_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                executorService.execute(new Runnable() {
                    @Override
                    public void run() {
                        app.getDatabase().logDao().deleteAllLogs();
                        // 删除完成后，在主线程更新UI
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    // 方式1: 通过ViewModel更新LiveData数据
                                    if (logViewModel != null) {
                                        logViewModel.updateLogList(new ArrayList<LogItem>());
                                    }
                                    // 方式2: 显示提示信息
                                    ToastUtil.showToast(getContext(), "数据已清空", Toast.LENGTH_SHORT);
                                }
                            });
                        }
                    }
                });
            }
        });

        filter_btn = view.findViewById(R.id.log_btn_filter);
        filter_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showLogFilterDialog();
            }
        });
        loadFilterPreferences();


        // 观察日志数据变化
        logViewModel.getLogListLiveData().observe(getViewLifecycleOwner(), new Observer<List<LogItem>>() {
            @Override
            public void onChanged(List<LogItem> logItems) {
                adapter.updateData(logItems);
            }
        });

        // 设置点击事件
        adapter.setOnItemClickListener(new LogAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(LogItem item, int position) {
                //Log.i("LogFragment", "点击了: " + item.getLogID());
                ToastUtil.showToast(getContext(), "点击了: " + item.getLogID(), Toast.LENGTH_SHORT);
                showLogDetailDialog(item);
            }
        });

        return view;

    }

    // 提供一个方法用于外部添加日志
    public void addLogItem(LogItem logItem) {
        logViewModel.addLogItem(logItem);
    }

    // 提供一个方法用于更新整个日志列表
    public void updateLogList(List<LogItem> newLogList) {
        adapter.updateData(newLogList);
    }

    private void showLogDetailDialog(LogItem item) {
        // 加载对话框布局
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        View dialogView = inflater.inflate(R.layout.window_alert_detail, null);

        TextView logDescription = dialogView.findViewById(R.id.detail_log_description);
        TextView logDevice      = dialogView.findViewById(R.id.detail_log_device);
        TextView logTime        = dialogView.findViewById(R.id.detail_log_time);
        TextView logId          = dialogView.findViewById(R.id.detail_log_id);

        MaterialButton logLevel = dialogView.findViewById(R.id.detail_log_type);
        ImageView logImage      = dialogView.findViewById(R.id.detail_log_image);
        Button btn              = dialogView.findViewById(R.id.detail_log_confirm);

        String level = item.getLogType();
        if(logDescription != null) {
            logDescription.setText("日志信息  " + item.getLogMsg());
        }
        if(logDevice != null) {
            logDevice.setText("来源设备  " + item.getLogDevice());
        }
        if(logTime != null) {
            logTime.setText("日志时间  " + item.getLogTime());
        }
        if(logId != null) {
            logId.setText("日志ID  " + item.getLogID());
        }

        switch(level) {
            case "INFO":
                logLevel.setText(getString(R.string.INFO));
                logLevel.setTextColor(getResources().getColor(R.color.INFO_text));
                logLevel.setBackgroundColor(getResources().getColor(R.color.INFO_background));
                break;
            case "WARN":
                logLevel.setText(getString(R.string.WARN));
                logLevel.setTextColor(getResources().getColor(R.color.WARN_text));
                logLevel.setBackgroundColor(getResources().getColor(R.color.WARN_background));
                break;
            case "ALERT":
                logLevel.setText(getString(R.string.ALERT));
                logLevel.setTextColor(getResources().getColor(R.color.ALERT_text));
                logLevel.setBackgroundColor(getResources().getColor(R.color.ALERT_background));
                break;
        }

        if(logImage != null) {
            logImage.setVisibility(View.GONE);
            // 在后台线程中查询数据库
            executorService.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        // 根据logId从数据库获取Log对象
                        String logIdStr = item.getLogID();
                        android.util.Log.i("LogFragment", "准备查询logId: " + logIdStr);
                        String imgPath = app.getDatabase().logDao().getImagePathByLogId(logIdStr);
                        android.util.Log.i("LogFragment", "查询到的图片路径: " + imgPath);

                        // 加载图片
                        Bitmap bitmap = BitmapFactory.decodeFile(imgPath);
                        if (bitmap != null && getActivity() != null) {
                            android.util.Log.i("LogFragment", "成功加载图片，准备在UI中显示");
                            // 在主线程中更新UI
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    logImage.setImageBitmap(bitmap);
                                    logImage.setVisibility(View.VISIBLE);
                                    android.util.Log.i("LogFragment", "图片已在UI中显示");
                                }
                            });
                        } else {
                            android.util.Log.i("LogFragment", "未找到图片或无法加载图片，bitmap=" + bitmap + ", getActivity()=" + getActivity());
                        }

                    } catch (Exception e) {
                        android.util.Log.e("LogFragment", "加载图片时出错", e);
                        e.printStackTrace();
                    }
                }
            });
        }

        // 创建并显示对话框
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(getContext());
        builder.setView(dialogView);

        androidx.appcompat.app.AlertDialog dialog = builder.create();
        dialog.show();
        // 设置确认按钮点击事件
        if (btn != null) {
            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                }
            });
        }
    }

    private void showLogFilterDialog() {
        // 创建自定义对话框
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(requireContext());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.window_log_filter, null);
        builder.setView(dialogView);

        // 设置标题
        TextView titleText = dialogView.findViewById(R.id.filter_name);
        titleText.setText("筛选日志");

        // 初始化设备列表
        RecyclerView deviceRecyclerView = dialogView.findViewById(R.id.customize_device_recyclerView);
        deviceRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        // 设置按钮点击事件
        Button cancelButton = dialogView.findViewById(R.id.customize_cancel);
        Button doneButton = dialogView.findViewById(R.id.customize_done);

        androidx.appcompat.app.AlertDialog dialog = builder.create();

        cancelButton.setOnClickListener(v -> dialog.dismiss());

        // 先显示对话框
        dialog.show();

        // 在后台线程中获取设备列表
        if (database != null) {
            AppDatabase.databaseWriteExecutor.execute(() -> {
                // 在后台线程中查询数据库
                List<Device> deviceList = getDevicesFromDatabase();

                // 切换到主线程更新UI
                requireActivity().runOnUiThread(() -> {
                    LogFilterAdapter filterAdapter = new LogFilterAdapter(requireContext(), deviceList);
                    filterAdapter.setSelectedDeviceIds(filteredDeviceIds);
                    deviceRecyclerView.setAdapter(filterAdapter);

                    doneButton.setOnClickListener(v -> {
                        // 保存选中的设备
                        filteredDeviceIds = filterAdapter.getSelectedDeviceIds();
                        saveFilterPreferences();

                        // 更新日志显示
                        adapter.setFilterDeviceIds(filteredDeviceIds);
                        Log.i("LogFragment", "当前筛选器设备列表："+filteredDeviceIds);
                        dialog.dismiss();
                    });
                });
            });
        } else {
            // 如果数据库不可用，使用空列表
            List<Device> deviceList = new ArrayList<>();
            LogFilterAdapter filterAdapter = new LogFilterAdapter(requireContext(), deviceList);
            filterAdapter.setSelectedDeviceIds(filteredDeviceIds);
            deviceRecyclerView.setAdapter(filterAdapter);
            doneButton.setOnClickListener(v -> {
                filteredDeviceIds = filterAdapter.getSelectedDeviceIds();
                saveFilterPreferences();

                // 更新日志显示
                adapter.setFilterDeviceIds(filteredDeviceIds);
                Log.i("LogFragment", "当前筛选器设备列表："+filteredDeviceIds);

                dialog.dismiss();
            });
        }
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

        return deviceList;
    }

    // 保存筛选条件到SharedPreferences
    private void saveFilterPreferences() {
        android.content.SharedPreferences prefs = requireContext().getSharedPreferences("LogFilter", android.content.Context.MODE_PRIVATE);
        android.content.SharedPreferences.Editor editor = prefs.edit();
        editor.putString("filtered_device_ids", String.join(",", filteredDeviceIds));
        editor.apply();
    }

    // 从SharedPreferences加载筛选条件
    private void loadFilterPreferences() {
        android.content.SharedPreferences prefs = requireContext().getSharedPreferences("LogFilter", android.content.Context.MODE_PRIVATE);
        String savedIdsString = prefs.getString("filtered_device_ids", "");
        filteredDeviceIds.clear();
        if (!savedIdsString.isEmpty()) {
            String[] idsArray = savedIdsString.split(",");
            for (String id : idsArray) {
                if (!id.isEmpty()) {
                    filteredDeviceIds.add(id);
                }
            }
        }
        // 应用已保存的过滤条件
        adapter.setFilterDeviceIds(filteredDeviceIds);
    }

    private void updateLogDisplay() {
        // 触发日志列表更新以应用筛选条件
        logViewModel.refreshLogs();
    }

    /**
     * 刷新数据的方法
     * 从数据库重新加载日志数据
     */
    public void refreshData() {
        if (logViewModel != null && database != null) {
            AppDatabase.databaseWriteExecutor.execute(() -> {
                try {
                    // 从数据库获取最新的日志数据
                    List<com.SmartHome.SmartHomeDemo.database.Log> logs = database.logDao().getAllLogs();
                    List<LogItem> logItems = new ArrayList<>();

                    // 转换为LogItem列表
                    for (com.SmartHome.SmartHomeDemo.database.Log log : logs) {
                        LogItem item = new LogItem(
                                log.getLogType(),
                                log.getTimestamp(),
                                log.getLogId(),
                                log.getDescription(),
                                log.getLogDevice()
                        );
                        logItems.add(item);
                    }

                    // 在主线程中更新UI
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            logViewModel.updateLogList(logItems);
                        });
                    }
                } catch (Exception e) {
                    Log.e("LogFragment", "刷新数据时出错", e);
                }
            });
        }
    }
}
