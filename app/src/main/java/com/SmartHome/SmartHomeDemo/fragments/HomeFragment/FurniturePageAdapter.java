package com.SmartHome.SmartHomeDemo.fragments.HomeFragment;

import android.content.res.Configuration;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.SmartHome.SmartHomeDemo.R;

import java.util.ArrayList;
import java.util.List;

public class FurniturePageAdapter extends RecyclerView.Adapter<FurniturePageAdapter.ViewHolder> {
    private List<FurnitureItem> furnitureList;
    private OnItemClickLitener onItemClickLitener;
    public interface OnItemClickLitener {
        void onItemClick(FurnitureItem item);
    }

    public void setOnItemClickLitener(OnItemClickLitener onItemClickLitener) {
        this.onItemClickLitener = onItemClickLitener;
    }

    // 构造函数
    public FurniturePageAdapter(List<FurnitureItem> furnitureList) {
        this.furnitureList = furnitureList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_furniture, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        FurnitureItem item = furnitureList.get(position);
        holder.bind(item);

        // 设置点击事件
        holder.itemView.setOnClickListener(v -> {
            if (onItemClickLitener != null) {
                onItemClickLitener.onItemClick(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return furnitureList.size();
    }

    // ViewHolder类
    public static class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView itemImage;
        private TextView nameText;
        private TextView workingText;
        private TextView statusText;
        private TextView timeText;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            itemImage   = itemView.findViewById(R.id.item_type);
            nameText    = itemView.findViewById(R.id.name_furniture);
            workingText = itemView.findViewById(R.id.working_furniture);
            statusText  = itemView.findViewById(R.id.status_furniture);
            timeText    = itemView.findViewById(R.id.item_time);
        }

        public void bind(final FurnitureItem item) {
            String status = item.getStatus();
            String type   = item.getDeviceType();
            FurnitureDataPack pack = item.getFurnitureDataPack();
            itemImage.setImageResource(item.getImageResource());
            nameText.setText(item.getDeviceId());

            // 检查当前是否为夜间模式
            boolean isNightMode = (itemImage.getContext().getResources().getConfiguration().uiMode
                    & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES;

            // 设置文本颜色
            int textColor = isNightMode ? R.color.text_color_dark : R.color.text_color_light;
            nameText.setTextColor(ContextCompat.getColor(nameText.getContext(), textColor));
            workingText.setTextColor(ContextCompat.getColor(workingText.getContext(), textColor));
            statusText.setTextColor(ContextCompat.getColor(statusText.getContext(), textColor));
            timeText.setTextColor(ContextCompat.getColor(timeText.getContext(), textColor));


//            // 使用FurnitureItem中的状态信息而不是直接访问pack
//            workingText.setText(item.getWorkingStatus());
//            statusText.setText(item.getStatus());

            if(!item.isOnline()) {
                workingText.setText("未连接设备");
                statusText.setText("连接设备以查看数据");
                itemImage.setColorFilter(itemView.getContext().getResources().getColor(R.color.gray));
                timeText.setText("--:--");
            }
            else {
                // 安全地处理pack和params
                if (pack != null && pack.getParams() != null && !pack.getParams().isEmpty()) {
                    if (pack.getStatus().get(0) == 1)
                        workingText.setText("工作中");
                    else
                        workingText.setText("已关机");
                    switch (type) {
                        case "light":
                            float bright = pack.getParams().get(0);
                            statusText.setText("亮度: " + bright + "%");
                            if (pack.getStatus().get(0) == 1) {
                                itemImage.setColorFilter(ContextCompat.getColor(itemImage.getContext(), R.color.WARN_text));
                            } else {
                                itemImage.setColorFilter(ContextCompat.getColor(itemImage.getContext(), R.color.gray));
                            }
                            break;
                        case "air_conditioner":
                            float temp = pack.getParams().get(0);
                            statusText.setText("温度: " + temp + "℃");
                            if (pack.getStatus().get(0) == 1) {
                                itemImage.setColorFilter(ContextCompat.getColor(itemImage.getContext(), R.color.blue));
                            } else {
                                itemImage.setColorFilter(ContextCompat.getColor(itemImage.getContext(), R.color.gray));
                            }
                            break;
                        case "ac":
                            temp = pack.getParams().get(0);
                            statusText.setText("温度: " + temp + "℃");
                            if (pack.getStatus().get(0) == 1) {
                                itemImage.setColorFilter(ContextCompat.getColor(itemImage.getContext(), R.color.blue));
                            } else {
                                itemImage.setColorFilter(ContextCompat.getColor(itemImage.getContext(), R.color.gray));
                            }
                            break;
                        default:
                            statusText.setText(status);
                            break;
                    }
                }

                timeText.setText(item.getTime());
            }
        }
    }

    public void updateData(List<FurnitureItem> newFurnitureList) {
        this.furnitureList = newFurnitureList != null ? newFurnitureList : new ArrayList<>();
        notifyDataSetChanged();
    }
}
