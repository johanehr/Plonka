package com.example.plonka;

import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * Zone class represents an enclosed geographical area, within which a work shift can be conducted
 */
public class Zone implements Serializable {
    private int identifier;
    private String description;
    private transient LatLng[] coordinates;
    private float balance;

    /**
     * Constructor with unique database id, human-readable name, polygon vertex coordinates, and a monetary balance
     * @param zone_id unique id number
     * @param zone_name readable zone name
     * @param coordinateString string defining zone polygon vertices
     * @param balanceString remaining monetary balance for zone
     */
    public Zone(String zone_id, String zone_name, String coordinateString, String balanceString){
        identifier = genIdentifier(zone_id);
        description = zone_name;
        coordinates = genCoordinates(coordinateString);
        balance = genBalance(balanceString);
    }

    /**
     * Parses the coordinate string and creates corresponding latitude/longitude pairs.
     * Splits string at semi-colons and commas, since data string is in format:
     * lat.latdecimals,long.longdecimals;lat.latdecimals,long.longdecimals;...
     * NOTE: naively assuming data is always in correct format
     *
     * @param coordStr string to parse
     * @return LatLng[] containing coordinates
     */
    private LatLng[] genCoordinates(String coordStr){
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

    /**
     * Generate a float value from a string of zone balance
     * @param balanceStr
     * @return float balance
     */
    private float genBalance(String balanceStr){
        // EXTENSION: Make this handle non-ideal cases that would generate an Exception. Skipped for now.
        return Float.parseFloat(balanceStr);
    }

    /**
     * Generate an integer from a string of zone database key
     * @param identifierStr
     * @return int zone database key
     */
    private int genIdentifier(String identifierStr){
        return Integer.parseInt(identifierStr);
    }

    /**
     * Getter for zone's polygon vertex coordinates
     * @return LatLng[] of coordinates
     */
    public LatLng[] getCoords(){
        return coordinates;
    }

    /**
     * Getter for zone description/name, human-readable
     * @return String name
     */
    public String getDescription(){
        return description;
    }

    /**
     * Getter for zone database identifier key
     * @return int zone id
     */
    public int getIdentifier() {return identifier;}

    /**
     * Getter for zone monetary balance
     * @return float balance
     */
    public float getBalance() {return balance;}

    /**
     * Serialize the object, based on https://stackoverflow.com/questions/14220554/how-to-serialize-a-third-party-non-serializable-final-class-e-g-googles-latln
     * @param out stream to write to
     * @throws IOException
     */
    private void writeObject(ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
        out.writeInt(coordinates.length);
        for (int i = 0; i < coordinates.length; i++){
            out.writeDouble(coordinates[i].latitude);
            out.writeDouble(coordinates[i].longitude);
        }
    }

    /**
     * Read the serialized object, based on https://stackoverflow.com/questions/14220554/how-to-serialize-a-third-party-non-serializable-final-class-e-g-googles-latln
     * @param in stream to read from
     * @throws IOException
     * @throws ClassNotFoundException
     */
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        int numLocations = in.readInt();
        coordinates = new LatLng[numLocations];
        for (int i = 0; i < numLocations; i++){
            coordinates[i] = new LatLng(in.readDouble(), in.readDouble());
        }
    }
}
