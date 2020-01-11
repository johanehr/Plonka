package com.example.plonka.ui;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.plonka.PolyUtil;
import com.example.plonka.R;
import com.example.plonka.SessionLog;
import com.example.plonka.Zone;
import com.example.plonka.data.model.LoggedInUser;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;

/**
 * ShiftActivity is used when currently working a shift, and logs the user's location continuously (screen kept on)
 */
public class ShiftActivity extends FragmentActivity implements OnMapReadyCallback, EndShiftDialogFragment.EndShiftDialogListener {

    private GoogleMap mMap;
    private UiSettings mUiSettings;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    private LatLng userLocation;
    private ArrayList<Zone> shiftZones = new ArrayList<Zone>(); // All current shift zones, passed with activity intent
    private Button stopWorkingButton;
    private FloatingActionButton fabButton;
    private boolean outsideZone = true; // Assumed to always be true when starting activity
    private Long leftZoneTimestamp; // Timestamp for when user left zone
    private LoggedInUser currentUser; // Contains necessary data for requesting data from DB
    private Vibrator vibrator;
    private File logFile;
    private ArrayList<SessionLog> sessionLogList = new ArrayList<>();

    private static final String LOG_TAG = "PLONKA_SHIFT_ACTIVITY";
    private static final String logFileName = "session.log";
    private static final int REQUEST_LOCATION_PERMISSIONS = 111;
    private static final String[] permissions = {Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION};
    private static final Long leftZoneMaximum = 120000L; //ms -> 120s -> 2 min

    /**
     * Sets up the UI provided that the required permissions are granted
     * @param savedInstanceState not used
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Instead of using a background service, just keep the screen on for now...
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // Retain user information, included in activity intent extras
        Integer userId = getIntent().getExtras().getInt("userId");
        String userPw = getIntent().getExtras().getString("userPw");
        String userName = getIntent().getExtras().getString("userName");
        currentUser = new LoggedInUser(userId, userPw, userName);
        Log.i(LOG_TAG, "LoggedInUser: " + userId + " - " + userPw + " - " + userName);

        // Retain knowledge of which zones to use for this shift
        Bundle zoneBundle = getIntent().getBundleExtra("zoneBundle");
        shiftZones = (ArrayList<Zone>) zoneBundle.getSerializable("zones");

        setContentView(R.layout.activity_shift);

        // Check that user has agreed to location permissions - if not, activity is finished
        if (!checkPermissions(permissions)) {
            Log.d(LOG_TAG, " > Requesting permissions");
            ActivityCompat.requestPermissions(this, permissions, REQUEST_LOCATION_PERMISSIONS);
        } else {
            Log.d(LOG_TAG, " > Permissions already granted.");
            initializeComponents();
        }
    }

    /**
     * Initializes the UI for the shift activity and sets up necessary files, location services, etc
     * @return void
     */
    public void initializeComponents() {

        Log.d(LOG_TAG, "called initializeComponents()");

        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);

        // Set up file for logging session information
        Context context = getApplicationContext();
        logFile = new File(context.getFilesDir(), logFileName);
        if(!logFile.exists()) {
            Log.i(LOG_TAG, "Session log file does not exist. Creating...");
            try {
                FileOutputStream outputStream = openFileOutput(logFileName, Context.MODE_PRIVATE);
                outputStream.close();
                Log.i(LOG_TAG, "Created empty file: "+logFileName);
            } catch (Exception e){
                e.printStackTrace();
            }
        }
        else {
            sessionLogList = readLogFile(); // Possible to continue on existing file, i.e. if app crashes before ending shift properly (which clears file).
        }

        // Set up periodic GPS-location request
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        locationRequest = LocationRequest.create();
        locationRequest.setInterval(10000); // Every 10s
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        locationCallback = new LocationCallback() {
            /**
             * Receives and logs the location result and checks if user is currently in the shift's related zone
             * @param locationResult result from location request
             */
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    Log.w(LOG_TAG, "LocationCallback(): locationResult == null");
                    return;
                }

                Location lastLocation = locationResult.getLastLocation();

                double latitude = lastLocation.getLatitude();
                double longitude = lastLocation.getLongitude();
                double accuracy = lastLocation.getAccuracy();

                userLocation = new LatLng(latitude, longitude);
                Long currentTimestamp = System.currentTimeMillis();

                String gpsData = "\nlat:" + latitude + "\nlong:" + longitude + "\naccuracy: " + accuracy + "m\ntimestamp: " + currentTimestamp;
                Log.d(LOG_TAG, "onLocationResult() called: " + gpsData);

