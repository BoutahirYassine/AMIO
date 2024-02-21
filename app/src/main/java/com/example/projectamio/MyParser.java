package com.example.projectamio;

import android.util.JsonReader;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class MyParser {
    List<Data> data_list;
    public MyParser() {
        data_list = new ArrayList();
    }

    public List<Data> readJsonStream(InputStream in) throws IOException {

        JsonReader reader = new JsonReader(new InputStreamReader(in, "UTF-8"));
        try {
            reader.beginObject();
            if (reader.hasNext()) {
                if (reader.nextName().equals("data")) {
                    reader.beginArray();
                    while (reader.hasNext()) {
                        data_list.add(readData(reader));
                    }
                    reader.endArray();
                }
            }
            reader.endObject();
            return data_list;
        } catch (IOException e) {
            Log.d("readJsonStream", "" + e);
            return null;
        } finally {
            reader.close();
        }
    }

    public Data readData(JsonReader reader) throws IOException{
        Long timestamp = null;
        String label = null;
        Double light_value = null;
        String mote = null;

        reader.beginObject();
        while(reader.hasNext()){
            String name = reader.nextName();
            if(name.equals("timestamp")){
                timestamp = reader.nextLong();
            }
            else if(name.equals("label")){
                label = reader.nextString();
            }
            else if(name.equals("value")){
                light_value = reader.nextDouble();
            }
            else if(name.equals("mote")){
                mote = reader.nextString();
            }
            else{
                reader.skipValue();
            }
        }
        reader.endObject();
        return new Data(timestamp,label,light_value,mote);
    }
}

