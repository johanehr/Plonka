package com.example.plonka.ui.map;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;

import com.example.plonka.AsyncGetZonesTask;
import com.example.plonka.Zone;
import com.example.plonka.PolyUtil;
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

import android.os.Looper;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import android.widget.Button;
import android.widget.Toast;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import com.example.plonka.R;

import java.util.ArrayList;
import java.util.Arrays;

public class MapFragment extends Fragment implements OnMapReadyCallback{

    private GoogleMap mMap;
    private UiSettings mUiSettings;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    private LatLng userLocation;
    private ArrayList<Zone> zones;
    private boolean insideZone = false;

    private View root;
    private FloatingActionButton fabButton;
    private Button startWorkingButton;

    private String LOG_TAG = "PLONKA_MAP_FRAGMENT";

    private static final int REQUEST_LOCATION_PERMISSIONS = 111;
    private String [] permissions = {Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION};

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        Log.d(LOG_TAG, "onCreateView() called");
        root = inflater.inflate(R.layout.fragment_map, container, false);

        // Check that user has agreed to location permissions - if not, activity is finished
        if (!checkPermissions(permissions)){
            Log.d(LOG_TAG, " > Requesting permissions");
            ActivityCompat.requestPermissions(getActivity(), permissions, REQUEST_LOCATION_PERMISSIONS);
        }
        else{
            Log.d(LOG_TAG, " > Permissions already granted.");
            initializeComponents();
        }
        return root;
    }

    public void initializeComponents(){

        // Set up periodic GPS-location request
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());
        locationRequest = LocationRequest.create();
        locationRequest.setInterval(2000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                Log.d(LOG_TAG, "called locationCallback()");
                if (locationResult == null) {
                    Log.w(LOG_TAG, "LocationCallback(): locationResult == null");
                    return;
                }

                Location lastLocation = locationResult.getLastLocation();

                double latitude = lastLocation.getLatitude();
                double longitude = lastLocation.getLongitude();
                double accuracy = lastLocation.getAccuracy();

                String gpsData = "\nlat:" + latitude + "\nlong:" + longitude + "\naccuracy: " + accuracy + "m";
                Log.d(LOG_TAG, "onLocationResult() called: " + gpsData);

                userLocation = new LatLng(latitude, longitude);

                // Check if within any zone. If yes, activate button and set text. If not, disable w/ other text.
                insideZone = false;
                for (Zone z : zones) {
                    if (PolyUtil.containsLocation(userLocation, Arrays.asList(z.getCoords()), true)){ // https://googlemaps.github.io/android-maps-utils/javadoc/com/google/maps/android/PolyUtil.html
                        insideZone = true;
                        // TODO: Add logic to select WHICH zone(s) have been entered
                        Log.i(LOG_TAG, "User has entered zone: "+z.getDescription() + " which has ID:"+z.getIdentifier());
                    }
                    else {
                        Log.i(LOG_TAG, "User is NOT in zone: "+z.getDescription() + " which has ID:"+z.getIdentifier());
                    }
                }

                startWorkingButton.setClickable(insideZone);
                startWorkingButton.setEnabled(insideZone);
                startWorkingButton.setBackground(ContextCompat.getDrawable(getContext(), insideZone? R.drawable.rounded_button : R.drawable.rounded_button_inactive));
                startWorkingButton.setText(getString( insideZone? R.string.button_start_working  : R.string.button_start_working_inactive));

            }
        };
        Log.d(LOG_TAG, " > setup fusedLocationClient");

        // Request periodic location updates
        startLocationUpdates();

        // Set up map
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.mapFragment);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
        else {
            Log.e(LOG_TAG, " > mapFragment == null: failure");
        }

        // Set up myLocationButton
        fabButton = root.findViewById(R.id.myLocationButton);
        fabButton.setOnClickListener(new View.OnClickListener() {
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
                    Toast.makeText(getActivity().getApplicationContext(), "Unknown location... Please try again.", Toast.LENGTH_SHORT).show();
                }
            }
        });
        Log.d(LOG_TAG, " > setup myLocationButton");

        // Set up startWorkingButton
        startWorkingButton = root.findViewById(R.id.startWorkingButton);
        startWorkingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (insideZone){ // Additional check
                    Log.d(LOG_TAG, "Pressed work button while inside zone!");
                    // TODO: Move to new activity
                }
            }
        });
        Log.d(LOG_TAG, " > setup startWorkingButton");
    }

    public boolean checkPermissions(String... permissions) {
        if (permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(getActivity(), permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(LOG_TAG, "called onCreate()");
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onStart() {
        Log.i(LOG_TAG, "called onStart()");
        super.onStart();
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
        Log.i(LOG_TAG, "called onMapReady()");
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
        fusedLocationClient.getLastLocation().addOnSuccessListener(getActivity(), new OnSuccessListener<Location>() {
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

        requestZonesFromDb();
    }

    // Inspired by: https://developer.android.com/training/location/receive-location-updates
    private void startLocationUpdates() {
        Log.d(LOG_TAG, "startLocationUpdates() called");
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
    }

    @Override
    public void onResume() {
        Log.d(LOG_TAG, "called onResume()");
        super.onResume();
        if (checkPermissions()) {
            startLocationUpdates();
        }
    }

    @Override
    public void onPause() {
        Log.d(LOG_TAG, "called onPause()");
        super.onPause();
        if (checkPermissions()) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
        }
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
        Log.d(LOG_TAG, "onRequestPermissionsResult() called");
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        boolean permissionsAccepted = false;
        switch (requestCode){
            case REQUEST_LOCATION_PERMISSIONS:
                permissionsAccepted  = (grantResults[0] == PackageManager.PERMISSION_GRANTED) && (grantResults[1] == PackageManager.PERMISSION_GRANTED);
        }
        Log.d(LOG_TAG, "onRequestPermissionsResult(): "+permissionsAccepted);
        if (!permissionsAccepted ){
            // User is required to allow location tracking - display error message
            Toast.makeText(getActivity().getApplicationContext(), getString(R.string.require_location_permissions), Toast.LENGTH_LONG).show();
            getActivity().finish();
        } else{
            initializeComponents();
        }
    }

    private void requestZonesFromDb(){
        AsyncGetZonesTask zoneTask = new AsyncGetZonesTask(new AsyncGetZonesTask.AsyncResponse(){
            @Override
            public void processFinish(ArrayList<Zone> output){
                Log.d(LOG_TAG, "processFinish() called");
            }
        });
        try{
            zones = zoneTask.execute().get();
            for (Zone z : zones){
                addZoneToMap(z.getCoords());
            }
        } catch (Exception e) {
            Log.e(LOG_TAG, "registerTask.execute() failed: "+e.toString());
        }
    }

    private void addZoneToMap(LatLng[] coords){
        PolygonOptions polyOpts = new PolygonOptions().clickable(true).add(coords);
        Polygon polygon1 = mMap.addPolygon(polyOpts);
        // Store a data object with the polygon, used here to indicate an arbitrary type.
        polygon1.setTag("A");
        polygon1.setStrokeWidth(6); // px width of stroke
        polygon1.setStrokeColor(0xffff8800); // Opaque orange
        polygon1.setFillColor(0x66ff8800); // Transparent orange defined as ARGB
    }
}