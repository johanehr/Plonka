package com.example.plonka;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

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
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    private LatLng userLocation;
    private ArrayList<ArrayList<LatLng>> zoneLocations;
    private FloatingActionButton fabButton;
    private Button startWorkingButton;
    private UiSettings mUiSettings;
    private LoggedInUser currentUser; // Contains necessary data for requesting data from DB
    private String LOG_TAG = "PLONKA_MAP_ACTIVITY";

    private static final int REQUEST_LOCATION_PERMISSIONS = 111;
    private String [] permissions = {Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}; // TODO: , Manifest.permission.ACCESS_BACKGROUND_LOCATION};
    private boolean permissionsAccepted = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Check that user has agreed to location permissions - if not, activity is finished
        ActivityCompat.requestPermissions(this, permissions, REQUEST_LOCATION_PERMISSIONS);

        // Retain user information, included in activity intent extras
        Integer userId = getIntent().getExtras().getInt("userId");
        String userPw = getIntent().getExtras().getString("userPw");
        String userName = getIntent().getExtras().getString("userName");
        currentUser = new LoggedInUser(userId, userPw, userName);
        Log.i(LOG_TAG, "LoggedInUser: "+userId+" - "+userPw+" - "+userName);

        setContentView(R.layout.activity_maps);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapFragment);
        mapFragment.getMapAsync(this);

        fabButton = findViewById(R.id.myLocationButton);
        fabButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CameraPosition cameraPosition = new CameraPosition.Builder()
                        .target(userLocation)       // Sets the center of the map to Mountain View
                        .zoom(16)                   // Sets the zoom
                        .build();                   // Creates a CameraPosition from the builder
                mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition), 250, null); // Animation takes 250ms

                Log.e(LOG_TAG, "Pressed FAB! Moved to current user location.");
            }
        });

        startWorkingButton = findViewById(R.id.startWorkingButton);
        startWorkingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Log.e(LOG_TAG, "Pressed work button!");
                // TODO: Move to work activity
            }
        });

        // Set up location functionality
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        locationRequest = LocationRequest.create();
        locationRequest.setInterval(5000); // every 5s
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    Log.w(LOG_TAG, "LocationCallback(): locationResult == null");
                    return;
                }
                for (Location location : locationResult.getLocations()) { // TODO: Why is this a for loop? How more than one location?
                    double latitude = location.getLatitude();
                    double longitude = location.getLongitude();
                    double accuracy = location.getAccuracy();

                    userLocation = new LatLng(latitude, longitude);

                    String gpsData = "\nlat:" + latitude + "\nlong:" + longitude + "\naccuracy: " + accuracy + "m"; // https://developer.android.com/reference/android/location/Location.html
                    Log.i(LOG_TAG, "onLocationResult() called: "+gpsData);

                    // TODO: Check if within a zone. If yes, activate button to start working. Change text and transparency if not.
                    //startWorkingButton.setClickable(true);
                    //startWorkingButton.setEnabled(true);
                    //

                }
            }
        };

        // Start location request
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());

        // TODO: Gather zone data from database
        // zoneLocations = getZonesFromDb();
        // list/array/queue of LatLngs -> polygon using polyutil?
        // if (!zoneDataAddedToMap){ addToMap(); };

        // TODO: Add menu https://www.journaldev.com/9958/android-navigation-drawer-example-tutorial 

    }


    /**
     * Callback function when requesting permissions, to verify that user allows app recording behavior.
     *
     * @param  requestCode  the request code used when triggering callback
     * @param  permissions  array containing the permissions that were requested
     * @param  grantResults array containing the permissions that were granted
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case REQUEST_LOCATION_PERMISSIONS:
                // Check all three requests
                permissionsAccepted  = (grantResults[0] == PackageManager.PERMISSION_GRANTED) && (grantResults[1] == PackageManager.PERMISSION_GRANTED); // TODO: && (grantResults[2] == PackageManager.PERMISSION_GRANTED);
                break;
        }
        if (!permissionsAccepted ){
            // User is required to allow location tracking - display error message
            Toast.makeText(getApplicationContext(), getString(R.string.require_location_permissions), Toast.LENGTH_LONG).show();
            finish();
        }
    }



    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Set up UI for map
        mMap.setMyLocationEnabled(true);
        mMap.setBuildingsEnabled(true); // cool 3D buildings!
        mUiSettings = mMap.getUiSettings(); // https://developers.google.com/maps/documentation/android-sdk/controls
        mUiSettings.setMyLocationButtonEnabled(false);
        mUiSettings.setCompassEnabled(true);
        mUiSettings.setTiltGesturesEnabled(true);
        mUiSettings.setRotateGesturesEnabled(true);
        mUiSettings.setScrollGesturesEnabled(true);
        mUiSettings.setZoomGesturesEnabled(true);

        // Immediately move the camera to current user location
        fusedLocationClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                // Got last known location. In some rare situations this can be null.
                if (location != null) {
                    double latitude = location.getLatitude();
                    double longitude = location.getLongitude();
                    LatLng startingLocation = new LatLng(latitude, longitude);
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(startingLocation));
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (permissionsAccepted) {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        fusedLocationClient.removeLocationUpdates(locationCallback);
    }

}
