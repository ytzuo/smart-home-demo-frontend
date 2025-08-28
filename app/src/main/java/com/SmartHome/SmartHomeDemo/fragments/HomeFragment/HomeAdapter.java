package com.SmartHome.SmartHomeDemo.fragments.HomeFragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.SmartHome.SmartHomeDemo.R;
import java.util.List;

public class HomeAdapter extends RecyclerView.Adapter<HomeAdapter.ViewHolder> {
    private List<FurnitureItem> furnitureList;
    private OnItemClickListener listener;

    // 定义点击监听器接口
    public interface OnItemClickListener {
        void onItemClick(FurnitureItem item, int position);
    }

    // 设置点击监听器
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    // 构造函数
    public HomeAdapter(List<FurnitureItem> furnitureList) {
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
        holder.bind(item, listener, position);
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

        public void bind(final FurnitureItem item, final OnItemClickListener listener, final int position) {
            itemImage.setImageResource(item.getImageResource());
            nameText.setText(item.getName());
            workingText.setText(item.getWorkingStatus());
            statusText.setText(item.getStatus());
            timeText.setText(item.getTime());

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
