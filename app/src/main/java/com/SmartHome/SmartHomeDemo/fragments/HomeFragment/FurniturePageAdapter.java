package com.SmartHome.SmartHomeDemo.fragments.HomeFragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.SmartHome.SmartHomeDemo.R;

import java.util.List;

public class FurniturePageAdapter extends RecyclerView.Adapter<FurniturePageAdapter.ViewHolder> {
    private List<FurnitureItem> furnitureList;

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

            // 安全地处理pack和params
            if (pack != null && pack.getParams() != null && !pack.getParams().isEmpty()) {
                if(pack.getStatus().get(0) == 1)
                    workingText.setText("工作中");
                else
                    workingText.setText("已关机");
                switch (type) {
                    case "light" :
                        float bright = pack.getParams().get(0);
                        statusText.setText("亮度: "+bright+"%");
                        break;
                    case "air_conditioner" :
                        float temp = pack.getParams().get(0);
                        statusText.setText("温度: "+temp+"℃");
                        break;
                    default:
                        statusText.setText(status);
                        break;
                }
            }

            timeText.setText(item.getTime());
        }
    }

    public void updateData(List<FurnitureItem> newFurnitureList) {
        this.furnitureList = newFurnitureList;
        notifyDataSetChanged();
    }
}
