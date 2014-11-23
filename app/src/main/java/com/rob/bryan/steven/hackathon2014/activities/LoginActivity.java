package com.rob.bryan.steven.hackathon2014.activities;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.rob.bryan.steven.hackathon2014.R;

import io.relayr.LoginEventListener;
import io.relayr.RelayrSdk;

/**
 * Created by steven on 23/11/14.
 */
public class LoginActivity extends BaseActivity implements LoginEventListener {

    private MenuItem mLogIn;
    private MenuItem mLogOut;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setActionBarIcon(R.drawable.abc_ic_ab_back_mtrl_am_alpha);

        if (!RelayrSdk.isUserLoggedIn()) {

            //if the user isn't logged in, we call the logIn method
            RelayrSdk.logIn(this, this);
        }
    }

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_login;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_login, menu);

        mLogIn = menu.findItem(R.id.action_log_in);
        mLogOut = menu.findItem(R.id.action_log_out);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {
            case R.id.action_log_in:
                RelayrSdk.logIn(this, this);
                return true;
            case R.id.action_log_out:
                logOut();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSuccessUserLogIn() {
        mLogOut.setVisible(true);
        mLogIn.setVisible(false);
        Toast.makeText(this, R.string.successfully_logged_in, Toast.LENGTH_SHORT).show();
        finish();
    }

    @Override
    public void onErrorLogin(Throwable throwable) {
        Toast.makeText(this, R.string.unsuccessfully_logged_in, Toast.LENGTH_SHORT).show();
    }

    private void logOut() {

        //call the logOut method on the reayr SDK
        RelayrSdk.logOut();

        mLogOut.setVisible(false);
        mLogIn.setVisible(true);

        //use the Toast library to display a message to the user
        Toast.makeText(this, R.string.successfully_logged_out, Toast.LENGTH_SHORT).show();
    }
}
