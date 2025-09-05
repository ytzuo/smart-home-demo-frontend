package com.SmartHome.SmartHomeDemo.fragments.HomeFragment;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import java.util.ArrayList;
import java.util.List;

public class FurnitureViewPagerAdapter extends FragmentStateAdapter {
    private List<String> categories;
    private List<List<FurnitureItem>> categorizedFurniture;
    private List<FurnitureListFragment> fragments;

    public FurnitureViewPagerAdapter(@NonNull FragmentActivity fragmentActivity,
                                     List<String> categories,
                                     List<List<FurnitureItem>> categorizedFurniture) {
        super(fragmentActivity);
        this.categories = categories;
        this.categorizedFurniture = categorizedFurniture;
        this.fragments = new ArrayList<>();
        // 初始化fragment列表
        for (int i = 0; i < categories.size(); i++) {
            fragments.add(null);
        }
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        FurnitureListFragment fragment = FurnitureListFragment.newInstance(categorizedFurniture.get(position));
        // 保存fragment引用
        if (position < fragments.size()) {
            fragments.set(position, fragment);
        } else {
            // 确保fragment列表大小足够
            while (fragments.size() <= position) {
                fragments.add(null);
            }
            fragments.set(position, fragment);
        }
        return fragment;
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    public CharSequence getPageTitle(int position) {
        return categories.get(position);
    }

    // 添加更新数据的方法
    public void updateData(List<String> categories, List<List<FurnitureItem>> categorizedFurniture) {
        this.categories = categories;
        this.categorizedFurniture = categorizedFurniture;

        // 更新每个fragment的数据
        for (int i = 0; i < Math.min(fragments.size(), categorizedFurniture.size()); i++) {
            if (fragments.get(i) != null) {
                fragments.get(i).updateFurnitureList(categorizedFurniture.get(i));
            }
        }

        notifyDataSetChanged();
    }
}
