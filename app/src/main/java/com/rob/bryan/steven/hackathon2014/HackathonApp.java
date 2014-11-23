package com.rob.bryan.steven.hackathon2014;

import android.app.Application;

import io.relayr.RelayrSdk;

/**
 * Created by steven on 23/11/14.
 */
public class HackathonApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        RelayrSdk.init(this);
    }

}
