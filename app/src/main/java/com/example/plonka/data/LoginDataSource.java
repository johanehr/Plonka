package com.example.plonka.data;

import android.util.Log;

import com.example.plonka.AsyncLoginTask;
import com.example.plonka.data.model.LoggedInUser;

import java.io.IOException;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * Class that handles authentication w/ login credentials and retrieves user information.
 */
public class LoginDataSource {

    private AsyncLoginTask loginTask;
    private String[] userDetails = {"Not logged in"};
    private String LOG_TAG = "PLONKA_LOGINDATASOURCE";

    public Result<LoggedInUser> login(long provided_user, String provided_password) {
        try {
            // Handle loggedInUser authentication with MySQL database
            loginTask = new AsyncLoginTask(new AsyncLoginTask.AsyncResponse(){
                @Override
                public void processFinish(String[] output){
                    Log.d(LOG_TAG, "processFinish() called");
                }
            });

            Log.d(LOG_TAG, "About to call execute on AsyncLoginTask");
            // Use .get() to wait for async to finish (bad code, but PHP request seems to require async...?)
            userDetails = loginTask.execute(Long.toString(provided_user), provided_password).get(); // https://stackoverflow.com/questions/12575068/how-to-get-the-result-of-onpostexecute-to-main-activity-because-asynctask-is-a
            Log.d(LOG_TAG, "Finished execute of AsyncLoginTask");

            // Check whether log in worked
            if (userDetails.length != 1){
                Integer accountId = Integer.valueOf(userDetails[0]);
                String userName = userDetails[1];
                Log.v(LOG_TAG, "Log in successful [account: "+userDetails[0]+", name: "+userName+"]");
                LoggedInUser currentUser = new LoggedInUser(accountId, userName);
                return new Result.Success<>(currentUser);
            }
            else {
                // Log in failed, show error message to user
                Log.d(LOG_TAG, "Log in failed: ["+userDetails[0]+"]");
                return new Result.Error(new Exception(userDetails[0]));
            }
        } catch (Exception e) {
            return new Result.Error(e);
        }
    }

    public void logout() {
        // TODO: Revoke authentication

    }
}
