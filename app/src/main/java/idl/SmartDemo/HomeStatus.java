package idl.SmartDemo;


public class HomeStatus{
    public boolean lightOn = false;// @ID(0)
    public float acTemp = 0;// @ID(1)
    public short cameraCount = 0;// @ID(2)
    public int timestamp_ms = 0;// @ID(3)

    public HomeStatus(){

    }

    public HomeStatus(HomeStatus other){
        this();
        copy(other);
    }

    public Object copy(Object src) {
        HomeStatus typedSrc = (HomeStatus)src;
        this.lightOn =  typedSrc.lightOn;
        this.acTemp =  typedSrc.acTemp;
        this.cameraCount =  typedSrc.cameraCount;
        this.timestamp_ms =  typedSrc.timestamp_ms;
        return this;
    }
}