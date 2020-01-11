package com.example.plonka.ui.login;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import android.util.Log;

import com.example.plonka.data.LoginRepository;
import com.example.plonka.data.Result;
import com.example.plonka.data.model.LoggedInUser;
import com.example.plonka.R;

/**
 * LoginViewModel is used as part of the log in process.
 * This class was partially generated through File > New > Activity > Login Activity in Android Studio
 */
public class LoginViewModel extends ViewModel {

    private MutableLiveData<LoginFormState> loginFormState = new MutableLiveData<>();
    private MutableLiveData<LoginResult> loginResult = new MutableLiveData<>();
    private LoginRepository loginRepository;
    private LoggedInUser currentUser;
    private String LOG_TAG = "PLONKA_LOGINVIEWMODEL";

    /**
     * Constructor, auto-generated
     * @param loginRepository -
     */
    LoginViewModel(LoginRepository loginRepository) {
        this.loginRepository = loginRepository;
    }

    /**
     * returns form state, auto-generated
     * @return login form state
     */
    LiveData<LoginFormState> getLoginFormState() {
        return loginFormState;
    }

    /**
     * returns login result, auto-generated
     * @return login result
     */
    LiveData<LoginResult> getLoginResult() { return loginResult; }

    /**
     *  Attempt to log in the user with specified personal number and password
     * @param user username (personal number), long required due to length
     * @param password password
      */
    public void login(Long user, String password) {
        Log.d(LOG_TAG, "login() called");
        Result<LoggedInUser> result = loginRepository.login(user, password);
        if (result instanceof Result.Success) {
            Log.d(LOG_TAG, "login() result is success");
            currentUser = ((Result.Success<LoggedInUser>) result).getData(); // Set currentUser member variable
            loginResult.setValue(new LoginResult(new LoggedInUserView(currentUser.getDisplayName()))); // Only need display name for UI purposes, rest is hidden from user
        }
        else if (result instanceof Result.Error) {
            Log.d(LOG_TAG, "login() result is failure");
            String errorMsg = ((Result.Error) result).getError().toString();
            loginResult.setValue(new LoginResult(errorMsg)); // Pass error string printed by login_user.php back to UI
        }
    }

    /**
     * Getter for current user
     * @return LoggedInUser currently logged in user
     */
    public LoggedInUser getCurrentUser(){
        return currentUser;
    }

    /**
     * Updates login data upon changes to the form and validates input
     * @param username provided username
     * @param password provided password
     */
    public void loginDataChanged(String username, String password) {
        if (!isUserNameValid(username)) {
            loginFormState.setValue(new LoginFormState(R.string.invalid_username, null));
        } else if (!isPasswordValid(password)) {
            loginFormState.setValue(new LoginFormState(null, R.string.invalid_password));
        } else {
            loginFormState.setValue(new LoginFormState(true));
        }
    }

    /**
     * validates username (personal number) to be 10 digits
     * @param username_str personal number
     * @return boolean, whether username is valid
     */
    private boolean isUserNameValid(String username_str) {

        try{
            Long username = Long.parseLong(username_str);
            // OK if username is a reasonable value containing 10 digits (personal number), EXTENSION: Use control numbers to check if valid
            if (username > 190000000000L) {
                Log.d(LOG_TAG, "isUserNameValid("+Long.toString(username)+")=true");
                return true;
            } else {
                Log.d(LOG_TAG, "isUserNameValid("+Long.toString(username)+")=false");
                return false;
            }
        }
        catch(NumberFormatException e){
            Log.d(LOG_TAG, "User entered non-numerical username");
            return false;
        }
    }

    /**
     * A simple password validation check, require at least 8 chars
     * @param password user-provided password
     * @return boolean, whether password meets requirements
     */
    private boolean isPasswordValid(String password) {
        return password != null && password.trim().length() > 8;
    }
}
