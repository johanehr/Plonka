package com.example.plonka.data;

import com.example.plonka.data.model.LoggedInUser;

/**
 * Class that requests authentication and user information from the remote data source and
 * maintains an in-memory cache of login status and user credentials information. This class was
 * generated as part of the provided File > New > Activity > Login Activity in Android Studio
 */
public class LoginRepository {

    private static volatile LoginRepository instance;

    private LoginDataSource dataSource;

    // If user credentials will be cached in local storage, it is recommended it be encrypted
    // @see https://developer.android.com/training/articles/keystore
    private LoggedInUser user = null;

    // private constructor : singleton access
    private LoginRepository(LoginDataSource dataSource) {
        this.dataSource = dataSource;
    }


    public static LoginRepository getInstance(LoginDataSource dataSource) {
        if (instance == null) {
            instance = new LoginRepository(dataSource);
        }
        return instance;
    }

    /**
     * Check whether a user is logged in
     * @return boolean whether a logged in user is present or not
     */
    public boolean isLoggedIn() {
        return user != null;
    }

    /**
     * Log out the current user by removing currently logged in user
     * @return void
     */
    public void logout() {
        user = null;
        dataSource.logout();
    }

    /**
     * Set a new logged in user
     * @param user the user to set as being logged in
     * @return void
     */
    private void setLoggedInUser(LoggedInUser user) {
        this.user = user;
        // If user credentials will be cached in local storage, it is recommended it be encrypted
        // @see https://developer.android.com/training/articles/keystore
    }

    /**
     * Authenicate user with provided email and password
     * @return Result<LoggedInUser> the logged in user
     */
    public Result<LoggedInUser> login(Long user, String password) {
        // handle login
        Result<LoggedInUser> result = dataSource.login(user, password);
        if (result instanceof Result.Success) {
            setLoggedInUser(((Result.Success<LoggedInUser>) result).getData());
        }
        return result;
    }
}
