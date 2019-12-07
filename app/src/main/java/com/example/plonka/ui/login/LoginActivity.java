package com.example.plonka.ui.login;

import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.plonka.HubActivity;
import com.example.plonka.R;
import com.example.plonka.RegisterUserActivity;
import com.example.plonka.data.model.LoggedInUser;

public class LoginActivity extends AppCompatActivity {

    private LoginViewModel loginViewModel;
    private String LOG_TAG = "PLONKA_LOGIN_ACTIVITY";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        loginViewModel = ViewModelProviders.of(this, new LoginViewModelFactory())
                .get(LoginViewModel.class);

        final EditText usernameEditText = findViewById(R.id.username);
        usernameEditText.setShadowLayer(5, 0, 0, Color.BLACK);
        final EditText passwordEditText = findViewById(R.id.password);
        passwordEditText.setShadowLayer(5, 0, 0, Color.BLACK);
        final Button loginButton = findViewById(R.id.login);
        final Button registerButton = findViewById(R.id.register);

        // Check if log in details fulfill criteria to enable log-in button
        loginViewModel.getLoginFormState().observe(this, new Observer<LoginFormState>() {
            @Override
            public void onChanged(@Nullable LoginFormState loginFormState) {
                if (loginFormState == null) {
                    return;
                }
                if (loginFormState.getUsernameError() != null) {
                    usernameEditText.setError(getString(loginFormState.getUsernameError()));
                }
                if (loginFormState.getPasswordError() != null) {
                    passwordEditText.setError(getString(loginFormState.getPasswordError()));
                }
            }
        });

        loginViewModel.getLoginResult().observe(this, new Observer<LoginResult>() {
            @Override
            public void onChanged(@Nullable LoginResult loginResult) {
                if (loginResult == null) {
                    return;
                }
                if (loginResult.getSuccess() != null) {
                    updateUiWithUser(loginResult.getSuccess());

                    // Log in details passed on to new activity. TECHNICAL DEBT: use parcelable instead of multiple putExtra fields
                    Intent mapIntent = new Intent(getApplicationContext(), HubActivity.class);
                    LoggedInUser user = loginViewModel.getCurrentUser();
                    mapIntent.putExtra("userPw", user.getPassword());
                    mapIntent.putExtra("userId",user.getAccountId());
                    mapIntent.putExtra("userName", user.getDisplayName());

                    // Initiate logged in experience, i.e. move to map activity. Will not come back to this activity, so finish.
                    startActivity(mapIntent);
                    finish();
                }
                if (loginResult.getError() != null) {
                    showLoginFailed(loginResult.getError());
                }

            }
        });

        TextWatcher afterTextChangedListener = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // ignore
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // ignore
            }

            @Override
            public void afterTextChanged(Editable s) {
                String username = usernameEditText.getText().toString();
                String password = passwordEditText.getText().toString();
                loginViewModel.loginDataChanged(username, password);
            }
        };

        // Adds functions to be called after changing text in EditText fields
        usernameEditText.addTextChangedListener(afterTextChangedListener);
        passwordEditText.addTextChangedListener(afterTextChangedListener);
        passwordEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    loginViewModel.login(Long.parseLong(usernameEditText.getText().toString()),
                            passwordEditText.getText().toString());
                }
                return false;
            }
        });

        // Attempt to log in user when clicking loginButton
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.v(LOG_TAG, "loginButton clicked");
                Toast.makeText(getApplicationContext(), "Attempting to log in...", Toast.LENGTH_SHORT).show();

                try {
                    loginViewModel.login(Long.parseLong(usernameEditText.getText().toString()), passwordEditText.getText().toString());
                }
                catch (NumberFormatException e){
                    Toast.makeText(getApplicationContext(), "Make sure your login details are in the right format.", Toast.LENGTH_LONG).show();
                }
            }
        });


        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(LOG_TAG, "registerButton clicked!");
                // Move user to register activity when registerButton is clicked
                Intent registerIntent = new Intent(getApplicationContext(), RegisterUserActivity.class);
                startActivity(registerIntent);
                // do NOT finish this activity, keep in background in case user misclicked
            }
        });

        Log.d(LOG_TAG, "onCreate() complete");
    }

    private void updateUiWithUser(LoggedInUserView model) {
        Log.d(LOG_TAG, "updateUiWithUser() called");
        String welcome = getString(R.string.welcome) + model.getDisplayName();
        // Overlay a Toast to welcome user
        Toast.makeText(getApplicationContext(), welcome, Toast.LENGTH_LONG).show();
    }

    // Show a Toast with login failure message
    private void showLoginFailed(String errorString) {
        Log.d(LOG_TAG, "showLoginFailed() called");
        Toast.makeText(getApplicationContext(), errorString, Toast.LENGTH_SHORT).show();
    }
}