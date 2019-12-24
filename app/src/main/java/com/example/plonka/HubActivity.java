package com.example.plonka;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
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
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.view.GravityCompat;
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
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class HubActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

    private AppBarConfiguration mAppBarConfiguration;

    private LoggedInUser currentUser; // Contains necessary data for requesting data from DB
    private String LOG_TAG = "PLONKA_HUB_ACTIVITY";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Retain user information, included in activity intent extras
        Integer userId = getIntent().getExtras().getInt("userId");
        String userPw = getIntent().getExtras().getString("userPw");
        String userName = getIntent().getExtras().getString("userName");
        currentUser = new LoggedInUser(userId, userPw, userName);
        Log.i(LOG_TAG, "LoggedInUser: "+userId+" - "+userPw+" - "+userName);

        // Set up layout
        setContentView(R.layout.activity_hub);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_map, R.id.nav_gallery, R.id.nav_faq)
                .setDrawerLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        // Set up custom intents for drawer menu buttons
        navigationView.setNavigationItemSelectedListener(this);

        // Set up user profile at top
        View headerView = navigationView.getHeaderView(0);
        TextView textUserName = (TextView) headerView.findViewById(R.id.textViewUserName);
        textUserName.setText(userName);
        TextView textUserId = (TextView) headerView.findViewById(R.id.textViewUserId);
        textUserId.setText("User ID: "+userId);

        Log.i(LOG_TAG, "Setup complete");
    }

    @Override
    public boolean onSupportNavigateUp() {
        Log.i(LOG_TAG, "called onSupportNavigateUp()");
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    // From: https://stackoverflow.com/questions/42297381/onclick-event-in-navigation-drawer
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // TODO: Fix so that all clicks work, broke legacy. Use onClick() for menu item instead? https://developer.android.com/guide/topics/ui/menus.html#java 
        // Handle navigation view item clicks here.
        switch (item.getItemId()) {

            case R.id.nav_support_email: {
                Log.d(LOG_TAG, "Pressed nav_support_email");
                Intent email = new Intent(Intent.ACTION_SEND);
                email.setType("text/plain");
                email.putExtra(Intent.EXTRA_EMAIL, new String[] {"support@plonka.se"});
                email.putExtra(Intent.EXTRA_SUBJECT, "Plonka support: <ENTER TITLE>");
                email.putExtra(Intent.EXTRA_TEXT, "User ID: "+currentUser.getAccountId()+"\n\n<ENTER YOUR SUPPORT ISSUE HERE>");

                if (email.resolveActivity(getPackageManager()) != null) {
                    startActivity(Intent.createChooser(email, "Choose your e-mail client:"));
                }
                break;
            }
            case R.id.nav_support_phone: {
                Log.d(LOG_TAG, "Pressed nav_support_phone");
                Intent call = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:+46701234567"));
                if (call.resolveActivity(getPackageManager()) != null) {
                    startActivity(call);
                }
                break;
            }
            default: {
                return super.onOptionsItemSelected(item);
            }
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

}
