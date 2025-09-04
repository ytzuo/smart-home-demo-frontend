package com.SmartHome.SmartHomeDemo.fragments.LogFragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.SmartHome.SmartHomeDemo.R;
import com.SmartHome.SmartHomeDemo.application.SmartHomeApplication;
import com.SmartHome.SmartHomeDemo.fragments.HomeFragment.FurnitureItem;
import com.SmartHome.SmartHomeDemo.fragments.LogFragment.LogAdapter;
import com.SmartHome.SmartHomeDemo.fragments.LogFragment.LogItem;
import com.SmartHome.SmartHomeDemo.fragments.LogFragment.LogViewModel;
import com.SmartHome.SmartHomeDemo.utils.ToastUtil;

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
                ToastUtil.showToast(getContext(), "点击了: " + item.getLogID(), Toast.LENGTH_SHORT);
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
}
