package com.example.projectamio;

public class Data {
    Long timestamp;
    String label;
    Double light;
    String mote;
    public Data(Long timestamp,String label, Double light , String mote){
        this.timestamp=timestamp;
        this.label=label;
        this.light=light;
        this.mote=mote;
    }
    public String toString(){
        return("data "+ label+" from "+mote+" acquired at "+timestamp+" : "+light);
    }
}
