package com.SmartHome.SmartHomeDemo.fragments.HomeFragment;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class FurnitureDataPack {
    @Override
    public String toString() {
        try{
            return "FurnitureDataPack{" +
                    "status=" + status.toString() +
                    ", params=" + params.toString() +
                    '}';
        }catch (NullPointerException e){
            Log.i("FurnitureDataPack", "NullPointerException");
        }
        return null;
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


