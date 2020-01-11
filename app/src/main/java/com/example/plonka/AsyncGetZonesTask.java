package com.example.plonka;

import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * AsyncGetZonesTask is used to fetch available zones from an online database by calling a PHP-script
 * This guide was used as inspiration: https://medium.com/@suragch/android-asynctasks-99e501e637e5
 */
public class AsyncGetZonesTask extends AsyncTask<Void, Void, ArrayList<Zone>> {

    /**
     * Interface used to receive the response from async task
     * Inspired by: https://stackoverflow.com/questions/12575068/how-to-get-the-result-of-onpostexecute-to-main-activity-because-asynctask-is-a
     */
    public interface AsyncResponse {
        void processFinish(ArrayList<Zone> data);
    }

    // 1) Call get zones PHP-script
    // 2) Return ArrayList of Zone objects

    public AsyncResponse delegate = null;
    private String LOG_TAG = "PLONKA_ASYNC_GET_ZONES_TASK";
    ArrayList<Zone> zones = new ArrayList<>();

    // https://www.tutorialspoint.com/android/android_php_mysql.htm
    // https://androidjson.com/android-php-send-data-mysql-database/
    public AsyncGetZonesTask(AsyncResponse delegate){
        this.delegate = delegate;
    }

    /**
     * Async task of interacting with PHP-script and returning an ArrayList of zones from database
     * @param voids no input arguments
     * @return ArrayList<Zone> list of zones from DB
     */
    @Override
    protected ArrayList<Zone> doInBackground(Void... voids) {
        Log.d(LOG_TAG, "Attempting to get zones by calling PHP-script... ");

        // Connect to db, get data
        HttpURLConnection conn = null;
        try {
            URL url = new URL("https://people.dsv.su.se/~joeh1022/scripts/get_zones.php");
            conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setRequestMethod("POST");
            conn.setReadTimeout(2000);
            conn.setConnectTimeout(2000);
            conn.connect();

            DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
            wr.flush();
            wr.close();

            Log.d(LOG_TAG, " > Connected to server");

            InputStream in = new BufferedInputStream(conn.getInputStream());
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));

            String firstLine = reader.readLine(); // Either a successful connection (with returned zones following) or error message

            Boolean connectedToDb = false;
            if (firstLine != null){
                connectedToDb = firstLine.contains("NUM_ZONES");
                Log.d(LOG_TAG, " > Connection established!");
            }
            if (connectedToDb){
                Log.d(LOG_TAG, " > Connection established!");
                Integer numZones = Integer.parseInt(firstLine.substring(firstLine.indexOf(':')+1));
                Log.d(LOG_TAG, " > Receiving "+numZones+" zones from DB...");

                // Naively assume known number of lines to read, all expected info returned
                for (int zone = 0; zone < numZones; zone++) {
                    String separator = reader.readLine();    // ---
                    String identifier = reader.readLine();   // Id field from DB
                    String description = reader.readLine();  // Description field from DB
                    String coordinates = reader.readLine();  // Position field from DB
                    String balance = reader.readLine();      // Balance field from DB

                    zones.add(new Zone(identifier, description, coordinates, balance));
                }
            } else {
                // Not connected, log error message
                Log.e(LOG_TAG, " > "+firstLine);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (conn != null) {
                conn.disconnect();
                Log.d(LOG_TAG, " > disconnected");
            }
        }
        return zones;
    }

    /**
     * onPostExecute used to return results
     * @param data ArrayList of found zones
     * @return void
     */
    @Override
    protected void onPostExecute(ArrayList<Zone> data) {
        super.onPostExecute(data);
        delegate.processFinish(data);
    }
}
