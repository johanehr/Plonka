package com.example.plonka;

import android.util.Log;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Shift {
    private String zone_ids;
    private String information; // EXTENSION: actually parsing this string, e.g. to show path on map
    private String status;
    private String timestamp;


    public Shift(String zonesStr, String infoStr, String statusStr){
        zone_ids = zonesStr;
        information = infoStr;
        status = statusStr;
        timestamp = genTimestamp(information);
    }

    // Generate a timestamp String from the first timestamp value in "information".
    private String genTimestamp(String infoStr){
        // Repeating format of infoStr: timestamp,latitude,longitude,status;...
        // NOTE: Naively assuming data is always in correct format

        String[] firstSessionLogItem = infoStr.split(",");

        long millisTimestamp = Long.parseLong(firstSessionLogItem[0]);
        Date currentDate = new Date(millisTimestamp);
        DateFormat df = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        return df.format(currentDate);
    }

    public String getStatus() { return status;}
    public String getTimestamp(){
        return timestamp;
    }
}
