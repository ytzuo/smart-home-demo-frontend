package com.SmartHome.SmartHomeDemo.fragments.SettingFragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.SmartHome.SmartHomeDemo.R;
import com.SmartHome.SmartHomeDemo.database.Device;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SceneModeFurnitureAdapter extends RecyclerView.Adapter<SceneModeFurnitureAdapter.ViewHolder> {
    private List<Device> deviceList;
    private Set<String> selectedDeviceIds;
    private String sceneModeKey;
    private Context context;

    public SceneModeFurnitureAdapter(Context context, List<Device> deviceList, String sceneModeKey) {
        this.context = context;
        this.deviceList = deviceList;
        this.sceneModeKey = sceneModeKey;
        this.selectedDeviceIds = new HashSet<>();

        // 从SharedPreferences加载已选中的设备
        loadSelectedDevices();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_scene_furniture, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Device device = deviceList.get(position);
        holder.bind(device);
    }

    @Override
    public int getItemCount() {
        return deviceList.size();
    }

    public Set<String> getSelectedDeviceIds() {
        return selectedDeviceIds;
    }

    public void saveSelectedDevices() {
        SharedPreferences prefs = context.getSharedPreferences("SceneModes", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        // 将选中的设备ID集合转换为字符串集合进行存储
        Set<String> selectedIds = new HashSet<>(selectedDeviceIds);
        editor.putStringSet(sceneModeKey, selectedIds);
        editor.apply();
    }

    private void loadSelectedDevices() {
        SharedPreferences prefs = context.getSharedPreferences("SceneModes", Context.MODE_PRIVATE);
        Set<String> savedIds = prefs.getStringSet(sceneModeKey, new HashSet<>());
        selectedDeviceIds.addAll(savedIds);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView itemImage;
        private TextView nameText;
        private CheckBox checkBox;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            itemImage = itemView.findViewById(R.id.item_type);
            nameText = itemView.findViewById(R.id.name_furniture);
            checkBox = itemView.findViewById(R.id.checkbox_selected);
        }

        public void bind(final Device device) {
            // 设置设备信息
            nameText.setText(device.getDeviceId());

            // 根据设备类型设置图标和状态文本
            if ("light".equals(device.getDeviceType())) {
                itemImage.setImageResource(R.drawable.icon_light); // 需要确保有这个资源
            } else if ("air_conditioner".equals(device.getDeviceType())) {
                itemImage.setImageResource(R.drawable.icon_air_conditioner); // 需要确保有这个资源
            } else {
                itemImage.setImageResource(R.drawable.icon_air_conditioner); // 默认图标
            }

            // 设置选中状态
            checkBox.setChecked(selectedDeviceIds.contains(device.getDeviceId()));

            // 设置复选框点击事件
            checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    selectedDeviceIds.add(device.getDeviceId());
                } else {
                    selectedDeviceIds.remove(device.getDeviceId());
                }
            });

            // 设置整个项的点击事件，切换选中状态
            itemView.setOnClickListener(v -> {
                checkBox.setChecked(!checkBox.isChecked());
            });
        }
    }
}
