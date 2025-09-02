package com.SmartHome.SmartHomeDemo.fragments.HomeFragment;

import android.content.Intent;
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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HomeFragment extends Fragment {
    public HomeFragment(){}
    private RecyclerView recyclerView;
    private HomeAdapter adapter;
    private HomeViewModel homeViewModel;

    //测试用按钮, 用于清空数据库
    private Button test_btn;
    private SmartHomeApplication app;
    private ExecutorService executorService = Executors.newSingleThreadExecutor();

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

        //测试用按钮, 用于清空数据库
        test_btn = view.findViewById(R.id.btn_test_del_all);
        app = (SmartHomeApplication) getActivity().getApplication();
        test_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                executorService.execute(new Runnable() {
                    @Override
                    public void run() {
                        app.getDatabase().deviceDao().deleteAll();
                    }
                });
            }
        });

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
                Toast.makeText(getContext(), "点击了: " + item.getDeviceId(), Toast.LENGTH_SHORT).show();
                // 使用FragmentTransaction显示HomeFurnitureSpecific Fragment
                HomeFurnitureSpecific fragment = new HomeFurnitureSpecific();

                // 传递参数
                Bundle args = new Bundle();
                args.putSerializable("furniture_item", item);
                fragment.setArguments(args);

                // 使用FragmentTransaction显示Fragment
                if (getActivity() != null) {
                    getActivity().getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.fragment_container, fragment)
                            .addToBackStack(null)
                            .commit();
                }
            }
        });

        return view;

    }
}
