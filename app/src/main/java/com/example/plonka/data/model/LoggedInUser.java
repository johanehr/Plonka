package com.example.plonka.data.model;

/**
 * Data class that captures user information for logged in users retrieved from LoginRepository
 */
public class LoggedInUser {

    //private long personalNumber;
    private Integer accountId;
    private String displayName;

    public LoggedInUser(/*long user, */Integer account, String name) {
        //this.personalNumber = user;
        this.accountId = account;
        this.displayName = name;
    }

    /*
    public long getPersonalNumber() {
        return personalNumber;
    }
    */

    public Integer getAccountId() {
        return accountId;
    }

    public String getDisplayName() {
        return displayName;
    }
}
