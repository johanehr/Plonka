package com.example.plonka.ui;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.DialogFragment;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.plonka.R;
import com.example.plonka.SessionLog;
import com.example.plonka.Zone;
import com.example.plonka.data.model.LoggedInUser;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.maps.model.LatLng;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * EndShiftActivity is used when submitting a finished work shift by uploading collected data to database
 */
public class EndShiftActivity extends AppCompatActivity implements AbandonShiftDialogFragment.AbandonShiftDialogListener {

    private static final String LOG_TAG = "PLONKA_END_SHIFT_ACTIVITY";
    private static final int REQUEST_IMAGE_CAPTURE = 1337;  // The request code used to match an intent with the activity result callback
    private static final String logFileName = "session.log";

    private ArrayList<SessionLog> sessionLogList;
    private ArrayList<Zone> shiftZones;
    private ArrayList<Integer> zoneIds = new ArrayList<>();
    private LoggedInUser currentUser;
    private File image;

    private String currentPhotoPath;
    private String photoFileName;
    private String tempPhotoFileName;
    private String tempCurrentPhotoPath;

    private Button abandonButton;
    private Button submitPhotoButton;
    private ImageButton photoButton;
    private Vibrator vibrator;

    /**
     * Sets up the activity, with buttons for abandoning shift and submitting the collected data, as well as a photo intent button
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_end_shift);

        // Get user information (necessary for DB insertion)
        Integer userId = getIntent().getExtras().getInt("userId");
        String userPw = getIntent().getExtras().getString("userPw");
        String userName = getIntent().getExtras().getString("userName");
        currentUser = new LoggedInUser(userId, userPw, userName);
        Log.i(LOG_TAG, "LoggedInUser: " + userId + " - " + userPw + " - " + userName);

        // Retain knowledge of which zones were used for this shift
        Bundle zoneBundle = getIntent().getBundleExtra("zoneBundle");
        shiftZones = (ArrayList<Zone>) zoneBundle.getSerializable("zones");

        for (Zone z : shiftZones){
            zoneIds.add(z.getIdentifier());
        }
        Log.i(LOG_TAG, "Shift took place in zones with IDs: "+zoneIds.toString());

        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);

        sessionLogList = readLogFile();

        // Set up user interface
        abandonButton = findViewById(R.id.abandonButton);
        submitPhotoButton = findViewById(R.id.submitButton);
        photoButton = findViewById(R.id.photoButton);

        abandonButton.setOnClickListener(new View.OnClickListener() {
            /**
             * Vibration effect and abandon shift dialog shown
             * @param v button with listener
             */
            @Override
            public void onClick(View v) {
                Log.d(LOG_TAG, "Pressed abandon button!");
                vibrator.vibrate(VibrationEffect.createOneShot(30, 30));
                showAbandonShiftDialog();
            }
        });

        submitPhotoButton.setOnClickListener(new View.OnClickListener() {
            /**
             * Vibration effect and upload collected data to database
             * @param v button with listener
             */
            @Override
            public void onClick(View v) {
                Log.d(LOG_TAG, "Pressed submit button!");
                vibrator.vibrate(VibrationEffect.createOneShot(30, 30));
                uploadSessionToDb();
            }
        });

        photoButton.setOnClickListener(new ImageButton.OnClickListener() {
            /**
             * Launch the camera to submit a photo of collected litter by trash can
             * Based on example at: https://developer.android.com/training/camera/photobasics.html
             * @param v button with listener
             */
            public void onClick(View v) {
                Log.d(LOG_TAG, "Pressed photo button!");
                // Code here executes on main thread after user presses button
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                    try {
                        createImageFile(new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date()), currentUser.getAccountId());
                    } catch (IOException ex) {
                        Log.w(LOG_TAG, "Image file creation failed.");
                    }
                    // Continue only if the File was successfully created
                    if (image != null) {
                        Uri photoURI = FileProvider.getUriForFile(getApplicationContext(),
                                "com.example.android.fileprovider",
                                image);
                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                        startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                    }
                }
            }
        });
    }

    /**
     * Instead of ending activity, provide user with a prompt
     * @return void
     */
    @Override
    public void onBackPressed() {
        showAbandonShiftDialog();
    }

    /**
     * Show the abandon shift dialog
     * @return void
     */
    private void showAbandonShiftDialog() {
        Log.i(LOG_TAG, "showAbandonShiftDialog() called");
        AbandonShiftDialogFragment newFragment = new AbandonShiftDialogFragment();
        newFragment.show(getSupportFragmentManager(), "abandonShift");
    }

    /**
     * The dialog fragment receives a reference to this Activity through the
     * Fragment.onAttach() callback, which it uses to call the following methods
     * defined by the EndShiftDialogFragment.EndShiftDialogListener interface
     * Clears the log file and ends the activity
     * @param dialog dialogfragment which created call
     */
    @Override
    public void onDialogPositiveClick(DialogFragment dialog) {
        // User touched the dialog's positive button
        Log.d(LOG_TAG, "onDialogPositiveClick() called");
        clearLogFile(); // Can't continue on an actively abandoned session, so clear file
        finish();
    }

    /**
     * The dialog fragment receives a reference to this Activity through the
     * Fragment.onAttach() callback, which it uses to call the following methods
     * defined by the EndShiftDialogFragment.EndShiftDialogListener interface
     * Does nothing, simply avoids ending the activity, see onDialogPositiveClick
     * @param dialog dialogfragment which created call
     */
    @Override
    public void onDialogNegativeClick(DialogFragment dialog) {
        // User touched the dialog's negative button, just cancels.
        Log.d(LOG_TAG, "onDialogNegativeClick() called");
    }

    /**
     * Read information saved in the log file, in case session was cut short or returned to
     * @return contents of log file
     */
    private ArrayList<SessionLog> readLogFile(){
        ArrayList<SessionLog> sessionLogList = new ArrayList<>();
        try {
            FileInputStream inputStream = getApplicationContext().openFileInput(logFileName);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            Log.i(LOG_TAG, "Reading from session log file:");
            while(true){
                String line = reader.readLine();
                if (line != null){
                    String[] data = line.split(",");
                    Log.i(LOG_TAG, " - "+ Arrays.toString(data));
                    SessionLog item = new SessionLog(
                            Long.parseLong(data[0]),
                            new LatLng(Double.parseDouble(data[1]), Double.parseDouble(data[2])),
                            data[3].equals("true")
                    );
                    sessionLogList.add(item);
                }
                else{
                    Log.i(LOG_TAG, "Reached end of file");
                    break; // No more lines to read
                }
            }
        }
        catch (Exception e){
            Log.i(LOG_TAG, "Problem reading from current session log file...");
            e.printStackTrace();
        }
        return sessionLogList;
    }

    /**
     * Attempt to delete the log file
     * @return void
     */
    private void clearLogFile(){
        File file = new File(getApplicationContext().getFilesDir(), logFileName);
        if(file.delete()){
            Log.i(LOG_TAG, "Log file removed!");
        }
        else{
            Log.e(LOG_TAG, "Error removing log file!");
        }
    }

    /**
     * Upload image and session information to webserver, partially based on examples at:
     * https://stackoverflow.com/questions/34915556/post-a-file-with-other-form-data-using-httpurlconnection,
     * https://stackoverflow.com/questions/19026256/how-to-upload-multipart-form-data-and-image-to-server-in-android/26145565#26145565
     */
     private void uploadSessionToDb() {
         /**
          * Internal AsyncTask, since only used here that returns a result String
          */
         class AsyncUploadSessionTask extends AsyncTask<Void, Void, String> {
             /**
              * Body of async task, which uploads information to database if user can be authenticated
              * @param voids no input
              * @return result string
              */
            @Override
            protected String doInBackground(Void... voids) {
                Log.i(LOG_TAG, "called doInBackground()");

                HashMap<String, String> params = new HashMap<>();
                params.put("user_id", Integer.toString(currentUser.getAccountId()));
                params.put("psw", currentUser.getPassword());
                params.put("zone_ids", zoneIds.toString());
                params.put("location_info", generateSessionsStringForDb(sessionLogList));
                params.put("img_path", photoFileName);

                HttpURLConnection conn;
                String res = "";

                String twoHyphens = "--";
                String boundary = "*****" + Long.toString(System.currentTimeMillis()) + "*****";
                String lineEnd = "\r\n";
                int bytesRead, bytesAvailable, bufferSize;
                byte[] buffer;
                int maxBufferSize = 1 * 1024 * 1024;
                String[] q = currentPhotoPath.split("/");
                int idx = q.length - 1;

                try {
                    File photoFile = new File(currentPhotoPath);
                    FileInputStream fileInputStream = new FileInputStream(photoFile);

                    URL url = new URL("https://people.dsv.su.se/~joeh1022/scripts/upload_shift.php");
                    conn = (HttpURLConnection) url.openConnection();
                    conn.setDoOutput(true);
                    conn.setDoInput(true);
                    conn.setUseCaches(false);
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Connection", "Keep-Alive");
                    conn.setRequestProperty("User-Agent", "Android Multipart HTTP Client 1.0");
                    conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
                    conn.setReadTimeout(2000);
                    conn.setConnectTimeout(2000);

                    conn.connect();

                    DataOutputStream wr = new DataOutputStream(conn.getOutputStream());

                    // Upload image
                    wr.writeBytes(twoHyphens + boundary + lineEnd);
                    wr.writeBytes("Content-Disposition: form-data; name=\"file\"; filename=\"" + q[idx] + "\"" + lineEnd);
                    wr.writeBytes("Content-Type: image/jpeg" + lineEnd);
                    wr.writeBytes("Content-Transfer-Encoding: binary" + lineEnd);
                    wr.writeBytes(lineEnd);
                    bytesAvailable = fileInputStream.available();
                    bufferSize = Math.min(bytesAvailable, maxBufferSize);
                    buffer = new byte[bufferSize];
                    bytesRead = fileInputStream.read(buffer, 0, bufferSize);
                    while (bytesRead > 0) {
                        wr.write(buffer, 0, bufferSize);
                        bytesAvailable = fileInputStream.available();
                        bufferSize = Math.min(bytesAvailable, maxBufferSize);
                        bytesRead = fileInputStream.read(buffer, 0, bufferSize);
                    }
                    wr.writeBytes(lineEnd);

                    // Upload POST data
                    Iterator<String> keys = params.keySet().iterator();
                    while (keys.hasNext()) {
                        String key = keys.next();
                        String value = params.get(key);

                        wr.writeBytes(twoHyphens + boundary + lineEnd);
                        wr.writeBytes("Content-Disposition: form-data; name=\"" + key + "\"" + lineEnd);
                        wr.writeBytes("Content-Type: text/plain" + lineEnd);
                        wr.writeBytes(lineEnd);
                        wr.writeBytes(value);
                        wr.writeBytes(lineEnd);
                    }

                    if (200 != conn.getResponseCode()) {
                        Log.e(LOG_TAG, "Failed to upload code:" + conn.getResponseCode() + " " + conn.getResponseMessage());
                    }

                    fileInputStream.close();
                    wr.flush();
                    wr.close();

                } catch (IOException e) {
                    e.printStackTrace();
                    return e.toString();
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
                    Log.d(LOG_TAG, "Submission success: " + res.contains("success") + "\nResult from server: \n" + res);
                }
                return res; // If success is included in php output, SQL insert worked!
            }

             /**
              * Used as part of AsyncTask override
              * @param res result
              */
            @Override
            protected void onPostExecute(String res) {
                super.onPostExecute(res);
            }
        }

        Log.i(LOG_TAG, "Called uploadSessionToDb()");
        AsyncUploadSessionTask uploadShiftTask = new AsyncUploadSessionTask();
        String uploadShiftOutput = "AsyncUploadSessionTask failed"; // Changed if successful
        try {
            uploadShiftOutput = uploadShiftTask.execute().get(); // Get async response on demand, make "synchronous"
        } catch (Exception e) {
            Log.e(LOG_TAG, "AsyncUploadSessionTask.execute() failed: " + e.toString());
        }

        Log.i(LOG_TAG, "Output: " + uploadShiftOutput);

        Toast.makeText(getApplicationContext(), uploadShiftOutput, Toast.LENGTH_LONG).show(); // Shows confirmation/error msg from server
        if (uploadShiftOutput.contains("Uploaded successfully!")) {
            // Send user back to hub activity if successfully submitted
            clearLogFile();
            image.delete(); // NOTE: This only deletes the final image taken. As an extension, the directory should be traversed and cleared of this type of image (but user can clear app storage manually if this is an issue)
            finish();
        }

        Log.i(LOG_TAG, "Exit uploadSessionToDb()");
    }

    /**
     * Create an image file name based on a provided timestamp and user id, used to match photo to database listing if photo is uploaded
     * @param timestamp timestamp
     * @param userId logged in user's unique id
     * @throws IOException
     */
    private void createImageFile(String timestamp, Integer userId) throws IOException {
        String imageFileName = Integer.toString(userId) + "_" + timestamp ;
        Log.i(LOG_TAG, "imageFileName: "+imageFileName);
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        image = new File(storageDir, imageFileName + ".jpg");

        // Save a file: path for use with ACTION_VIEW intents. Note that this is temporary until the user has actually saved an image.
        tempPhotoFileName = imageFileName + ".jpg";
        tempCurrentPhotoPath = image.getAbsolutePath();
        Log.i(LOG_TAG, "Image will be saved to path: "+tempCurrentPhotoPath);
    }

    /**
     * Fetches the image at currentPhotoPath and sets it as the ImageButton background
     *
     * @param  requestCode  Request identifier to filter out requests
     * @param  resultCode Used to verify whether the activity had an OK result
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            try {
                Log.d(LOG_TAG, "Image saved at specified path!");
                currentPhotoPath = tempCurrentPhotoPath; // No longer temporary if activity has RESULT_OK
                photoFileName = tempPhotoFileName;
                File file = new File(currentPhotoPath);
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getApplicationContext().getContentResolver(), Uri.fromFile(file));
                photoButton.setImageBitmap(bitmap);

                // Activate submit button now that a photo has been taken
                submitPhotoButton.setEnabled(true);
                submitPhotoButton.setClickable(true);
                submitPhotoButton.setBackground(ContextCompat.getDrawable(this, R.drawable.rounded_button));

            } catch (Exception error) {
                error.printStackTrace();
            }
        }
    }

    /**
     * Generate a single String to save in database on repeating format: <timestamp>,<latitude>,<longitude>,<isInsideZone? true/false>;
     * Split on ';' to get each SessionLog, and then on ',' to get individual values.
     * @param sessions list of session log items to be included in output string
     * @return long string to be saved in single field in database
     */
    private String generateSessionsStringForDb(ArrayList<SessionLog> sessions){
        String stringForDb = "";
        for (SessionLog s : sessions){
            stringForDb += s.toLogLine(true);
        }
        Log.i(LOG_TAG, "Generated sessions string for DB:\n"+stringForDb);
        return stringForDb;
    }
}
