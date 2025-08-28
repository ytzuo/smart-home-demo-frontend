package idl.SmartDemo02;


public class HomeStatus{
    public com.zrdds.infrastructure.StringSeq deviceIds = new com.zrdds.infrastructure.StringSeq();// @ID(0)
    public com.zrdds.infrastructure.StringSeq deviceTypes = new com.zrdds.infrastructure.StringSeq();// @ID(1)
    public com.zrdds.infrastructure.FloatSeq acTemp = new com.zrdds.infrastructure.FloatSeq();// @ID(2)
    public com.zrdds.infrastructure.BooleanSeq lightOn = new com.zrdds.infrastructure.BooleanSeq();// @ID(3)
    public com.zrdds.infrastructure.BooleanSeq cameraOn = new com.zrdds.infrastructure.BooleanSeq();// @ID(4)
    public int timestamp_ms = 0;// @ID(5)

    public HomeStatus(){

        this.deviceIds.maximum(255);
        this.deviceTypes.maximum(255);
        this.acTemp.maximum(255);
        this.lightOn.maximum(255);
        this.cameraOn.maximum(255);
    }

    public HomeStatus(HomeStatus other){
        this();
        copy(other);
    }

    public Object copy(Object src) {
        HomeStatus typedSrc = (HomeStatus)src;
        this.deviceIds.copy(typedSrc.deviceIds);
        this.deviceTypes.copy(typedSrc.deviceTypes);
        this.acTemp.copy(typedSrc.acTemp);
        this.lightOn.copy(typedSrc.lightOn);
        this.cameraOn.copy(typedSrc.cameraOn);
        this.timestamp_ms =  typedSrc.timestamp_ms;
        return this;
    }
}