package com.SmartHome.SmartHomeDemo.fragments.SettingFragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SceneModeFurnitureAdapter extends RecyclerView.Adapter<SceneModeFurnitureAdapter.ViewHolder> {
    private List<Device> deviceList;
    private List<String> selectedDeviceIds;  // 修改：使用List而不是Set
    private List<String> selectedDeviceTypes; // 保持：存储选中设备的类型
    private String sceneModeKey;
    private Context context;

    // 设备类型存储的键名
    private static final String SCENE_MODE_DEVICE_TYPES_SUFFIX = "_types";

    public SceneModeFurnitureAdapter(Context context, List<Device> deviceList, String sceneModeKey) {
        this.context = context;
        this.deviceList = deviceList;
        this.sceneModeKey = sceneModeKey;
        this.selectedDeviceIds = new ArrayList<>();  // 修改：初始化为ArrayList
        this.selectedDeviceTypes = new ArrayList<>(); // 初始化设备类型列表

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

    public List<String> getSelectedDeviceIds() {  // 修改：返回List而不是Set
        return new ArrayList<>(selectedDeviceIds);
    }

    public void saveSelectedDevices() {
        Log.i(sceneModeKey, selectedDeviceIds.toString());
        SharedPreferences prefs = context.getSharedPreferences("SceneModes", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        // 修改：将选中的设备ID列表转换为逗号分隔的字符串进行存储
        editor.putString(sceneModeKey, String.join(",", selectedDeviceIds));
        Log.i(sceneModeKey, String.join(",", selectedDeviceIds));
        // 保存选中设备的类型列表，与ID列表顺序一一对应
        editor.putString(sceneModeKey + SCENE_MODE_DEVICE_TYPES_SUFFIX, String.join(",", selectedDeviceTypes));
        Log.i(sceneModeKey, String.join(",", selectedDeviceTypes));
        editor.apply();
    }

    private void loadSelectedDevices() {
        SharedPreferences prefs = context.getSharedPreferences("SceneModes", Context.MODE_PRIVATE);

        // 加载设备ID列表（只使用新格式：逗号分隔的字符串）
        selectedDeviceIds.clear();
        String savedIdsString = prefs.getString(sceneModeKey, "");
        if (!savedIdsString.isEmpty()) {
            // 新格式：逗号分隔的字符串
            String[] idsArray = savedIdsString.split(",");
            for (String id : idsArray) {
                if (!id.isEmpty()) {  // 避免添加空字符串
                    selectedDeviceIds.add(id);
                }
            }
        }
        // 不再尝试读取旧的Set格式，完全使用字符串格式

        // 加载设备类型列表
        String savedTypesString = prefs.getString(sceneModeKey + SCENE_MODE_DEVICE_TYPES_SUFFIX, "");
        selectedDeviceTypes.clear();
        if (!savedTypesString.isEmpty()) {
            String[] typesArray = savedTypesString.split(",");
            for (String type : typesArray) {
                if (!type.isEmpty()) {  // 避免添加空字符串
                    selectedDeviceTypes.add(type);
                }
            }
        }

        // 确保ID和类型列表长度一致（数据完整性保护）
        if (selectedDeviceIds.size() != selectedDeviceTypes.size()) {
            // 如果长度不一致，以ID列表为准截断或补充类型列表
            if (selectedDeviceTypes.size() > selectedDeviceIds.size()) {
                // 截断类型列表
                selectedDeviceTypes = new ArrayList<>(selectedDeviceTypes.subList(0, selectedDeviceIds.size()));
            } else {
                // 补充类型列表
                while (selectedDeviceTypes.size() < selectedDeviceIds.size()) {
                    selectedDeviceTypes.add("");
                }
            }
        }


        // 确保ID和类型列表长度一致（数据完整性保护）
        if (selectedDeviceIds.size() != selectedDeviceTypes.size()) {
            // 如果长度不一致，以ID列表为准截断或补充类型列表
            if (selectedDeviceTypes.size() > selectedDeviceIds.size()) {
                // 截断类型列表
                selectedDeviceTypes = new ArrayList<>(selectedDeviceTypes.subList(0, selectedDeviceIds.size()));
            } else {
                // 补充类型列表
                while (selectedDeviceTypes.size() < selectedDeviceIds.size()) {
                    selectedDeviceTypes.add("");
                }
            }
        }
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
                    // 修改：检查是否已存在，避免重复添加
                    if (!selectedDeviceIds.contains(device.getDeviceId())) {
                        selectedDeviceIds.add(device.getDeviceId());
                        // 同时添加设备类型
                        selectedDeviceTypes.add(device.getDeviceType());
                    }
                } else {
                    // 修改：移除指定的设备ID和对应的设备类型
                    int index = selectedDeviceIds.indexOf(device.getDeviceId());
                    if (index != -1) {
                        selectedDeviceIds.remove(index);
                        selectedDeviceTypes.remove(index);
                    }
                }
            });

            // 设置整个项的点击事件，切换选中状态
            itemView.setOnClickListener(v -> {
                checkBox.setChecked(!checkBox.isChecked());
            });
        }
    }

    // 获取选中的设备类型列表
    public List<String> getSelectedDeviceTypes() {
        return new ArrayList<>(selectedDeviceTypes);
    }
}
