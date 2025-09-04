package com.SmartHome.SmartHomeDemo.fragments.HomeFragment;

import java.util.ArrayList;
import java.util.List;

public class FurnitureDataPack {
    @Override
    public String toString() {
        return "FurnitureDataPack{" +
                "status=" + status.toString() +
                ", params=" + params.toString() +
                '}';
    }

    private List<Integer> status;
    private List<Float> params;

    public FurnitureDataPack() {
        status = new ArrayList<>();
        params = new ArrayList<>();
    }

    public FurnitureDataPack(List<Integer> status, List<Float> params) {
        this.status = status;
        this.params = params;
    }

    public List<Integer> getStatus() {
        return status;
    }

    public void setStatus(List<Integer> status) {
        this.status = status;
    }

    public List<Float> getParams() {
        return params;
    }

    public void setParams(List<Float> params) {
        this.params = params;
    }

}


