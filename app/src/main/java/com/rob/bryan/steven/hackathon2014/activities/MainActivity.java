package com.rob.bryan.steven.hackathon2014.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.gson.Gson;
import com.rob.bryan.steven.hackathon2014.R;

import java.util.ArrayList;
import java.util.List;

import io.relayr.RelayrSdk;
import io.relayr.model.DeviceModel;
import io.relayr.model.Reading;
import io.relayr.model.Transmitter;
import io.relayr.model.TransmitterDevice;
import io.relayr.model.User;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;


public class MainActivity extends BaseActivity {

    private String TAG = "MainActivity";
    private Subscription mWebSocketSubscription, mTemperatureDeviceSubscription;
    private MenuItem mLogIn;
    private MenuItem mLogOut;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setActionBarIcon(R.drawable.abc_ic_ab_back_mtrl_am_alpha);

        if (!RelayrSdk.isUserLoggedIn()) {
            startActivity(new Intent(this, LoginActivity.class));
        }
        setupSubscription();

    }

    private void setupSubscription(){
        mTemperatureDeviceSubscription = RelayrSdk.getRelayrApi()
                .getUserInfo()
                .flatMap(new Func1<User, Observable<List<Transmitter>>>() {
                    @Override
                    public Observable<List<Transmitter>> call(User user) {
                        return RelayrSdk.getRelayrApi().getTransmitters(user.id);
                    }
                })
                .flatMap(new Func1<List<Transmitter>, Observable<List<TransmitterDevice>>>() {
                    @Override
                    public Observable<List<TransmitterDevice>> call(List<Transmitter> transmitters) {
                        // This is a naive implementation. Users may own multiple WunderBars or different
                        // kinds of transmitters.
                        if (transmitters.isEmpty())
                            return Observable.from(new ArrayList<List<TransmitterDevice>>());
                        return RelayrSdk.getRelayrApi().getTransmitterDevices(transmitters.get(0).id);
                    }
                })
                .filter(new Func1<List<TransmitterDevice>, Boolean>() {
                    @Override
                    public Boolean call(List<TransmitterDevice> devices) {
                        // Check whether there is a thermometer among the devices listed under the transmitter.
                        for (TransmitterDevice device : devices) {
                            if (device.model.equals(DeviceModel.TEMPERATURE_HUMIDITY.getId())) {
                                return true;
                            }
                        }
                        return false;
                    }
                })
                .flatMap(new Func1<List<TransmitterDevice>, Observable<TransmitterDevice>>() {
                    @Override
                    public Observable<TransmitterDevice> call(List<TransmitterDevice> devices) {
                        for (TransmitterDevice device : devices) {
                            if (device.model.equals(DeviceModel.TEMPERATURE_HUMIDITY.getId())) {
                                return Observable.just(device);
                            }
                        }
                        return null;
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<TransmitterDevice>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        Toast.makeText(MainActivity.this, "PROBLEMZ",
                                Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onNext(TransmitterDevice device) {
                        subscribeForTemperatureUpdates(device);
                    }
                });
    }

    private void subscribeForTemperatureUpdates(TransmitterDevice device) {
        mWebSocketSubscription = RelayrSdk.getWebSocketClient()
                .subscribe(device, new Subscriber<Object>() {

                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        Toast.makeText(MainActivity.this, "A PROBLEM",
                                Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onNext(Object o) {
                        Reading reading = new Gson().fromJson(o.toString(), Reading.class);
                        Log.d(TAG, reading.temp + "˚C");
                    }
                });
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
