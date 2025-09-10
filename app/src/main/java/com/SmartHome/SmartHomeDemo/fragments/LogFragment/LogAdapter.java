package com.SmartHome.SmartHomeDemo.fragments.LogFragment;

import android.content.res.Configuration;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.SmartHome.SmartHomeDemo.R;
import com.google.android.material.button.MaterialButton;
import androidx.core.content.ContextCompat;
import java.util.List;
import java.util.ArrayList;

public class LogAdapter extends RecyclerView.Adapter<LogAdapter.ViewHolder> {
    private List<LogItem> logList;
    private List<LogItem> filteredLogList; // 用于存储过滤后的日志列表
    private List<String> filterDeviceIds; // 用于存储过滤设备ID
    private LogAdapter.OnItemClickListener listener;

    // 定义点击监听器接口
    public interface OnItemClickListener {
        void onItemClick(LogItem item, int position);
    }

    // 设置点击监听器
    public void setOnItemClickListener(LogAdapter.OnItemClickListener listener) {
        this.listener = listener;
    }

    // 构造函数
    public LogAdapter(List<LogItem> logList) {
        this.logList = logList;
        this.filteredLogList = new ArrayList<>(logList);
        this.filterDeviceIds = new ArrayList<>();
    }

    @NonNull
    @Override
    public LogAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_log, parent, false);
        return new LogAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LogAdapter.ViewHolder holder, int position) {
        LogItem item = filteredLogList.get(position);
        holder.bind(item, listener, position);
    }

    @Override
    public int getItemCount() {
        return filteredLogList.size();
    }

    // ViewHolder类
    public static class ViewHolder extends RecyclerView.ViewHolder {
        private MaterialButton logType;
        private TextView logTime;
        private TextView logMsg;
        private TextView logID;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            logType = itemView.findViewById(R.id.log_type);
            logTime = itemView.findViewById(R.id.log_time);
            logID   = itemView.findViewById(R.id.log_id);
            logMsg  = itemView.findViewById(R.id.log_msg);
        }

        public void bind(final LogItem item, final LogAdapter.OnItemClickListener listener, final int position) {
            logType.setText(item.getLogType());
            logTime.setText(item.getLogTime());
            logID.setText(item.getLogID());
            logMsg.setText(item.getLogMsg());

            // 检查当前是否为夜间模式
            boolean isNightMode = (itemView.getContext().getResources().getConfiguration().uiMode
                    & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES;
            Log.i("LogAdapter", "bind" + isNightMode);
            // 设置文本颜色
            int textColor = isNightMode ? R.color.text_color_dark : R.color.text_color_light;
            logTime.setTextColor(ContextCompat.getColor(logTime.getContext(), textColor));
            logID.setTextColor(ContextCompat.getColor(logID.getContext(), textColor));
            logMsg.setTextColor(ContextCompat.getColor(logMsg.getContext(), textColor));

            String log_type = item.getLogType();
            if ("INFO".equals(log_type)) {
                logType.setBackgroundTintList(ContextCompat.getColorStateList(itemView.getContext(), R.color.INFO_background));
                logType.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.INFO_text));
            } else if ("WARN".equals(log_type)) {
                logType.setBackgroundTintList(ContextCompat.getColorStateList(itemView.getContext(), R.color.WARN_background));
                logType.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.WARN_text));
            } else if ("ALERT".equals(log_type)) {
                logType.setBackgroundTintList(ContextCompat.getColorStateList(itemView.getContext(), R.color.ALERT_background));
                logType.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.ALERT_text));
            }
            // 设置点击事件
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        listener.onItemClick(item, position);
                    }
                }
            });
        }
    }

    public void updateData(List<LogItem> newLogList) {
        this.logList = newLogList;
        applyFilter(); // 应用当前过滤器
    }

    // 设置过滤设备ID并应用过滤
    public void setFilterDeviceIds(List<String> filterDeviceIds) {
        this.filterDeviceIds = filterDeviceIds != null ? new ArrayList<>(filterDeviceIds) : new ArrayList<>();
        applyFilter();
    }

    // 应用过滤器
    private void applyFilter() {
        filteredLogList.clear();

        // 如果没有过滤条件，则显示所有日志
        if (filterDeviceIds.isEmpty()) {
            filteredLogList.addAll(logList);
        } else {
            // 根据设备ID过滤日志
            for (LogItem logItem : logList) {
                Log.i("LogAdapter", "LogId: " + logItem.getLogID() + " LogDevice: " + logItem.getLogDevice());
                if (filterDeviceIds.contains(logItem.getLogDevice())) {
                    filteredLogList.add(logItem);
                }
            }
        }

        notifyDataSetChanged();
    }
}
