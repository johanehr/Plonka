package com.example.plonka;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;

import com.example.plonka.ui.login.LoginActivity;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;

import android.os.VibrationEffect;
import android.os.Vibrator;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
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

/**
 * RegisterUserActivity is launched from LoginActivity to register a new user to the online database through an invoked PHP-script
 */
public class RegisterUserActivity extends AppCompatActivity {

    private String LOG_TAG = "PLONKA_REGISTER_USER";

    private EditText personal_number;
    private EditText name;
    private EditText email;
    private EditText phone;
    private EditText password;
    private EditText password_confirm;
    private CheckBox checkbox_terms;
    private Button continueButton;
    Vibrator vibrator;

    private String str_name;
    private String str_email;
    private String str_personal_number;
    private String str_phone;
    private String str_password;
    private String str_password_confirm;
    private boolean terms_accepted;

    /**
     * Sets up the UI (form fields, buttons, etc)
     * @param savedInstanceState unused
     * @return void
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_user);

        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);

        personal_number = findViewById(R.id.editTextPersonalNumber); // EXTENSION: Use personal number to automatically fetch name, mobile, etc using some kind of look-up API
        name = findViewById(R.id.editTextName);
        email = findViewById(R.id.editTextEmail);
        phone = findViewById(R.id.editTextMobile);
        password = findViewById(R.id.editTextPassword);
        password_confirm = findViewById(R.id.editTextPasswordConfirm);
        checkbox_terms = findViewById(R.id.checkBoxTerms);

        // Make links clickable in checkbox_terms, https://www.tutorialspoint.com/how-to-set-the-part-of-the-android-textview-as-clickable
        String checkbox_text = getResources().getString(R.string.accept_terms);
        SpannableString spannableString = new SpannableString(checkbox_text);
        ClickableSpan clickableSpanTermsAndConditions = new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                Log.d(LOG_TAG, "Clicked Terms and Conditions link");
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com")));
            }
        };
        ClickableSpan clickableSpanPrivacyPolicy = new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                Log.d(LOG_TAG, "Clicked Privacy Policy link");
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com")));
            }
        };
        spannableString.setSpan(clickableSpanTermsAndConditions, 27,45, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableString.setSpan(clickableSpanPrivacyPolicy, 50,64, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        checkbox_terms.setText(spannableString);
        checkbox_terms.setMovementMethod(LinkMovementMethod.getInstance());

        continueButton = findViewById(R.id.buttonContinue);
        continueButton.setOnClickListener(new View.OnClickListener() {
            /**
             * Vibrate and attempt to register user to database
             * @param view button with listener
             */
            @Override
            public void onClick(View view) {
                Log.i(LOG_TAG, "User clicked Continue button");
                vibrator.vibrate(VibrationEffect.createOneShot(30, 30));
                gatherContent();
                if (validateRegistrationDetails()){
                    registerUserToDb();
                }
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    /**
     * Check whether provided form strings are valid. Displays error message in Snackbar if not.
     * @return boolean, whether form strings are valid or not
     */
    public boolean validateRegistrationDetails(){
        boolean valid = true;
        String feedbackMsg = "Registering user...";

        if (!Patterns.EMAIL_ADDRESS.matcher(str_email).matches()){
            // Appears to be a valid email
            feedbackMsg = "ERROR: Invalid email format";
            valid = false;
        }
        else if (str_password.length() < 8){
            feedbackMsg = "ERROR: Password must be 8+ chars";
            valid = false;
        }
        else if (!str_password.equals(str_password_confirm)){
            feedbackMsg = "ERROR: Passwords must match";
            Log.d(LOG_TAG, "Password mismatch");
            valid = false;
        }
        else if (!str_phone.matches("^(\\+467|07)[0-9]{8}$")){
            feedbackMsg = "ERROR: Phone number format is incorrect.";
            valid = false;
        }
        else if (!str_personal_number.matches("^[0-9]{12}$")){
            feedbackMsg = "ERROR: Personal number must be 12 digits";
            valid = false;
        }
        else if (!terms_accepted){
            feedbackMsg = "ERROR: Accept terms and conditions";
            valid = false;
        }

        Snackbar.make(findViewById(android.R.id.content), feedbackMsg, Snackbar.LENGTH_SHORT).show();
        return valid;
    }

    /**
     * Gather the form strings from fields to local variables
     * @return void
     */
    public void gatherContent() {
        str_name = name.getText().toString();
        str_email = email.getText().toString();
        str_personal_number = personal_number.getText().toString();
        str_phone = phone.getText().toString();
        str_password = password.getText().toString();
        str_password_confirm = password_confirm.getText().toString();
        terms_accepted = checkbox_terms.isChecked();

        Log.d(LOG_TAG, "Gathered the following message data:\n  - "
                +str_name+"\n  - "
                +str_email+"\n  - "
                +str_personal_number+"\n  - "
                +str_phone+"\n  - "
                +str_password+"\n  - "
                +str_password_confirm+"\n  - "
                +terms_accepted);
    }

    /**
     * Register the user to online SQL database by invoking a PHP-script on webserver.
     * - https://www.tutorialspoint.com/android/android_php_mysql.htm
     * - https://androidjson.com/android-php-send-data-mysql-database/
     */
    public void registerUserToDb(){
        /**
         * Custom AsyncTask used to post form data, String returned
         */
        class AsyncRegisterTask extends AsyncTask<Void, Void, String> {
            /**
             * Call the PHP-script with form data through POST and read the results to check whether it worked.
             * @param voids no arguments
             * @return String result from webserver
             */
            @Override
            protected String doInBackground(Void... voids) {
                Log.i(LOG_TAG, "called doInBackground()");

                HashMap<String, String> params = new HashMap<>();;
                params.put("name", str_name);
                params.put("email", str_email);
                params.put("phone", str_phone);
                params.put("psw1", str_password);
                params.put("psw2", str_password_confirm);
                params.put("personal_number", str_personal_number);
                params.put("conditions", "accept");

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
                    URL url = new URL("https://people.dsv.su.se/~joeh1022/scripts/register_user.php");
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
                    Log.d(LOG_TAG, "Post success: " + res.contains("success") + "\nResult from server: \n" + res);
                }
                return res; // If success is included in php output, SQL insert worked!
            }

            /**
             * Create debug log message regarding result from webserver
             * @param res result string from webserver
             */
            @Override
            protected void onPostExecute(String res){
                super.onPostExecute(res);
                String msg;
                if (res.contains("success")){
                    msg = "Registration COMPLETED SUCCESSFULLY";
                }
                else {
                    msg = "Registration FAILED";
                }
                Log.d(LOG_TAG, msg);
            }
        }

        Log.i(LOG_TAG, "Called postToDb()");
        AsyncRegisterTask registerTask = new AsyncRegisterTask();
        String registerOutput = "registerTask failed"; // Changed if successful
        try{
            registerOutput = registerTask.execute().get();
        } catch (Exception e) {
            Log.e(LOG_TAG, "registerTask.execute() failed: "+e.toString());
        }

        Log.i(LOG_TAG, "Register output: "+registerOutput);

        Toast.makeText(getApplicationContext(), registerOutput, Toast.LENGTH_LONG).show(); // Shows confirmation/error msg from server
        if (registerOutput.contains("success")) {
            // Send user to login page if successfully registered
            Intent loginIntent = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(loginIntent);
            finish();
        }

        Log.i(LOG_TAG, "Exit postToDb()");
    }
}