package com.example.plonka.ui.login;

/**
 * Class exposing authenticated user details to the UI.
 * This class was partially generated by using the provided File > New > Activity > Login Activity in Android Studio
 */
class LoggedInUserView {
    private String displayName;
    //... other data fields that may be accessible to the UI

    /**
     * Constructor with displayName
     * @param displayName user's name
     */
    LoggedInUserView(String displayName) {
        this.displayName = displayName;
    }

    /**
     * Return the display name
     * @return String of displayName
     */
    String getDisplayName() {
        return displayName;
    }
}
