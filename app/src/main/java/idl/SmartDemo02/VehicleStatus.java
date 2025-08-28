package idl.SmartDemo02;


public class VehicleStatus{
    public boolean engineOn = false;// @ID(0)
    public boolean doorsLocked = false;// @ID(1)
    public float fuelPercent = 0;// @ID(2)
    public String location = "";// @ID(3)
    public int timestamp_ms = 0;// @ID(4)

    public VehicleStatus(){

    }

    public VehicleStatus(VehicleStatus other){
        this();
        copy(other);
    }

    public Object copy(Object src) {
        VehicleStatus typedSrc = (VehicleStatus)src;
        this.engineOn =  typedSrc.engineOn;
        this.doorsLocked =  typedSrc.doorsLocked;
        this.fuelPercent =  typedSrc.fuelPercent;
        this.location =  typedSrc.location;
        this.timestamp_ms =  typedSrc.timestamp_ms;
        return this;
    }
}