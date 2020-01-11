package com.example.plonka;

import com.google.android.gms.maps.model.LatLng;

/**
 * SessionLog contains a single location point with associated information for the shift log
 */
public class SessionLog {

    private Long timestamp;
    private transient LatLng coordinate;
    private boolean isInsideZone;

    /**
     * Constructor with a timestamp, location, and whether the location is within a zone
     * @param time timestamp
     * @param coord location
     * @param inside whether user was inside the zone
     */
    public SessionLog(Long time, LatLng coord, boolean inside){
        timestamp = time;
        coordinate = coord;
        isInsideZone = inside;
    }

    /**
     * Outputs a String suitable for the log file/database in the format 12345...,10.123...,9.123...,true[;|\n]
     * Note: Remove need for separate database/log file option in future to simplify code
     * @param forDatabase whether the string should be formatted for use in the database or in the log file
     * @return
     */
    public String toLogLine(boolean forDatabase){
        String ending = "\n";
        if (forDatabase){
            ending = ";";
        }
        return timestamp.toString()+","+coordinate.latitude+","+coordinate.longitude+","+isInsideZone+ending;
    }

    /**
     * Getter for the log item's timestamp
     * @return Long timestamp
     */
    public Long getTimestamp(){
        return timestamp;
    }

    /**
     * Getter for the log item's location
     * @return LatLng coordinates
     */
    public LatLng getCoordinate(){
        return coordinate;
    }

    /**
     * Getter for the log item's zone status
     * @return boolean whether user was inside a zone
     */
    public boolean getInside(){
        return isInsideZone;
    }
}
