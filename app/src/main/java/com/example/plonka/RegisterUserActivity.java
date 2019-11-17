package com.example.plonka;

import android.os.AsyncTask;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

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



public class RegisterUserActivity extends AppCompatActivity {

    private String LOG_TAG = "PLONKA_REGISTER_USER";

    private EditText name;
    private EditText email;
    private EditText webpage;
    private EditText comment;

    private String str_name;
    private String str_email;
    private String str_webpage;
    private String str_comment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_user);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

    }
}
        /*

        // TODO: Get actual views
        name = findViewById(R.id.editTextName);
        email = findViewById(R.id.editTextEmail);
        webpage = findViewById(R.id.editTextWebpage);
        comment = findViewById(R.id.editTextComment);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(LOG_TAG, "User clicked fab");
                Snackbar.make(view, "Sending to database!", Snackbar.LENGTH_LONG).show();
                postToDb();
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    public void run() {
                        Log.i(LOG_TAG, "Returning to main activity");
                        finish();
                    }
                }, 3500);   //3.5 seconds, enough time to display a Snackbar.LENGTH_LONG before exiting activity
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    public void gatherContent() {
        str_name = name.getText().toString();
        str_email = email.getText().toString();
        str_webpage = webpage.getText().toString();
        str_comment = comment.getText().toString();

        Log.i(LOG_TAG, "Gathered the following message data:\n  - "
                +str_name+"\n  - "
                +str_email+"\n  - "
                +str_webpage+"\n  - "
                +str_comment);
    }

    // https://www.tutorialspoint.com/android/android_php_mysql.htm
    // https://androidjson.com/android-php-send-data-mysql-database/

    public void postToDb(){

        class sendPostReqAsyncTask extends AsyncTask<Void, Void, Boolean> {
            @Override
            protected Boolean doInBackground(Void... voids) {
                Log.i(LOG_TAG, "called doInBackground()");

                HashMap<String, String> params = new HashMap<>();;

                params.put("name", str_name);
                params.put("email", str_email);
                params.put("webpage", str_webpage);
                params.put("comment", str_comment);

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

                Log.i(LOG_TAG, "Built string: "+sbParams.toString());

                HttpURLConnection conn;
                String res = "";
                try {
                    URL url = new URL("https://people.dsv.su.se/~joeh1022/databaskoppling/databaskoppling_insert.php");
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

                } catch (IOException e) {
                    e.printStackTrace();
                    return false;
                }
                try {
                    InputStream in = new BufferedInputStream(conn.getInputStream());
                    BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                    StringBuilder result = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        result.append(line);
                    }
                    res = result.toString();

                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if (conn != null) {
                        conn.disconnect();
                    }

                    Log.d(LOG_TAG, "Post success: " + res.contains("success") + "\nResult from server: \n" + res);
                }

                return res.contains("success"); // If success is included in php output, SQL insert worked!
            }

            @Override
            protected void onPostExecute(Boolean res){
                super.onPostExecute(res);
                String msg;
                if (res){
                    msg = "httpPost COMPLETE";
                }
                else {
                    msg = "httpPost FAILED";
                    Toast.makeText(getApplicationContext(), "ERROR: Database insertion failed.", Toast.LENGTH_SHORT).show();
                }
                Log.i(LOG_TAG, msg);
            }
        }
        Log.i(LOG_TAG, "Called postToDb()");
        gatherContent();
        sendPostReqAsyncTask sendPostReqAsyncTask = new sendPostReqAsyncTask();
        sendPostReqAsyncTask.execute();
        Log.i(LOG_TAG, "Exit postToDb()");
    }
}
*/