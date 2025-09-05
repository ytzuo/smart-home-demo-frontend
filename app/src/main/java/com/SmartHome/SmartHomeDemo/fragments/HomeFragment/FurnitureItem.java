package com.SmartHome.SmartHomeDemo.fragments.HomeFragment;

import android.util.Log;

import com.SmartHome.SmartHomeDemo.R;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class FurnitureItem implements Serializable {
    private String TAG = "FurnitureItem";
    private static final long serialVersionUID = 1L;
    private String deviceId;
    private String deviceType; //"air_conditioner" 或 "light"
    private String workingStatus;
    private String status = "00000000";
    private String time;
    private int imageResource;
    private float acTemp; //空调温度
    private String switchStatus; //八位长的字符串, 类似00000000, 代表家具的功能开关状态
    private float lightPercent;
    private String deviceGroup; // 新增设备组字段

    public FurnitureDataPack getFurnitureDataPack() {
        return furnitureDataPack;
    }

    private FurnitureDataPack furnitureDataPack;


    public String sendDataJSON(){
        updateDataPack();
        if (furnitureDataPack == null) {
            return null;
        }
        com.google.gson.Gson gson = new com.google.gson.Gson();
        return gson.toJson(furnitureDataPack);
    }

    //接收JSON数据包并更新自身
    public boolean receiveDataJson(String receivedDataJson) {
        if (receivedDataJson == null || receivedDataJson.isEmpty()) {
            return false;
        }

        try {
            Log.i(TAG, receivedDataJson);
            com.google.gson.Gson gson = new com.google.gson.Gson();
            furnitureDataPack = gson.fromJson(receivedDataJson, FurnitureDataPack.class);

            // 检查反序列化是否成功
            if (furnitureDataPack == null) {
                return false;
            }

            return updateSelfFromDataPack();
        } catch (Exception e) {
            // 处理JSON解析异常
            e.printStackTrace();
            return false;
        }
    }

    //通过数据包更新自身的数据
    public boolean updateSelfFromDataPack(){
        if(furnitureDataPack == null){
            return false;
        }

        List<Integer> receivedStatus = furnitureDataPack.getStatus();
        if (receivedStatus == null) {
            return false;
        }

        // 重新构建switchStatus而不是追加
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < receivedStatus.size(); i++){
            sb.append(receivedStatus.get(i) == 1 ? "1" : "0");
        }
        switchStatus = sb.toString();
        if(switchStatus.charAt(0) == '1') {
            workingStatus = "工作中";
        } else {
            workingStatus = "关机";
        }

        // 检查params列表是否存在且不为空
        List<Float> params = furnitureDataPack.getParams();
        if (params == null || params.isEmpty()) {
            return true; // 只更新status部分
        }

        // 根据设备类型更新相应参数
        try {
            switch (deviceType){
                case "air_conditioner":
                    if (params.size() > 0) {
                        acTemp = params.get(0);
                        status = "温度 " + acTemp;
                    }
                    imageResource = R.drawable.icon_air_conditioner;
                    break;
                case "light":
                    if (params.size() > 0) {
                        lightPercent = params.get(0);

                    }
                    imageResource = R.drawable.icon_light;
                    break;
            }
        } catch (IndexOutOfBoundsException e) {
            // 处理索引越界异常
            e.printStackTrace();
            return false;
        }

        return true;
    }

    //通过自身的数据更新数据包
    public void updateDataPack() {
        // 确保furnitureDataPack对象存在
        if (furnitureDataPack == null) {
            furnitureDataPack = new FurnitureDataPack();
        }

        // 清空现有参数列表，避免重复添加
        furnitureDataPack.getParams().clear();

        // 将switchStatus的前4位分成4个布尔值并加入FurnitureDataPack的status中
        List<Integer> statusList = new ArrayList<>();
        if (switchStatus != null && switchStatus.length() >= 4) {
            for (int i = 0; i < 4; i++) {
                char c = switchStatus.charAt(i);
                statusList.add(c == '1' ? 1 : 0);
            }
        } else {
            // 如果switchStatus为空或长度不足，添加默认值
            for (int i = 0; i < 4; i++) {
                statusList.add(0);
            }
        }

        // 根据设备类型添加相应参数
        switch (deviceType){
            case "air_conditioner":
                furnitureDataPack.getParams().add(acTemp);
                break;
            case "light":
                furnitureDataPack.getParams().add(lightPercent);
                break;
        }

        // 更新furnitureDataPack的状态列表
        furnitureDataPack.setStatus(statusList);
    }


    public float getAcTemp() {
        return acTemp;
    }

    public void setAcTemp(float acTemp) {
        this.acTemp = acTemp;
    }

    public String getSwitchStatus() {
        return switchStatus;
    }

    public void setSwitchStatus(String acStatus) {
        this.switchStatus = acStatus;
    }

    public float getLightPercent() {
        return lightPercent;
    }

    public void setLightPercent(float lightPercent) {
        this.lightPercent = lightPercent;
    }

    public FurnitureItem() {}
    public FurnitureItem(String name, String deviceType, String workingStatus, String status, String time, int imageResource,
                         float acTemp, String switchStatus, float lightPercent) {
        this.deviceId = name;
        this.deviceType = deviceType;
        this.workingStatus = workingStatus;
        this.status = status;
        this.time = time;
        this.imageResource = imageResource;
        this.acTemp = acTemp;
        this.switchStatus = switchStatus;
        this.lightPercent = lightPercent;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getWorkingStatus() {
        return workingStatus;
    }

    public void setWorkingStatus(String workingStatus) {
        this.workingStatus = workingStatus;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public int getImageResource() {
        return imageResource;
    }

    public void setImageResource(int imageResource) {
        this.imageResource = imageResource;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    public String getDeviceGroup() {
        return deviceGroup;
    }

    public void setDeviceGroup(String deviceGroup) {
        this.deviceGroup = deviceGroup;
    }
}
