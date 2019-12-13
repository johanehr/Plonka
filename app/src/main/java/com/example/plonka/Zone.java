package com.example.plonka;

import com.google.android.gms.maps.model.LatLng;

public class Zone {
    private String description;
    private LatLng[] coordinates;
    private float balance;

    public Zone(String zone_name, String coordinateString, String balanceString){
        description = zone_name;
        coordinates = genCoordinates(coordinateString);
        balance = genBalance(balanceString);
    }

    private LatLng[] genCoordinates(String coordStr){
        // Parse the string by splitting at semi-colons and commas, since data string is in format:
        // lat.latdecimals,long.longdecimals;lat.latdecimals,long.longdecimals;...
        // NOTE: naively assuming data is always in correct format
        String[] locations = coordStr.split(";");
        int numLocations = locations.length;
        LatLng[] coordinates = new LatLng[numLocations];

        for (int loc = 0; loc < numLocations; loc++){
            String[] latLngPair = locations[loc].split(",");
            double lat = Double.parseDouble(latLngPair[0]);
            double lng = Double.parseDouble(latLngPair[1]);
            coordinates[loc] = new LatLng(lat,lng);
        }
        return coordinates;
    }

    private float genBalance(String balanceStr){
        // EXTENSION: Make this handle non-ideal cases that would generate an Exception. Skipped for now.
        return Float.parseFloat(balanceStr);
    }

    public LatLng[] getCoords(){
        return coordinates;
    }

    public String getDescription(){
        return description;
    }
}
