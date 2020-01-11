package com.example.plonka;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Shift {
    private String zone_ids; // May be useful if work on app continues
    private String information; // EXTENSION: actually parsing this string fully, e.g. to show path on map
    private String status;
    private String timestamp;

    /**
     * Constructor for a work Shift object
     * @param zonesStr zones used for work shift
     * @param infoStr sessionLog information as a single string
     * @param statusStr shift status (paid, pending, etc)
     */
    public Shift(String zonesStr, String infoStr, String statusStr){
        zone_ids = zonesStr;
        information = infoStr;
        status = statusStr;
        timestamp = genTimestamp(information);
    }

    /**
     *  Generate a timestamp String from the first timestamp value in "information".
     *  Repeating format of infoStr: timestamp,latitude,longitude,status;... (i.e. sessionLog info)
     *  NOTE: Naively assuming data is always in correct format
     * @param infoStr the information string relating to a shift
     * @return String timestamp with format "dd-MM-yyyy HH:mm:ss"
     */
    private String genTimestamp(String infoStr){
        String[] firstSessionLogItem = infoStr.split(",");

        long millisTimestamp = Long.parseLong(firstSessionLogItem[0]);
        Date currentDate = new Date(millisTimestamp);
        DateFormat df = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        return df.format(currentDate);
    }

    /**
     * Getter for the Shift's status (pending, paid, etc)
     * @return String status string
     */
    public String getStatus() { return status;}

    /**
     * Getter for the Shift's timestamp
     * @return String timestamp
     */
    public String getTimestamp(){
        return timestamp;
    }
}
