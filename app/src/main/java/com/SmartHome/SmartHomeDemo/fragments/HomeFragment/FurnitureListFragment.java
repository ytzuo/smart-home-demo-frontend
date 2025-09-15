package com.SmartHome.SmartHomeDemo.fragments.HomeFragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.SmartHome.SmartHomeDemo.MainActivity;
import com.SmartHome.SmartHomeDemo.R;
import com.SmartHome.SmartHomeDemo.utils.ToastUtil;

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
        recyclerView.setLayoutAnimation(AnimationUtils.loadLayoutAnimation(getContext(), R.anim.layout_animation_fall_down));
        recyclerView.scheduleLayoutAnimation();

        // 检查是否有更新的数据
        if (getArguments() != null && getArguments().containsKey("updated_list")) {
            furnitureList = (List<FurnitureItem>) getArguments().getSerializable("updated_list");
            getArguments().remove("updated_list"); // 移除已使用的数据
        }
        adapter = new FurniturePageAdapter(furnitureList);

        // 设置点击事件监听器
        adapter.setOnItemClickLitener(new FurniturePageAdapter.OnItemClickLitener() {
            @Override
            public void onItemClick(FurnitureItem item) {
                Log.i("FurnitureListFragment", item.getStatus());
                ToastUtil.showToast(getContext(), "点击了: " + item.getDeviceId(), Toast.LENGTH_SHORT);
                // 使用FragmentTransaction显示HomeFurnitureSpecific Fragment
                HomeFurnitureSpecific fragment = new HomeFurnitureSpecific();

                // 传递参数
                Bundle args = new Bundle();
                args.putSerializable("furniture_item", item);
                fragment.setArguments(args);

                // 使用FragmentTransaction显示Fragment
                if (getActivity() != null) {
                    // 隐藏HomeFragment中的内容
                    if (getActivity() instanceof MainActivity) {
                        MainActivity mainActivity = (MainActivity) getActivity();
                        HomeFragment homeFragment = mainActivity.getCurrentHomeFragment();
                        if (homeFragment != null) {
                            homeFragment.hideAllFragments();
                        }
                    }

                    getActivity().getSupportFragmentManager()
                            .beginTransaction()
                            .add(R.id.fragment_container, fragment)
                            .addToBackStack(null)
                            .commit();
                }
            }
        });
        recyclerView.setAdapter(adapter);

        return view;
    }

    // 添加更新数据的方法
    // 添加更新数据的方法
    public void updateFurnitureList(List<FurnitureItem> newList) {
        if (newList != null) {
            this.furnitureList = newList;
            if (adapter != null) {
                adapter.updateData(furnitureList);
            } else {
                // 如果adapter为null，说明Fragment还没有创建视图，保存数据以便在onCreateView中使用
                if (getArguments() == null) {
                    setArguments(new Bundle());
                }
                getArguments().putSerializable("updated_list", (ArrayList<FurnitureItem>) newList);
            }
        }
    }
    // 添加更新特定家具项的方法
    public void updateSpecificFurnitureItem(FurnitureItem updatedItem) {
        if (updatedItem != null && furnitureList != null && adapter != null) {
            // 查找并更新匹配的家具项
            for (int i = 0; i < furnitureList.size(); i++) {
                FurnitureItem item = furnitureList.get(i);
                if (item.getDeviceId().equals(updatedItem.getDeviceId())) {
                    furnitureList.set(i, updatedItem);
                    adapter.notifyItemChanged(i);  // 只更新特定项而不是整个列表
                    break;
                }
            }
        }
    }

    // 当从HomeFurnitureSpecific返回时，刷新整个列表
    @Override
    public void onResume() {
        super.onResume();
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }
}
