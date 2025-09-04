package com.SmartHome.SmartHomeDemo.fragments.CarFragment;

public class CarItem {
    private String carName;
    private String engineStatus;
    private String fuel;
    private String location;
    private boolean lock;
    private boolean engineOn;
    private boolean acOn;

    public CarItem(String carName, String fuel, String location, boolean lock, boolean engineOn, boolean acOn) {
        this.carName = carName;
        this.fuel = fuel;
        this.location = location;
        this.lock = lock;
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

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getFuel() {
        return fuel;
    }

    public void setFuel(String fuel) {
        this.fuel = fuel;
    }

    public boolean isLock() {
        return lock;
    }

    public void setLock(boolean lock) {
        this.lock = lock;
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
