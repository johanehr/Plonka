package com.example.plonka.data.model;

/**
 * Data class that captures user information for logged in users retrieved from LoginRepository
 */
public class LoggedInUser {

    private String password; // Keep copy of user-provided password to be used when requesting changes to DB (for authentication)
    private Integer accountId;
    private String displayName;

    /**
     * Constructor for a logged in user, containing a password,
     * unique account id, and name
     * @param account A unique account id, from database
     * @param password  the user's password
     * @param name the user's displayed name
     */
    public LoggedInUser(Integer account, String password, String name) {
        this.accountId = account;
        this.password = password;
        this.displayName = name;
    }

    /**
     * Getter for the user's password
     * @return String password
     */
    public String getPassword() {
        return password;
    }

    /**
     * Getter for the user's password
     * @return String password
     */
    public Integer getAccountId() {
        return accountId;
    }

    /**
     * Getter for the user's password
     * @return String password
     */
    public String getDisplayName() {
        return displayName;
    }
}
