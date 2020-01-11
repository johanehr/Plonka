package com.example.plonka;

import com.example.plonka.data.model.LoggedInUser;

import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.core.view.GravityCompat;
import androidx.fragment.app.DialogFragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.plonka.ui.ShiftActivity;
import com.example.plonka.ui.map.MapFragment;
import com.example.plonka.ui.map.StartShiftDialogFragment;
import com.google.android.material.navigation.NavigationView;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Main activity in app where the user can navigate between multiple fragments
 */
public class HubActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, MapFragment.startShiftListener {

    private AppBarConfiguration mAppBarConfiguration;
    private LoggedInUser currentUser; // Contains necessary data for requesting data from DB
    private String LOG_TAG = "PLONKA_HUB_ACTIVITY";

    /**
     * Sets up the UI with a side drawer used to navigate to different fragments
     * @param savedInstanceState unused
     * @return void
     */
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
                R.id.nav_map, R.id.nav_history, R.id.nav_faq)
                .setDrawerLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        // Set up custom intents for drawer menu buttons
        navigationView.setNavigationItemSelectedListener(this);

        // Set up user profile at top
        View headerView = navigationView.getHeaderView(0); // This requires a certain order in xml file
        TextView textUserName = (TextView) headerView.findViewById(R.id.textViewUserName);
        textUserName.setText(userName);
        TextView textUserId = (TextView) headerView.findViewById(R.id.textViewUserId);
        textUserId.setText("User ID: "+userId);

        Log.i(LOG_TAG, "Setup complete");
    }

    /**
     * Set up navigation flow between fragments
     */
    @Override
    public boolean onSupportNavigateUp() {
        Log.i(LOG_TAG, "called onSupportNavigateUp()");
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    // From: https://stackoverflow.com/questions/42297381/onclick-event-in-navigation-drawer

    /**
     * Handles special cases for intents invoked when clicking in navigation drawer menu items for getting in touch.
     * @param item selected drawer menu item
     */
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {

        // Handle navigation view item clicks here.
        switch (item.getItemId()) {

            case R.id.nav_support_email:
            {
                mailCallback();
                break;
            }
            case R.id.nav_support_phone:
            {
                phoneCallback();
                break;
            }
            default:
            {
                Log.d(LOG_TAG, "Entered default case in onNavigationItemSelected()");
                Navigation.findNavController(this, R.id.nav_host_fragment).navigate(item.getItemId());
            }
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);

        return true;
    }

    /**
     * Initiate a phone dialer to get in contact with Plonka.
     * TODO: Using dummy phone number
     * @return void
     */
    private void phoneCallback(){
        Log.d(LOG_TAG, "Pressed nav_support_phone");
        Intent call = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:+46701234567"));
        if (call.resolveActivity(getPackageManager()) != null) {
            startActivity(call);
        }
    }

    /**
     * Initiate an email intent with some pre-filled information for Plonka representative to identify user
     * TODO: Set up this email account
     * @return void
     */
    private void mailCallback(){
        Log.d(LOG_TAG, "Pressed nav_support_email");
        Intent email = new Intent(Intent.ACTION_SEND);
        email.setType("text/plain");
        email.putExtra(Intent.EXTRA_EMAIL, new String[] {"support@plonka.se"});
        email.putExtra(Intent.EXTRA_SUBJECT, "Plonka support: <ENTER TITLE>");
        email.putExtra(Intent.EXTRA_TEXT, "User ID: "+currentUser.getAccountId()+"\n\n<ENTER YOUR SUPPORT ISSUE HERE>");

        if (email.resolveActivity(getPackageManager()) != null) {
            startActivity(Intent.createChooser(email, "Choose your e-mail client:"));
        }
    }

    /**
     * Use an interface to handle when button to start a work shift is pressed in MapFragment
     * @param zones the zones to include in started shift
     * @return void
     */
    @Override
    public void startShiftInterface(ArrayList<Zone> zones){
        Log.i(LOG_TAG, "RECEIVED START_SHIFT_INTERFACE!");

        // Log in details passed on to new activity. TECHNICAL DEBT: use parcelable instead of multiple putExtra fields
        Intent shiftIntent = new Intent(getApplicationContext(), ShiftActivity.class);
        shiftIntent.putExtra("userPw", currentUser.getPassword());
        shiftIntent.putExtra("userId",currentUser.getAccountId());
        shiftIntent.putExtra("userName", currentUser.getDisplayName());

        Bundle zoneBundle = new Bundle();
        zoneBundle.putSerializable("zones", (Serializable) zones);
        shiftIntent.putExtra("zoneBundle", zoneBundle);

        startActivity(shiftIntent);
    }
}