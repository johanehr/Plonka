package com.example.plonka.ui.login;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import android.util.Log;
import android.util.Patterns;

import com.example.plonka.data.LoginRepository;
import com.example.plonka.data.Result;
import com.example.plonka.data.model.LoggedInUser;
import com.example.plonka.R;

public class LoginViewModel extends ViewModel {

    private MutableLiveData<LoginFormState> loginFormState = new MutableLiveData<>();
    private MutableLiveData<LoginResult> loginResult = new MutableLiveData<>();
    private LoginRepository loginRepository;
    private String LOG_TAG = "PLONKA_LOGINVIEWMODEL";

    LoginViewModel(LoginRepository loginRepository) {
        this.loginRepository = loginRepository;
    }

    LiveData<LoginFormState> getLoginFormState() {
        return loginFormState;
    }

    LiveData<LoginResult> getLoginResult() { return loginResult; }

    // Attempt to log in the user with specified personal number and password
    public void login(long user, String password) {
        Log.d(LOG_TAG, "login() called");
        Result<LoggedInUser> result = loginRepository.login(user, password);
        if (result instanceof Result.Success) {
            Log.d(LOG_TAG, "login() result is success");
            LoggedInUser data = ((Result.Success<LoggedInUser>) result).getData();
            loginResult.setValue(new LoginResult(new LoggedInUserView(data.getDisplayName()))); // Only need display name for UI purposes, id is hidden from user
        }
        else if (result instanceof Result.Error) {
            Log.d(LOG_TAG, "login() result is failure");
            String errorMsg = ((Result.Error) result).getError().toString();
            loginResult.setValue(new LoginResult(errorMsg)); // Pass error string printed by login_user.php back to UI
        }
    }

    public void loginDataChanged(long username, String password) {
        if (!isUserNameValid(username)) {
            loginFormState.setValue(new LoginFormState(R.string.invalid_username, null));
        } else if (!isPasswordValid(password)) {
            loginFormState.setValue(new LoginFormState(null, R.string.invalid_password));
        } else {
            loginFormState.setValue(new LoginFormState(true));
        }
    }

    // A simple username validation check
    private boolean isUserNameValid(long username) {
        // OK if username is 10 digits (personal number), EXTENSION: Use control numbers to check if valid
        if (username > 190000000000L) {
            Log.d(LOG_TAG, "isUserNameValid("+Long.toString(username)+")=true");
            return true;
        } else {
            Log.d(LOG_TAG, "isUserNameValid("+Long.toString(username)+")=false");
            return false;
        }
    }

    // A simple password validation check, require at least 8 chars
    private boolean isPasswordValid(String password) {
        return password != null && password.trim().length() > 8;
    }
}
