package com.example.plonka.ui.login;

import android.app.Activity;

import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.plonka.R;
import com.example.plonka.ui.login.LoginViewModel;
import com.example.plonka.ui.login.LoginViewModelFactory;

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
        final EditText passwordEditText = findViewById(R.id.password);
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
                loginButton.setEnabled(loginFormState.isDataValid());
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
                    // TODO : initiate successful logged in experience, move to map activity. Save log in status somewhere? Destroy current activity?
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
                loginViewModel.loginDataChanged(Long.parseLong(usernameEditText.getText().toString()), passwordEditText.getText().toString());
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
                loginViewModel.login(Long.parseLong(usernameEditText.getText().toString()), passwordEditText.getText().toString());
            }
        });


        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(LOG_TAG, "registerButton clicked!");
                Toast.makeText(getApplicationContext(), "Registration not yet implemented!", Toast.LENGTH_SHORT).show();
                // TODO: Move user to register activity when registerButton is clicked (use existing sign-up PHP script?)
            }
        });

        Log.d(LOG_TAG, "onCreate() complete");
    }

    private void updateUiWithUser(LoggedInUserView model) {
        Log.d(LOG_TAG, "updateUiWithUser()");
        String welcome = getString(R.string.welcome) + model.getDisplayName();
        // Overlay a Toast to welcome user
        Toast.makeText(getApplicationContext(), welcome, Toast.LENGTH_LONG).show();
    }

    // Show a Toast with login failure message
    private void showLoginFailed(String errorString) {
        Log.d(LOG_TAG, "showLoginFailed()");
        Toast.makeText(getApplicationContext(), errorString, Toast.LENGTH_SHORT).show();
    }
}