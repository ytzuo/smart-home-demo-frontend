package idl.SmartDemo;


public class Presence{
    public String source = "";// @ID(0)
    public boolean inRange = false;// @ID(1)
    public int distance_cm = 0;// @ID(2)
    public int timestamp_ms = 0;// @ID(3)

    public Presence(){

    }

    public Presence(Presence other){
        this();
        copy(other);
    }

    public Object copy(Object src) {
        Presence typedSrc = (Presence)src;
        this.source =  typedSrc.source;
        this.inRange =  typedSrc.inRange;
        this.distance_cm =  typedSrc.distance_cm;
        this.timestamp_ms =  typedSrc.timestamp_ms;
        return this;
    }
}