package com.example.plonka;

import android.content.Context;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
// import java.io.Serializable;
import java.util.ArrayList;

public class SessionLog { // implements Serializable {

    private Long timestamp;
    private transient LatLng coordinate;
    private boolean isInsideZone;

    public SessionLog(Long time, LatLng coord, boolean inside){
        timestamp = time;
        coordinate = coord;
        isInsideZone = inside;
    }

    // Outputs a String suitable for the log file/database in the format 12345...,10.123...,9.123...,true[;|\n]
    public String toLogLine(boolean forDatabase){
        String ending = "\n";
        if (forDatabase){
            ending = ";";
        }
        return timestamp.toString()+","+coordinate.latitude+","+coordinate.longitude+","+isInsideZone+ending;
    }

    public Long getTimestamp(){
        return timestamp;
    }

    public LatLng getCoordinate(){
        return coordinate;
    }

    public boolean getInside(){
        return isInsideZone;
    }
}
