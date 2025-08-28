package com.SmartHome.SmartHomeDemo.fragments.LogFragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.SmartHome.SmartHomeDemo.R;
import com.SmartHome.SmartHomeDemo.fragments.HomeFragment.FurnitureItem;
import com.SmartHome.SmartHomeDemo.fragments.HomeFragment.HomeAdapter;

import java.util.ArrayList;
import java.util.List;

public class LogFragment extends Fragment {
    public LogFragment(){}
    private RecyclerView recyclerView;
    private LogAdapter adapter;
    private List<LogItem> logList;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_log, container, false);

        // 初始化RecyclerView
        recyclerView = view.findViewById(R.id.log_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // 初始化数据
        initData();

        // 设置适配器
        adapter = new LogAdapter(logList);
        recyclerView.setAdapter(adapter);

        // 设置点击事件
        adapter.setOnItemClickListener(new LogAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(LogItem item, int position) {
                Toast.makeText(getContext(), "点击了: " + item.getLogID(), Toast.LENGTH_SHORT).show();
            }
        });

        return view;

    }

    private void initData() { //这里是测试数据, 正式版删去
        logList = new ArrayList<>();
        logList.add(new LogItem("INFO", "2025-8-28 19:53", "log01", "测试日志1"));
        logList.add(new LogItem("WARN", "2025-8-28 19:53", "log02", "测试日志2"));
        logList.add(new LogItem("ALERT", "2025-8-28 19:53", "log03", "测试日志3"));
    }
}
