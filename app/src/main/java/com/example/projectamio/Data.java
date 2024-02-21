package com.example.projectamio;

public class Data {
    public Long timestamp;
    public String label;
    public Double light;
    public String mote;
    public Data(Long timestamp,String label, Double light , String mote){
        this.timestamp=timestamp;
        this.label=label;
        this.light=light;
        this.mote=mote;
    }

    public Boolean getLightStatus() {
        return light >= 250;
    }

    public String toString(){
        return("data "+ label+" from "+mote+" acquired at "+timestamp+" : "+light);
    }
}
