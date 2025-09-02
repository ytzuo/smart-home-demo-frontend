package com.SmartHome.SmartHomeDemo.fragments.HomeFragment;

import java.util.ArrayList;
import java.util.List;

public class FurnitureDataPack {
    private List<Boolean> status;
    private List<Float> params;

    public FurnitureDataPack() {
        status = new ArrayList<>();
        params = new ArrayList<>();
    }

    public FurnitureDataPack(List<Boolean> status, List<Float> params) {
        this.status = status;
        this.params = params;
    }

    public List<Boolean> getStatus() {
        return status;
    }

    public void setStatus(List<Boolean> status) {
        this.status = status;
    }

    public List<Float> getParams() {
        return params;
    }

    public void setParams(List<Float> params) {
        this.params = params;
    }

}


