package com.example.plonka.data.model;

/**
 * Data class that captures user information for logged in users retrieved from LoginRepository
 */
public class LoggedInUser {

    private String password; // Keep copy of provided password for additional security when requesting changes to DB
    private Integer accountId;
    private String displayName;

    public LoggedInUser(Integer account, String password, String name) {
        this.accountId = account;
        this.password = password;
        this.displayName = name;
    }

    public String getPassword() {
        return password;
    }

    public Integer getAccountId() {
        return accountId;
    }

    public String getDisplayName() {
        return displayName;
    }
}
