package idl.SmartDemo02;


public class Command{
    public String deviceId = "";// @ID(0)
    public String deviceType = "";// @ID(1)
    public short action = 0;// @ID(2)
    public float value = 0;// @ID(3)
    public boolean autoMode = false;// @ID(4)
    public int timestamp_ms = 0;// @ID(5)

    public Command(){

    }

    public Command(Command other){
        this();
        copy(other);
    }

    public Object copy(Object src) {
        Command typedSrc = (Command)src;
        this.deviceId =  typedSrc.deviceId;
        this.deviceType =  typedSrc.deviceType;
        this.action =  typedSrc.action;
        this.value =  typedSrc.value;
        this.autoMode =  typedSrc.autoMode;
        this.timestamp_ms =  typedSrc.timestamp_ms;
        return this;
    }
}