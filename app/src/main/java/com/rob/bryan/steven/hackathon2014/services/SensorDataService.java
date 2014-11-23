package com.rob.bryan.steven.hackathon2014.services;

import android.app.IntentService;
import android.app.Notification;
import android.content.Intent;
import android.content.Context;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;
import com.rob.bryan.steven.hackathon2014.R;
import com.rob.bryan.steven.hackathon2014.object.Alert;
import com.rob.bryan.steven.hackathon2014.utils.AlarmManager;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;
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

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class SensorDataService extends IntentService {
    // TODO: Rename actions, choose action names that describe tasks that this
    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
    private static final String ACTION_SUBSCRIBE = "com.rob.bryan.steven.hackathon2014.services.action.SUBSCIRBE";
    private static final String ACTION_BAZ = "com.rob.bryan.steven.hackathon2014.services.action.BAZ";

    // TODO: Rename parameters
    private static final String EXTRA_PARAM1 = "com.rob.bryan.steven.hackathon2014.services.extra.PARAM1";
    private static final String EXTRA_PARAM2 = "com.rob.bryan.steven.hackathon2014.services.extra.PARAM2";

    private Subscription mWebSocketSubscription, mTemperatureDeviceSubscription;

    /**
     * Starts this service to perform action Foo with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    // TODO: Customize helper method
    public static void startActionSubscribe(Context context) {
        Intent intent = new Intent(context, SensorDataService.class);
        intent.setAction(ACTION_SUBSCRIBE);
        context.startService(intent);
    }

    /**
     * Starts this service to perform action Baz with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    // TODO: Customize helper method
    public static void startActionBaz(Context context, String param1, String param2) {
        Intent intent = new Intent(context, SensorDataService.class);
        intent.setAction(ACTION_BAZ);
        intent.putExtra(EXTRA_PARAM1, param1);
        intent.putExtra(EXTRA_PARAM2, param2);
        context.startService(intent);
    }

    public SensorDataService() {
        super("SensorDataService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d("Service", "handleIntent");
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_SUBSCRIBE.equals(action)) {
                handleActionSubscribe();
            } else if (ACTION_BAZ.equals(action)) {
                final String param1 = intent.getStringExtra(EXTRA_PARAM1);
                final String param2 = intent.getStringExtra(EXTRA_PARAM2);
                handleActionBaz(param1, param2);
            }
        }
    }

    private void handleActionSubscribe() {
        // TODO: Handle action Foo
        //throw new UnsupportedOperationException("Not yet implemented");
        Log.d("Service", "subscribe");
        setupSubscription();
    }

    /**
     * Handle action Baz in the provided background thread with the provided
     * parameters.
     */
    private void handleActionBaz(String param1, String param2) {
        // TODO: Handle action Baz
        throw new UnsupportedOperationException("Not yet implemented");
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
                            if (device.model.equals(DeviceModel.TEMPERATURE_HUMIDITY.getId())
                                    || device.model.equals(DeviceModel.LIGHT_PROX_COLOR.getId())
                                    || device.model.equals(DeviceModel.MICROPHONE)){
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
                            if (device.model.equals(DeviceModel.TEMPERATURE_HUMIDITY.getId())
                                    || device.model.equals(DeviceModel.LIGHT_PROX_COLOR.getId())
                                    || device.model.equals(DeviceModel.MICROPHONE.getId())) {
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
                        Toast.makeText(SensorDataService.this, "PROBLEMZ",
                                Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onNext(TransmitterDevice device) {
                        subscribeForUpdates(device);
                    }
                });
    }

    private void subscribeForUpdates(TransmitterDevice device) {
        mWebSocketSubscription = RelayrSdk.getWebSocketClient()
                .subscribe(device, new Subscriber<Object>() {

                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        Toast.makeText(SensorDataService.this, "A PROBLEM",
                                Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onNext(Object o) {
                        Reading reading = new Gson().fromJson(o.toString(), Reading.class);
                        //Log.d("SensorDataService", reading.temp + "˚C");
                        if (AlarmManager.checkFridgeTemperature(reading.temp, SensorDataService.this)
                                || AlarmManager.checkNoiseLevel(reading.snd_level, SensorDataService.this)
                                || AlarmManager.checkLight(reading.light, SensorDataService.this)
                                || AlarmManager.checkWindowOpen(reading.prox, SensorDataService.this)) {
                            showNotification();
                        }
                    }
                });
    }

    private void showNotification() {

        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setContentTitle(getResources().getString(R.string.notification_title))
                        .setContentText("test");

        ArrayList<Alert> alertList = AlarmManager.getAlertsList(SensorDataService.this);
        ArrayList<Notification> pages = new ArrayList<Notification>();
        for (int i = 0; i < alertList.size(); i++) {
            Alert alert = alertList.get(i);
            NotificationCompat.BigTextStyle pageStyle = new NotificationCompat.BigTextStyle();
            pageStyle.setBigContentTitle(alert.getAlertTypeString())
                    .bigText(alert.getDescription())
                    .build();

            Notification notificationPage =
                    new NotificationCompat.Builder(this)
                            .setStyle(pageStyle)
                            .build();
            pages.add(notificationPage);
        }

        NotificationCompat.WearableExtender wearableExtender = new NotificationCompat.WearableExtender();
        wearableExtender.addPages(pages);


        Notification notification = notificationBuilder.extend(wearableExtender)
                .build();

        int notificationId = 001;

        // Get an instance of the NotificationManager service
        NotificationManagerCompat notificationManager =
                NotificationManagerCompat.from(this);

        // Build the notification and issues it with notification manager.
        notificationManager.notify(notificationId, notification);
    }
}
