package com.SmartHome.SmartHomeDemo;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.*;

public class SpecificFurnitureActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_furniture_specific);

        initialization("light");

    }

    private void initialization(String CurrentFurnitureType){

        TextView temp;

        switch (CurrentFurnitureType) {
            case "air_conditioner":
                //设置图标
                ImageView tempImage = findViewById(R.id.iv_furniture);
                tempImage.setImageResource(R.drawable.icon_air_conditioner);

                //设置参数项文本
                temp = findViewById(R.id.arg1);
                temp.setText("状态");
                temp = findViewById(R.id.arg2);
                temp.setText("当前温度");

                //设置开关项文本
                temp = findViewById(R.id.switch_label_11);
                temp.setText("开关");
                temp = findViewById(R.id.switch_label_12);
                temp.setText("制冷");
                temp = findViewById(R.id.switch_label_13);
                temp.setText("扫风");
                temp = findViewById(R.id.switch_label_14);
                temp.setText("除湿");

                // 设置滑块项文本
                temp = findViewById(R.id.seekbar_label_1);
                temp.setText("温度");
                temp = findViewById(R.id.seekbar_label_2);
                temp.setText("风速");

                hideUnusedItems();

                break;

            case "light":
                //设置图标
                tempImage = findViewById(R.id.iv_furniture);
                tempImage.setImageResource(R.drawable.icon_light);

                //设置参数项文本
                temp = findViewById(R.id.arg1);
                temp.setText("状态");
                temp = findViewById(R.id.arg2);
                temp.setText("亮度");

                //设置开关项文本
                temp = findViewById(R.id.switch_label_11);
                temp.setText("开关");

                // 设置滑块项文本
                temp = findViewById(R.id.seekbar_label_1);
                temp.setText("亮度");

            default:
                // 默认情况下隐藏所有未使用的设置项
                hideUnusedItems();
                break;
        }

    }

    private void hideUnusedItems() {
        // 隐藏未使用的开关项
        for(int i = 1; i <= 2; i++) {
            for(int j = 1; j <= 4; j++) {
                // 构造开关标签ID
                int labelId = getResources().getIdentifier("switch_label_" + i + j, "id", getPackageName());

                // 检查ID是否存在
                if(labelId != 0) {
                    TextView label = findViewById(labelId);
                    if(label != null) {
                        // 如果标签文本包含"设置项"，则隐藏该项
                        if(label.getText().toString().contains("设置项")) {
                            // 获取包含TextView和Switch的LinearLayout并隐藏
                            LinearLayout parentLayout = (LinearLayout) label.getParent();
                            parentLayout.setVisibility(View.GONE);
                        }
                    }
                }
            }
        }


    }



}
