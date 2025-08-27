package idl.SmartDemo;


public class Alert{
    public String source = "";// @ID(0)
    public String level = "";// @ID(1)
    public String description = "";// @ID(2)
    public int timestamp = 0;// @ID(3)

    public Alert(){

    }

    public Alert(Alert other){
        this();
        copy(other);
    }

    public Object copy(Object src) {
        Alert typedSrc = (Alert)src;
        this.source =  typedSrc.source;
        this.level =  typedSrc.level;
        this.description =  typedSrc.description;
        this.timestamp =  typedSrc.timestamp;
        return this;
    }
}