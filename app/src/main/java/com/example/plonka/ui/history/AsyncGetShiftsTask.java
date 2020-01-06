package com.example.plonka.ui.history;

import android.os.AsyncTask;
import android.util.Log;

import com.example.plonka.Shift;
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

// Good guide: https://medium.com/@suragch/android-asynctasks-99e501e637e5
public class AsyncGetShiftsTask extends AsyncTask<String, Void, ArrayList<Shift>> {

    // From: https://stackoverflow.com/questions/12575068/how-to-get-the-result-of-onpostexecute-to-main-activity-because-asynctask-is-a
    public interface AsyncResponse {
        void processFinish(ArrayList<Shift> data);
    }

    // 1) Call get shifts PHP-script
    // 2) Return ArrayList of Shift objects

    public AsyncResponse delegate = null;
    private String LOG_TAG = "PLONKA_ASYNC_GET_SHIFTS_TASK";
    ArrayList<Shift> shifts = new ArrayList<>();

    // https://www.tutorialspoint.com/android/android_php_mysql.htm
    // https://androidjson.com/android-php-send-data-mysql-database/
    public AsyncGetShiftsTask(AsyncResponse delegate){
        this.delegate = delegate;
    }

    @Override
    protected ArrayList<Shift> doInBackground(String... args) {
        Log.d(LOG_TAG, "Attempting to get shifts by calling PHP-script... ");
        String userId = args[0];
        String userPw = args[1];
        Log.d(LOG_TAG, "Input: "+userId+", "+userPw);

        // Include user_id and password in order to authenticate before getting shifts from DB
        HashMap<String, String> params = new HashMap<>();
        params.put("user_id", userId);
        params.put("psw", userPw);

        StringBuilder sbParams = new StringBuilder();
        int i = 0;
        for (String key : params.keySet()) {
            try {
                if (i != 0) { // Only include ampersand if non-first key
                    sbParams.append("&");
                }
                sbParams.append(key).append("=").append(URLEncoder.encode(params.get(key), "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            i++;
        }

        // Connect to db, get data
        HttpURLConnection conn = null;
        try {
            URL url = new URL("https://people.dsv.su.se/~joeh1022/scripts/get_shifts.php");
            conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setRequestMethod("POST");
            conn.setReadTimeout(2000);
            conn.setConnectTimeout(2000);
            conn.connect();

            DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
            wr.writeBytes(sbParams.toString());
            wr.flush();
            wr.close();

            Log.d(LOG_TAG, " > Connected to server");

            InputStream in = new BufferedInputStream(conn.getInputStream());
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));

            String firstLine = reader.readLine(); // Either a successful connection (with returned zones following) or error message

            Boolean connectedToDb = false;
            if (firstLine != null){
                connectedToDb = firstLine.contains("NUM_SHIFTS");
            }
            if (connectedToDb){
                Log.d(LOG_TAG, " > Connection established!");
                Integer numShifts = Integer.parseInt(firstLine.substring(firstLine.indexOf(':')+1));
                Log.d(LOG_TAG, " > Receiving "+numShifts+" shifts from DB...");

                // Naively assume known number of lines to read, all expected info returned
                for (int shift = 0; shift < numShifts; shift++) {
                    Log.d(LOG_TAG, " << Reading shift number "+shift);
                    String sep = reader.readLine();     // ---
                    String zones = reader.readLine();   // zone_ids field from DB
                    String info = reader.readLine();    // information field from DB
                    String status = reader.readLine();  // status field from DB
                    Log.d(LOG_TAG, " >> Found shift with zones: "+zones+"\ninfo: "+info+"\nstatus: "+status);

                    shifts.add(new Shift(zones, info, status));
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
        return shifts;
    }

    @Override
    protected void onPostExecute(ArrayList<Shift> data) {
        super.onPostExecute(data);
        delegate.processFinish(data);
    }
}

