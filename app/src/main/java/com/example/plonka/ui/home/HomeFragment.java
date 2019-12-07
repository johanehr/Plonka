package com.example.plonka.ui.home;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;

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
import com.google.android.material.snackbar.Snackbar;

import android.os.Looper;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.navigation.NavigationView;

import androidx.drawerlayout.widget.DrawerLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.Menu;
import android.widget.Button;
import android.widget.Toast;

import java.util.ArrayList;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.example.plonka.R;

public class HomeFragment extends Fragment implements OnMapReadyCallback{

    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    private LatLng userLocation;
    private ArrayList<ArrayList<LatLng>> zoneLocations;
    private FloatingActionButton fabButton;
    private Button startWorkingButton;
    private UiSettings mUiSettings;

    private String LOG_TAG = "PLONKA_MAP_FRAGMENT";

    private static final int REQUEST_LOCATION_PERMISSIONS = 111;
    private String [] permissions = {Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}; // TODO: , Manifest.permission.ACCESS_BACKGROUND_LOCATION}; https://developer.android.com/training/location/receive-location-updates.html#request-background-location
    private boolean permissionsAccepted = false;


    @Override // TODO: ADDED THIS
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        Log.i(LOG_TAG, "onCreateView() called");

        View root = inflater.inflate(R.layout.fragment_map, container, false);

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.mapFragment);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
        else {
            Log.e(LOG_TAG, " > mapFragment == null: failure");
        }

        // Set up periodic GPS-location request
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());
        locationRequest = LocationRequest.create();
        locationRequest.setInterval(2000); // every 2s
        locationRequest.setFastestInterval(1000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                Log.i(LOG_TAG, "called locationCallback()");
                if (locationResult == null) {
                    Log.w(LOG_TAG, "LocationCallback(): locationResult == null");
                    return;
                }

                /*

                double latitude = locationResult.getLastLocation().getLatitude();
                double longitude = locationResult.getLastLocation().getLongitude();
                double accuracy = locationResult.getLastLocation().getAccuracy();

                 */

                double latitude = 0, longitude = 0, accuracy = 0;
                for (Location location : locationResult.getLocations()){
                    latitude = location.getLatitude();
                    longitude = location.getLongitude();
                    accuracy = location.getAccuracy();
                }

                userLocation = new LatLng(latitude, longitude);

                String gpsData = "\nlat:" + latitude + "\nlong:" + longitude + "\naccuracy: " + accuracy + "m"; // https://developer.android.com/reference/android/location/Location.html
                Log.i(LOG_TAG, "onLocationResult() called: "+gpsData);

                // TODO: Check if within a zone. If yes, activate button and set text. If not, disable w/ other text.
                startWorkingButton.setClickable(true);
                startWorkingButton.setEnabled(true);
            }
        };
        // Request periodic location updates
        startLocationUpdates();

        Log.i(LOG_TAG, " > setup fusedLocationClient");

        // Set up myLocationButton
        fabButton = root.findViewById(R.id.myLocationButton);
        fabButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(LOG_TAG, "Pressed FAB!");
                if (userLocation != null){
                    CameraPosition cameraPosition = new CameraPosition.Builder()
                            .target(userLocation)       // Sets the center of the map to Mountain View
                            .zoom(16)                   // Sets the zoom
                            .build();                   // Creates a CameraPosition from the builder
                    mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition), 250, null); // Animation takes 250ms
                    Log.i(LOG_TAG, "Moved to current user location, due to FAB button.");
                }
                else {
                    Log.w(LOG_TAG, "Couldn't move to current user location, userLocation == null");
                    Toast.makeText(getActivity().getApplicationContext(), "Unknown location... Please try again soon.", Toast.LENGTH_SHORT).show();
                }

            }
        });
        Log.i(LOG_TAG, " > setup myLocationButton");

        // Set up startWorkingButton
        startWorkingButton = root.findViewById(R.id.startWorkingButton);
        startWorkingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e(LOG_TAG, "Pressed work button!");
                // TODO: Move to separate work activity
            }
        });
        Log.i(LOG_TAG, " > setup startWorkingButton");

        // TODO: Gather zone data from database
        // zoneLocations = getZonesFromDb();
        // list/array/queue of LatLngs -> polygon using polyutil?
        // if (!zoneDataAddedToMap){ addToMap(); }

        return root;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(LOG_TAG, "called onCreate()");
        super.onCreate(savedInstanceState);

        // Check that user has agreed to location permissions - if not, activity is finished
        ActivityCompat.requestPermissions(getActivity(), permissions, REQUEST_LOCATION_PERMISSIONS);
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
        mMap.setBuildingsEnabled(true); // cool 3D buildings!
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
                    Log.i(LOG_TAG, " > moved map to last known location");
                }
            }
        });
    }

    // Inspired by: https://developer.android.com/training/location/receive-location-updates
    private void startLocationUpdates() {
        Log.i(LOG_TAG, "startLocationUpdates() called");
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
    }

    @Override
    public void onResume() {
        Log.i(LOG_TAG, "called onResume()");
        super.onResume();
        if (permissionsAccepted) {
            startLocationUpdates();
        }
    }

    @Override
    public void onPause() {
        Log.i(LOG_TAG, "called onPause()");
        super.onPause();
        fusedLocationClient.removeLocationUpdates(locationCallback);
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

        }
        Log.i(LOG_TAG, "onRequestPermissionsResult(): "+permissionsAccepted);
        if (!permissionsAccepted ){
            // User is required to allow location tracking - display error message
            Toast.makeText(getActivity().getApplicationContext(), getString(R.string.require_location_permissions), Toast.LENGTH_LONG).show();
            getActivity().finish();
        }
    }
}