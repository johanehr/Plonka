package com.example.plonka;

import android.os.AsyncTask;
import android.util.Log;

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
import java.util.HashMap;

// Good guide: https://medium.com/@suragch/android-asynctasks-99e501e637e5
public class AsyncLoginTask extends AsyncTask<String, Void, String[]> {

    // From: https://stackoverflow.com/questions/12575068/how-to-get-the-result-of-onpostexecute-to-main-activity-because-asynctask-is-a
    public interface AsyncResponse {
        void processFinish(String[] data);
    }

    // 1) Call PHP log-in script
    // 2) Return userId (db column: id), userName (db column: name) or errorMsg from PHP-script

    public AsyncResponse delegate = null;
    private String LOG_TAG = "PLONKA_ASYNC_LOGIN_TASK";
    String[] userData;

    // https://www.tutorialspoint.com/android/android_php_mysql.htm
    // https://androidjson.com/android-php-send-data-mysql-database/
    public AsyncLoginTask(AsyncResponse delegate){
        this.delegate = delegate;
    }

    @Override
    protected String[] doInBackground(String... argv) {
        Log.v(LOG_TAG, "Attempting to log in user by calling PHP-script... ");

        // Connect to db, get data
        HttpURLConnection conn = null;
        try {
            HashMap<String, String> params = new HashMap<>();
            params.put("provided_user", argv[0]);
            params.put("provided_pw", argv[1]);
            StringBuilder sbParams = new StringBuilder();
            int i = 0;
            for (String key : params.keySet()) {
                try {
                    if (i != 0){ // Only include ampersand if non-first key
                        sbParams.append("&");
                    }
                    sbParams.append(key).append("=").append(URLEncoder.encode(params.get(key), "UTF-8"));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                i++;
            }
            Log.v(LOG_TAG, "Built string: "+sbParams.toString());

            URL url = new URL("https://people.dsv.su.se/~joeh1022/scripts/login_user.php");
            conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setRequestMethod("POST");
            conn.setReadTimeout(2000);
            conn.setConnectTimeout(2000);
            conn.connect();

            // Add POST data
            DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
            wr.writeBytes(sbParams.toString());
            wr.flush();
            wr.close();

            Log.v(LOG_TAG, " > Connected to server");

            InputStream in = new BufferedInputStream(conn.getInputStream());
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));

            String firstLine = reader.readLine(); // Either a successful connection (with returned values following) or error message

            Boolean connectedToDb = false;
            if (firstLine != null){
                connectedToDb = firstLine.contains("Access granted");
                Log.v(LOG_TAG, " > Logged in: "+connectedToDb);
            }

            if (connectedToDb){
                // Naively assume known number of lines to read, all expected info returned
                String userId = reader.readLine();
                String userName = reader.readLine();

                // Remove "Name:", etc from beginning of strings
                userName = userName.substring(userName.indexOf(':')+1);
                userId = userId.substring(userId.indexOf(':')+1);
                userData = new String[]{userId, userName};

                // Log values, for manual testing
                Log.v(LOG_TAG, " > > Got userId: "+userId);
                Log.v(LOG_TAG, " > > Got userName: "+userName);
            } else {
                // Not connected, log error message
                Log.e(LOG_TAG, " > "+firstLine);
                userData = new String[]{firstLine};
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (conn != null) {
                conn.disconnect();
                Log.v(LOG_TAG, " > disconnected");
            }
        }
        return userData; // If only contains 1 String, then this is error msg. Else, actual data
    }

    @Override
    protected void onPostExecute(String[] data) {
        super.onPostExecute(data);
        delegate.processFinish(data);
    }
}
