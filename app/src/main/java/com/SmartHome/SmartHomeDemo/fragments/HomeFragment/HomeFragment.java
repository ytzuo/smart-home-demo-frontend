package com.SmartHome.SmartHomeDemo.fragments.HomeFragment;

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

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {
    public HomeFragment(){}
    private RecyclerView recyclerView;
    private HomeAdapter adapter;
    private HomeViewModel homeViewModel;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_furniture, container, false);

        // 初始化ViewModel
        homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);
        // 初始化RecyclerView
        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // 设置适配器
        adapter = new HomeAdapter(new ArrayList<FurnitureItem>());
        recyclerView.setAdapter(adapter);

        // 观察家具数据变化
        homeViewModel.getFurnitureLiveData().observe(getViewLifecycleOwner(), new Observer<List<FurnitureItem>>() {
            @Override
            public void onChanged(List<FurnitureItem> furnitureItems) {
                adapter.updateData(furnitureItems);
            }
        });

        // 设置点击事件
        adapter.setOnItemClickListener(new HomeAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(FurnitureItem item, int position) {
                Toast.makeText(getContext(), "点击了: " + item.getName(), Toast.LENGTH_SHORT).show();
            }
        });

        return view;

    }
}
