// 创建一个新的文件 FurnitureItemViewModel.java
package com.SmartHome.SmartHomeDemo.fragments.HomeFragment;

import androidx.lifecycle.ViewModel;
import java.util.HashMap;
import java.util.Map;

public class FurnitureItemViewModel extends ViewModel {
    // 使用Map存储不同设备的UI状态
    private Map<String, UIState> uiStates = new HashMap<>();

    public static class UIState {
        public String arg1Text;
        public String arg2Text;
        public String switchStatus;
        public float acTemp;
        public float lightPercent;
        // 添加其他需要保存的UI状态

        public UIState(String arg1Text, String arg2Text, String switchStatus,
                       float acTemp, float lightPercent) {
            this.arg1Text = arg1Text;
            this.arg2Text = arg2Text;
            this.switchStatus = switchStatus;
            this.acTemp = acTemp;
            this.lightPercent = lightPercent;
        }
    }

    public void saveUIState(String deviceId, UIState state) {
        uiStates.put(deviceId, state);
    }

    public UIState getUIState(String deviceId) {
        return uiStates.get(deviceId);
    }

    public boolean hasUIState(String deviceId) {
        return uiStates.containsKey(deviceId);
    }
}
