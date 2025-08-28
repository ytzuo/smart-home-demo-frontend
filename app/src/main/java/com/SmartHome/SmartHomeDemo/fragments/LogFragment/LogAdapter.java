package com.SmartHome.SmartHomeDemo.fragments.LogFragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.SmartHome.SmartHomeDemo.R;
import com.SmartHome.SmartHomeDemo.fragments.HomeFragment.FurnitureItem;
import com.SmartHome.SmartHomeDemo.fragments.HomeFragment.HomeAdapter;
import com.google.android.material.button.MaterialButton;
import androidx.core.content.ContextCompat;
import java.util.List;

public class LogAdapter extends RecyclerView.Adapter<LogAdapter.ViewHolder> {
    private List<LogItem> logList;
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
        LogItem item = logList.get(position);
        holder.bind(item, listener, position);
    }

    @Override
    public int getItemCount() {
        return logList.size();
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
}
