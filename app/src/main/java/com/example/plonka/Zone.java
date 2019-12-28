package com.example.plonka;

import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class Zone implements Serializable {
    private int identifier;
    private String description;
    private transient LatLng[] coordinates;
    private float balance;

    public Zone(String zone_id, String zone_name, String coordinateString, String balanceString){
        identifier = genIdentifier(zone_id);
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

    private int genIdentifier(String identifierStr){
        return Integer.parseInt(identifierStr);
    }

    public LatLng[] getCoords(){
        return coordinates;
    }

    public String getDescription(){
        return description;
    }

    public int getIdentifier() {return identifier;}

    public float getBalance() {return balance;}

    // https://stackoverflow.com/questions/14220554/how-to-serialize-a-third-party-non-serializable-final-class-e-g-googles-latln
    private void writeObject(ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
        out.writeInt(coordinates.length);
        for (int i = 0; i < coordinates.length; i++){
            out.writeDouble(coordinates[i].latitude);
            out.writeDouble(coordinates[i].longitude);
        }
    }

    // https://stackoverflow.com/questions/14220554/how-to-serialize-a-third-party-non-serializable-final-class-e-g-googles-latln
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        int numLocations = in.readInt();
        coordinates = new LatLng[numLocations];
        for (int i = 0; i < numLocations; i++){
            coordinates[i] = new LatLng(in.readDouble(), in.readDouble());
        }
    }
}
