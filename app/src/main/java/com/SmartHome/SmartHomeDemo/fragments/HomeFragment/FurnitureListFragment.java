package com.SmartHome.SmartHomeDemo.fragments.HomeFragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.SmartHome.SmartHomeDemo.R;

import java.util.ArrayList;
import java.util.List;

public class FurnitureListFragment extends Fragment {
    private static final String ARG_FURNITURE_LIST = "furniture_list";

    private List<FurnitureItem> furnitureList;
    private FurniturePageAdapter adapter;

    public FurnitureListFragment() {
        // Required empty public constructor
    }

    public static FurnitureListFragment newInstance(List<FurnitureItem> furnitureList) {
        FurnitureListFragment fragment = new FurnitureListFragment();
        Bundle args = new Bundle();
        // 不再传递实际的数据，只传递一个标识
        args.putInt("size", furnitureList != null ? furnitureList.size() : 0);
        fragment.setArguments(args);
        // 直接设置数据
        fragment.furnitureList = furnitureList != null ? furnitureList : new ArrayList<>();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (furnitureList == null) {
            furnitureList = new ArrayList<>();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_furniture_list, container, false);

        RecyclerView recyclerView = view.findViewById(R.id.recycler_view_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new FurniturePageAdapter(furnitureList);
        recyclerView.setAdapter(adapter);

        return view;
    }

    // 添加更新数据的方法
    public void updateFurnitureList(List<FurnitureItem> newList) {
        if (newList != null) {
            this.furnitureList = newList;
            if (adapter != null) {
                adapter.updateData(furnitureList);
            }
        }
    }
}
