package com.SmartHome.SmartHomeDemo.fragments.HomeFragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.SmartHome.SmartHomeDemo.R;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {
    public HomeFragment(){}
    private RecyclerView recyclerView;
    private HomeAdapter adapter;
    private List<FurnitureItem> furnitureList;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_furniture, container, false);

        // 初始化RecyclerView
        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // 初始化数据
        initData();

        // 设置适配器
        adapter = new HomeAdapter(furnitureList);
        recyclerView.setAdapter(adapter);

        // 设置点击事件
        adapter.setOnItemClickListener(new HomeAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(FurnitureItem item, int position) {
                Toast.makeText(getContext(), "点击了: " + item.getName(), Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }

    private void initData() { //这里是测试数据, 正式版删去
        furnitureList = new ArrayList<>();
        furnitureList.add(new FurnitureItem("智能灯具", "工作中", "开启", "今天 14:30", R.drawable.ic_launcher_foreground));
        furnitureList.add(new FurnitureItem("智能空调", "工作中", "制冷 24℃", "今天 10:15", R.drawable.ic_launcher_foreground));
        furnitureList.add(new FurnitureItem("智能窗帘", "待机中", "关闭", "昨天 18:20", R.drawable.ic_launcher_foreground));
        furnitureList.add(new FurnitureItem("智能音响", "工作中", "播放中", "今天 09:45", R.drawable.ic_launcher_foreground));
        furnitureList.add(new FurnitureItem("智能电视", "待机中", "关闭", "昨天 22:30", R.drawable.ic_launcher_foreground));
    }
}
