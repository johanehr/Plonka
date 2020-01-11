package com.example.plonka.ui.login;

import androidx.annotation.Nullable;

/**
 * Data validation state of the login form.
 * This class was partially generated through File > New > Activity > Login Activity in Android Studio
 */
class LoginFormState {
    @Nullable
    private Integer usernameError;
    @Nullable
    private Integer passwordError;
    private boolean isDataValid;

    /**
     * Constructor with errors
     * @param usernameError
     * @param passwordError
     */
    LoginFormState(@Nullable Integer usernameError, @Nullable Integer passwordError) {
        this.usernameError = usernameError;
        this.passwordError = passwordError;
        this.isDataValid = false;
    }

    /**
     * Regular state for whether data is valid or not
     * @param isDataValid boolean for whether data is valid or not
     */
    LoginFormState(boolean isDataValid) {
        this.usernameError = null;
        this.passwordError = null;
        this.isDataValid = isDataValid;
    }

    /**
     * Getter for usernameError
     * @return Integer usernameError
     */
    @Nullable
    Integer getUsernameError() {
        return usernameError;
    }

    /**
     * Getter for passwordError
     * @return Integer passwordError
     */
    @Nullable
    Integer getPasswordError() {
        return passwordError;
    }

    /**
     * Getter for isDataValid field
     * @return boolean isDataValid, true if valid
     */
    boolean isDataValid() {
        return isDataValid;
    }
}
