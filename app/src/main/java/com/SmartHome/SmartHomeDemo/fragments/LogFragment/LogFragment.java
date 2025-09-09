package com.SmartHome.SmartHomeDemo.fragments.LogFragment;

import static androidx.core.app.NotificationCompat.getColor;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
//import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.SmartHome.SmartHomeDemo.R;
import com.SmartHome.SmartHomeDemo.application.SmartHomeApplication;
import com.SmartHome.SmartHomeDemo.database.AppDatabase;
import com.SmartHome.SmartHomeDemo.fragments.HomeFragment.FurnitureItem;
import com.SmartHome.SmartHomeDemo.fragments.LogFragment.LogAdapter;
import com.SmartHome.SmartHomeDemo.fragments.LogFragment.LogItem;
import com.SmartHome.SmartHomeDemo.fragments.LogFragment.LogViewModel;
import com.SmartHome.SmartHomeDemo.utils.ToastUtil;
import com.google.android.material.button.MaterialButton;
import com.SmartHome.SmartHomeDemo.database.Log;

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
    private SmartHomeApplication app;
    private AppDatabase database;
    private ExecutorService executorService = Executors.newSingleThreadExecutor();

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
        logViewModel.updateLogList(newLogList);
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
                        String imgPath = app.getDatabase().logDao().getImagePathByLogId(logIdStr);

                        // 加载图片
                        Bitmap bitmap = BitmapFactory.decodeFile(imgPath);
                        if (bitmap != null && getActivity() != null) {
                            // 在主线程中更新UI
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    logImage.setImageBitmap(bitmap);
                                    logImage.setVisibility(View.VISIBLE);
                                }
                            });
                        }

                    } catch (Exception e) {
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
}
