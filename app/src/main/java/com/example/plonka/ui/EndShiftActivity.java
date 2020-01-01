package com.example.plonka.ui;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.example.plonka.R;
import com.example.plonka.SessionLog;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.maps.model.LatLng;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;

public class EndShiftActivity extends AppCompatActivity implements AbandonShiftDialogFragment.AbandonShiftDialogListener {

    private ArrayList<SessionLog> sessionLogList;
    private static final String LOG_TAG = "PLONKA_END_SHIFT_ACTIVITY";
    private static final String logFileName = "session.log";

    private Button abandonButton;
    private Button submitPhotoButton;
    private Vibrator vibrator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_end_shift);

        // TODO: Get user information! (necessary for DB insertion)

        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);

        sessionLogList = readLogFile();

        // TODO: Set up user interface (Abandon, Submit photo and end session, Image viewer with photo opportunity)
        abandonButton = findViewById(R.id.abandonButton);
        submitPhotoButton = findViewById(R.id.submitButton);

        abandonButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(LOG_TAG, "Pressed abandon button!");
                vibrator.vibrate(VibrationEffect.createOneShot(30, 30));
                showAbandonShiftDialog();
            }
        });

        // TODO: Post to DB/webserver using PHP. If successful, call clearLogFile()
        // TODO: How to name photo? UserId_timestamp.png, should be unique? (see microphone app)
    }

    @Override
    public void onBackPressed() {
        showAbandonShiftDialog();
    }

    private void showAbandonShiftDialog() {
        Log.i(LOG_TAG, "showAbandonShiftDialog() called");
        AbandonShiftDialogFragment newFragment = new AbandonShiftDialogFragment();
        newFragment.show(getSupportFragmentManager(), "abandonShift");
    }

    // The dialog fragment receives a reference to this Activity through the
    // Fragment.onAttach() callback, which it uses to call the following methods
    // defined by the EndShiftDialogFragment.EndShiftDialogListener interface
    @Override
    public void onDialogPositiveClick(DialogFragment dialog) {
        // User touched the dialog's positive button
        Log.d(LOG_TAG, "onDialogPositiveClick() called");
        clearLogFile(); // Can't continue on an actively abandoned session, so clear file
        finish();
    }

    @Override
    public void onDialogNegativeClick(DialogFragment dialog) {
        // User touched the dialog's negative button, just cancels.
        Log.d(LOG_TAG, "onDialogNegativeClick() called");
    }

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

    private void clearLogFile(){
        File file = new File(getApplicationContext().getFilesDir(), logFileName);
        if(file.delete()){
            Log.i(LOG_TAG, "Log file removed!");
        }
        else{
            Log.e(LOG_TAG, "Error removing log file!");
        }
    }
}
