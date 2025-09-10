package com.SmartHome.SmartHomeDemo.fragments.LogFragment;

import android.content.Context;
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
import java.util.List;

public class LogFilterAdapter extends RecyclerView.Adapter<LogFilterAdapter.ViewHolder> {
    private List<Device> deviceList;
    private List<String> selectedDeviceIds;
    private Context context;

    public LogFilterAdapter(Context context, List<Device> deviceList) {
        this.context = context;
        this.deviceList = deviceList;
        this.selectedDeviceIds = new ArrayList<>();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_log_filter_furniture, parent, false);
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

    public List<String> getSelectedDeviceIds() {
        return new ArrayList<>(selectedDeviceIds);
    }

    public void setSelectedDeviceIds(List<String> selectedIds) {
        this.selectedDeviceIds.clear();
        this.selectedDeviceIds.addAll(selectedIds);
        notifyDataSetChanged();
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

            // 根据设备类型设置图标
            if ("light".equals(device.getDeviceType())) {
                itemImage.setImageResource(R.drawable.icon_light);
            } else if ("air_conditioner".equals(device.getDeviceType()) || "ac".equals(device.getDeviceType())) {
                itemImage.setImageResource(R.drawable.icon_air_conditioner);
            } else {
                itemImage.setImageResource(R.drawable.icon_air_conditioner); // 默认图标
            }

            // 设置选中状态
            checkBox.setChecked(selectedDeviceIds.contains(device.getDeviceId()));

            // 设置复选框点击事件
            checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    if (!selectedDeviceIds.contains(device.getDeviceId())) {
                        selectedDeviceIds.add(device.getDeviceId());
                    }
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
