package com.SmartHome.SmartHomeDemo.fragments.SettingFragment;

import android.content.Context;
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
import com.SmartHome.SmartHomeDemo.database.BlacklistedDevice;

import java.util.ArrayList;
import java.util.List;

public class BlacklistDeviceAdapter extends RecyclerView.Adapter<BlacklistDeviceAdapter.ViewHolder> {
    private List<BlacklistedDevice> blacklistedDevices;
    private List<BlacklistedDevice> selectedDevicesForRemoval;
    private Context context;
    private String TAG = "BlacklistDeviceAdapter";

    public BlacklistDeviceAdapter(Context context, List<BlacklistedDevice> blacklistedDevices) {
        this.context = context;
        this.blacklistedDevices = blacklistedDevices;
        this.selectedDevicesForRemoval = new ArrayList<>();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_blacklist_device, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        BlacklistedDevice device = blacklistedDevices.get(position);
        holder.bind(device);
    }

    @Override
    public int getItemCount() {
        return blacklistedDevices.size();
    }

    public List<BlacklistedDevice> getSelectedDevicesForRemoval() {
        return new ArrayList<>(selectedDevicesForRemoval);
    }

    public void removeSelectedDevices() {
        Log.i(TAG, "准备移除 " + selectedDevicesForRemoval.size() + " 个设备");
        blacklistedDevices.removeAll(selectedDevicesForRemoval);
        notifyDataSetChanged();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView deviceIcon;
        private TextView deviceIdText;
        private TextView deviceTypeText;
        private CheckBox checkBox;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            deviceIcon = itemView.findViewById(R.id.blacklist_device_icon);
            deviceIdText = itemView.findViewById(R.id.blacklist_device_id);
            deviceTypeText = itemView.findViewById(R.id.blacklist_device_type);
            checkBox = itemView.findViewById(R.id.blacklist_checkbox);
        }

        public void bind(BlacklistedDevice device) {
            deviceIdText.setText(device.getDeviceId());
            deviceTypeText.setText(device.getDeviceType());

            // 根据设备类型设置图标
            if ("light".equals(device.getDeviceType())) {
                deviceIcon.setImageResource(R.drawable.icon_light);
            } else if ("air_conditioner".equals(device.getDeviceType()) || "ac".equals(device.getDeviceType())) {
                deviceIcon.setImageResource(R.drawable.icon_air_conditioner);
            } else if ("car".equals(device.getDeviceType())) {
                deviceIcon.setImageResource(R.drawable.icon_car);
            } else {
                deviceIcon.setImageResource(R.drawable.icon_home);
            }

            // 设置复选框状态变化监听器
            checkBox.setOnCheckedChangeListener(null); // 先移除监听器避免冲突
            checkBox.setChecked(selectedDevicesForRemoval.contains(device));
            checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    if (!selectedDevicesForRemoval.contains(device)) {
                        selectedDevicesForRemoval.add(device);
                    }
                } else {
                    selectedDevicesForRemoval.remove(device);
                }
            });
        }
    }
}
