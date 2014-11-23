package com.rob.bryan.steven.hackathon2014.activities;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.rob.bryan.steven.hackathon2014.R;

import io.relayr.LoginEventListener;
import io.relayr.RelayrSdk;

public class LoginActivity extends BaseActivity implements LoginEventListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setActionBarIcon(R.drawable.abc_ic_ab_back_mtrl_am_alpha);

        if (! RelayrSdk.isUserLoggedIn()) {
            RelayrSdk.logIn(this, this);
        }
    }

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_login;
    }

    @Override
    public void onSuccessUserLogIn() {
        Toast.makeText(this, R.string.successfully_logged_in, Toast.LENGTH_SHORT).show();
        finish();
    }

    @Override
    public void onErrorLogin(Throwable throwable) {
        Toast.makeText(this, R.string.unsuccessfully_logged_in, Toast.LENGTH_SHORT).show();
    }
}
