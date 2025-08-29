package com.SmartHome.SmartHomeDemo.fragments.LogFragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.SmartHome.SmartHomeDemo.R;
import com.SmartHome.SmartHomeDemo.fragments.LogFragment.LogAdapter;
import com.SmartHome.SmartHomeDemo.fragments.LogFragment.LogItem;
import com.SmartHome.SmartHomeDemo.fragments.LogFragment.LogViewModel;

import java.util.ArrayList;
import java.util.List;

public class LogFragment extends Fragment {
    public LogFragment(){}
    private RecyclerView recyclerView;
    private LogAdapter adapter;
    private LogViewModel logViewModel;

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
                Toast.makeText(getContext(), "点击了: " + item.getLogID(), Toast.LENGTH_SHORT).show();
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