                if (!checkInsideZone()) { // Check current status, but don't update outsideZone yet!
                    if (!outsideZone) {
                        // Transition to outside zone state (countdown to automatic termination of shift) if not already outside
                        leftZoneTimestamp = System.currentTimeMillis();
                        outsideZone = true;
                        Log.e(LOG_TAG, "User has left the designated zone area.");

                        // Alert user visually and with vibration
                        Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), "Warning! You have left the zone. Return within 2 minutes.", Snackbar.LENGTH_LONG);
                        snackbar.show();

                        // Something like this:       -   du   -   Du!
                        long[] timings   = new long[]{0, 200, 200, 200}; // ms
                        int[] amplitudes = new int[] {0, 128,   0, 200}; // 0-255
                        vibrator.vibrate(VibrationEffect.createWaveform(timings,amplitudes, -1));
                    }

                    if (currentTimestamp - leftZoneTimestamp > leftZoneMaximum) {
                        Log.e(LOG_TAG, "User has been outside of the designated zone area for too long. Terminating shift.");
                        Toast.makeText(getApplicationContext(), "You left the zone for more than 2 minutes - session terminated.", Toast.LENGTH_LONG).show();
                        endShift();
                    }
                } else if (outsideZone) {  // Outside -> inside: i.e. user has re-entered zone, transition to safe state
                    outsideZone = false;
                    Log.e(LOG_TAG, "User has re-entered the designated zone area.");
                }

                // Log path of user
                sessionLogList.add(new SessionLog(currentTimestamp, userLocation, !outsideZone));
            }
        };

        Log.d(LOG_TAG, " > setup fusedLocationClient");

        // Request periodic location updates
        startLocationUpdates();

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // Set up stopWorkingButton
        stopWorkingButton = findViewById(R.id.stopWorkingButton);
        stopWorkingButton.setOnClickListener(new View.OnClickListener() {
            /**
             * Vibration effect added upon clicking stop working button, and shows end shift dialog
             * @param v button with listener
             */
            @Override
            public void onClick(View v) {
                Log.d(LOG_TAG, "Pressed stop working button!");
                vibrator.vibrate(VibrationEffect.createOneShot(30, 30));
                showEndShiftDialog();
            }
        });

        // Set up myLocationButton
        fabButton = findViewById(R.id.myLocationButton);
        fabButton.setOnClickListener(new View.OnClickListener() {
            /**
             * Vibration effect added upon clicking fab button, and moves camera to current location on map
             * @param v button with listener
             */
            @Override
            public void onClick(View v) {
                Log.d(LOG_TAG, "Pressed FAB!");
                if (userLocation != null){
                    CameraPosition cameraPosition = new CameraPosition.Builder()
                            .target(userLocation)       // Sets the center of the map to Mountain View
                            .zoom(16)                   // Sets the zoom
                            .build();                   // Creates a CameraPosition from the builder
                    mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition), 250, null); // Animation takes 250ms
                    Log.d(LOG_TAG, "> Moved to current user location.");
                }
                else {
                    Log.w(LOG_TAG, " > Couldn't move to current user location, userLocation == null");
                    Toast.makeText(getApplicationContext(), "Unknown location... Please try again.", Toast.LENGTH_SHORT).show();
                }
                vibrator.vibrate(VibrationEffect.createOneShot(30, 30));
            }
        });
        Log.d(LOG_TAG, " > setup myLocationButton");

        // Add pulsing animation to "Tracking your work" notice
        TextView reminder = (TextView) findViewById(R.id.reminder);
        ObjectAnimator scaleDown = ObjectAnimator.ofPropertyValuesHolder(
                reminder,
                PropertyValuesHolder.ofFloat("scaleX", 1.1f),
                PropertyValuesHolder.ofFloat("scaleY", 1.1f));
        scaleDown.setDuration(800);
        scaleDown.setRepeatCount(ObjectAnimator.INFINITE);
        scaleDown.setRepeatMode(ObjectAnimator.REVERSE);
        scaleDown.start();
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     *
     * Sets up the map UI
     *
     * @param googleMap the map used when callback made
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d(LOG_TAG, "called onMapReady()");
        mMap = googleMap;

        // Set up UI for map
        mMap.setMyLocationEnabled(true);
        mMap.setBuildingsEnabled(true);
        mUiSettings = mMap.getUiSettings(); // https://developers.google.com/maps/documentation/android-sdk/controls
        mUiSettings.setMyLocationButtonEnabled(false);
        mUiSettings.setCompassEnabled(true);
        mUiSettings.setTiltGesturesEnabled(true);
        mUiSettings.setRotateGesturesEnabled(true);
        mUiSettings.setScrollGesturesEnabled(true);
        mUiSettings.setZoomGesturesEnabled(true);


        // Move the camera to last known user location
        fusedLocationClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() { // this -> getActivity()??
            @Override
            public void onSuccess(Location location) {
                // Got last known location. In some rare situations this can be null.
                if (location != null) {
                    double latitude = location.getLatitude();
                    double longitude = location.getLongitude();
                    LatLng startingLocation = new LatLng(latitude, longitude);

                    CameraPosition cameraPosition = new CameraPosition.Builder()
                            .target(startingLocation)   // Sets the center of the map
                            .zoom(16)                   // Sets the zoom
                            .build();                   // Creates a CameraPosition from the builder
                    mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition), 250, null);
                    Log.d(LOG_TAG, " > moved map to last known location");
                }
            }
        });
        addZoneHolesToMap(shiftZones);
    }

    /**
     * Starts location updates
     * Inspired by: https://developer.android.com/training/location/receive-location-updates
     * @return void
     */
    private void startLocationUpdates() {
        Log.d(LOG_TAG, "startLocationUpdates() called");
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
    }

    /**
     * When activity is resumed, location updates are also resumed
     * @return void
     */
    @Override
    public void onResume() {
        Log.d(LOG_TAG, "called onResume()");
        super.onResume();
        if (checkPermissions()) {
            startLocationUpdates();
        }
    }

    /**
     * When activity is paused, location updates are also paused
     * @return void
     */
    @Override
    public void onPause() {
        Log.d(LOG_TAG, "called onPause()");
        super.onPause();
        if (checkPermissions()) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
        }
    }

    /**
     * Callback function when requesting permissions, to verify that user allows location recording behavior.
     *
     * @param requestCode  the request code used when triggering callback
     * @param permissions  array containing the permissions that were requested
     * @param grantResults array containing the permissions that were granted
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.d(LOG_TAG, "onRequestPermissionsResult() called");
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        boolean permissionsAccepted = false;
        switch (requestCode) {
            case REQUEST_LOCATION_PERMISSIONS:
                permissionsAccepted = (grantResults[0] == PackageManager.PERMISSION_GRANTED) && (grantResults[1] == PackageManager.PERMISSION_GRANTED);
        }
        Log.d(LOG_TAG, "onRequestPermissionsResult(): " + permissionsAccepted);
        if (!permissionsAccepted) {
            // User is required to allow location tracking - display error message
            Toast.makeText(this, getString(R.string.require_location_permissions), Toast.LENGTH_LONG).show();
            finish();
        } else {
            initializeComponents();
        }
    }

    /**
     * Checks whether the required permissions have been granted by the user
     * @param permissions permissions to check
     * @return boolean, whether permissions granted
     */
    public boolean checkPermissions(String... permissions) {
        if (permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Add holes in the map overlay, to show where the user is permitted to be
     * Note: Shading does not work well with overlapping holes - but outlines still show up and regular logic works
     * @param activeZones the zones to include as holes
     * @return void
     */
    private void addZoneHolesToMap(ArrayList<Zone> activeZones) {
        final float delta = 0.0001f;
        LatLng[] wholeWorld = new LatLng[9];
        wholeWorld[0] = new LatLng(90 - delta, -180 + delta);
        wholeWorld[1] = new LatLng(0, -180 + delta);
        wholeWorld[2] = new LatLng(-90 + delta, -180 + delta);
        wholeWorld[3] = new LatLng(-90 + delta, 0);
        wholeWorld[4] = new LatLng(-90 + delta, 180 - delta);
        wholeWorld[5] = new LatLng(0, 180 - delta);
        wholeWorld[6] = new LatLng(90 - delta, 180 - delta);
        wholeWorld[7] = new LatLng(90 - delta, 0);
        wholeWorld[8] = new LatLng(90 - delta, -180 + delta);

        PolygonOptions polyOpts = new PolygonOptions().clickable(true).add(wholeWorld);

        for (Zone z : activeZones) {
            LatLng[] coords = z.getCoords();
            ArrayList<LatLng> coordsIterable = new ArrayList<LatLng>();
            for (int c = 0; c < coords.length; c++) {
                coordsIterable.add(coords[c]);
            }
            polyOpts.addHole(coordsIterable);
        }

        // Store a data object with the polygon, used here to indicate an arbitrary type.
        Polygon polygon1 = mMap.addPolygon(polyOpts);
        polygon1.setTag("A");
        polygon1.setStrokeWidth(6); // px width of stroke
        polygon1.setStrokeColor(0xffff8800); // Opaque orange
        polygon1.setFillColor(0x88222222); // Transparent dark grey defined as ARGB
    }

    /**
     * When user presses back button, don't leave activity, but rather prompt user for confirmation
     * @return void
     */
    @Override
    public void onBackPressed() {
        showEndShiftDialog();
    }

    /**
     * Show a dialog prompting user whether to end the shift.
     * See onDialogPositive/NegativeClick for resulting behavior.
     * @return void
     */
    private void showEndShiftDialog() {
        Log.i(LOG_TAG, "showEndShiftDialog() called");
        EndShiftDialogFragment newFragment = new EndShiftDialogFragment();
        newFragment.show(getSupportFragmentManager(), "endShift");
    }

    /**
     * The dialog fragment receives a reference to this Activity through the
     * Fragment.onAttach() callback, which it uses to call the following methods
     * defined by the EndShiftDialogFragment.EndShiftDialogListener interface
     *
     * Ends the shift if user confirms prompt
     *
     * @param dialog DialogFragment that invoked callback
     */
    @Override
    public void onDialogPositiveClick(DialogFragment dialog) {
        // User touched the dialog's positive button
        Log.d(LOG_TAG, "onDialogPositiveClick() called");
        endShift();
    }

    /**
     * The dialog fragment receives a reference to this Activity through the
     * Fragment.onAttach() callback, which it uses to call the following methods
     * defined by the EndShiftDialogFragment.EndShiftDialogListener interface
     *
     * Do nothing if user cancels prompt
     *
     * @param dialog DialogFragment that invoked callback
     */
    @Override
    public void onDialogNegativeClick(DialogFragment dialog) {
        // User touched the dialog's negative button, just cancels.
        Log.d(LOG_TAG, "onDialogNegativeClick() called");
    }

    /**
     * End the current shift by writing to the log file, so that EndShiftActivity can post shift information to database when transitioned to
     * @return void
     */
    private void endShift() {
        Log.i(LOG_TAG, "endShift() called");

        Toast.makeText(this, "endShift() called", Toast.LENGTH_LONG).show();
        if (writeLogFile())
        {
            Log.i(LOG_TAG, "Successfully wrote to log file.");
        }else{
            Log.e(LOG_TAG, "Failure when writing to log file!");
        }

        // Log in details passed on to new activity. TECHNICAL DEBT: use parcelable instead of multiple putExtra fields
        Intent endIntent = new Intent(getApplicationContext(), EndShiftActivity.class);
        endIntent.putExtra("userPw", currentUser.getPassword());
        endIntent.putExtra("userId",currentUser.getAccountId());
        endIntent.putExtra("userName", currentUser.getDisplayName());

        Bundle zoneBundle = new Bundle();
        zoneBundle.putSerializable("zones", (Serializable) shiftZones);
        endIntent.putExtra("zoneBundle", zoneBundle);

        startActivity(endIntent);
        finish(); // Won't return to this activity
    }

    /**
     * Check whether the user's location is within any of the current zones
     * @return boolean, whether user is in any of the current zones
     */
    private boolean checkInsideZone() {
        for (Zone z : shiftZones) {
            if (PolyUtil.containsLocation(userLocation, Arrays.asList(z.getCoords()), true)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Write to a log file when necessary, which can be parsed later
     * @return boolean, whether file was written to
     */
    public boolean writeLogFile(){
        try {
            Log.i(LOG_TAG, "Re-writing session log file:");
            FileOutputStream outputStream = openFileOutput(logFileName, Context.MODE_PRIVATE);
            for (SessionLog item : sessionLogList){
                outputStream.write(item.toLogLine(false).getBytes());
                Log.i(LOG_TAG, " - "+item.toLogLine(false));
            }
            outputStream.close();
            Log.i(LOG_TAG, "Wrote session log to file.");
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * Parses the log information from file if present.
     * @return ArrayList containing the shift information
     */
    public ArrayList<SessionLog> readLogFile(){
        ArrayList<SessionLog> sessionLogList = new ArrayList<>();
        try {
            FileInputStream inputStream = getApplicationContext().openFileInput(logFileName);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

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
}