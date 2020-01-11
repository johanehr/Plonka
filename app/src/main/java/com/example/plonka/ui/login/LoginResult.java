package com.example.plonka.ui.login;

import androidx.annotation.Nullable;

/**
 * Authentication result : success (has user details) or error message.
 * This class was partially generated through File > New > Activity > Login Activity in Android Studio
 */
class LoginResult {
    @Nullable
    private LoggedInUserView success;
    @Nullable
    private String error;

    /**
     * Constructor with String argument containing error
     * @param error error message for failed login
     */
    LoginResult(@Nullable String error) {
        this.error = error;
    }

    /**
     * Constructor with LoggedInUserView argument on success
     * @param success the logged in user
     */
    LoginResult(@Nullable LoggedInUserView success) {
        this.success = success;
    }

    /**
     * returns user info on success
     * @return Logged in user
     */
    @Nullable
    LoggedInUserView getSuccess() {
        return success;
    }

    /**
     * returns error message on error
     * @return error message
     */
    @Nullable
    String getError() {
        return error;
    }
}
