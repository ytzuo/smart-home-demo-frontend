package com.SmartHome.SmartHomeDemo.fragments.CarFragment;

public class CarItem {
    private String carName;
    private String engineStatus;
    private String fuel;
    private String mile;
    private boolean lightOn;
    private boolean engineOn;
    private boolean acOn;

    public CarItem(String carName, String engineStatus, String fuel, String mile, boolean lightOn, boolean engineOn, boolean acOn) {
        this.carName = carName;
        this.engineStatus = engineStatus;
        this.fuel = fuel;
        this.mile = mile;
        this.lightOn = lightOn;
        this.engineOn = engineOn;
        this.acOn = acOn;
    }

    public String getCarName() {
        return carName;
    }

    public void setCarName(String carName) {
        this.carName = carName;
    }

    public String getEngineStatus() {
        return engineStatus;
    }

    public void setEngineStatus(String engineStatus) {
        this.engineStatus = engineStatus;
    }

    public String getMile() {
        return mile;
    }

    public void setMile(String mile) {
        this.mile = mile;
    }

    public String getFuel() {
        return fuel;
    }

    public void setFuel(String fuel) {
        this.fuel = fuel;
    }

    public boolean isLightOn() {
        return lightOn;
    }

    public void setLightOn(boolean lightOn) {
        this.lightOn = lightOn;
    }

    public boolean isEngineOn() {
        return engineOn;
    }

    public void setEngineOn(boolean engineOn) {
        this.engineOn = engineOn;
    }

    public boolean isAcOn() {
        return acOn;
    }

    public void setAcOn(boolean acOn) {
        this.acOn = acOn;
    }
}
