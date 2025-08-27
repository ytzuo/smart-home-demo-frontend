package idl.SmartDemo;


public class Command{
    public String device = "";// @ID(0)
    public short action = 0;// @ID(1)
    public float value = 0;// @ID(2)
    public boolean autoMode = false;// @ID(3)
    public int timestamp_ms = 0;// @ID(4)

    public Command(){

    }

    public Command(Command other){
        this();
        copy(other);
    }

    public Object copy(Object src) {
        Command typedSrc = (Command)src;
        this.device =  typedSrc.device;
        this.action =  typedSrc.action;
        this.value =  typedSrc.value;
        this.autoMode =  typedSrc.autoMode;
        this.timestamp_ms =  typedSrc.timestamp_ms;
        return this;
    }
}