package com.rob.bryan.steven.hackathon2014.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.rob.bryan.steven.hackathon2014.R;

import io.relayr.RelayrSdk;
import rx.Subscription;

public class MainActivity extends BaseActivity {

    private String TAG = "MainActivity";
    private Subscription mWebSocketSubscription, mTemperatureDeviceSubscription;
    private MenuItem mLogIn;
    private MenuItem mLogOut;
    private int loginResultCode = 1337;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setActionBarIcon(R.drawable.abc_ic_ab_back_mtrl_am_alpha);

        login();
    }


    private void login() {
        if (!RelayrSdk.isUserLoggedIn()) {
            startActivityForResult(new Intent(this, LoginActivity.class), loginResultCode);
        }
        else {
            startActivity(new Intent(this, AlertsActivity.class));
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == loginResultCode){
            login();
        }
    }

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_main;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        mLogIn = menu.findItem(R.id.action_log_in);
        mLogOut = menu.findItem(R.id.action_log_out);

        if (RelayrSdk.isUserLoggedIn()) {
            mLogOut.setVisible(true);
            mLogIn.setVisible(false);
        } else {
            mLogOut.setVisible(false);
            mLogIn.setVisible(true);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {
            case R.id.action_settings:
                return true;
            case R.id.action_log_in:
                startActivity(new Intent(this, LoginActivity.class));
                return true;
            case R.id.action_log_out:
                logOut();
                return true;
        }

        return super.onOptionsItemSelected(item);
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
