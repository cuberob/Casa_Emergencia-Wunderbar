package com.rob.bryan.steven.hackathon2014.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.rob.bryan.steven.hackathon2014.object.Alert;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Array;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import de.greenrobot.event.EventBus;
import io.relayr.model.AccelGyroscope;

/**
 * Created by robdeknegt on 23/11/14.
 */
public class AlarmManager {

    public static final String ALERTS_SP_KEY = "com.steven.bryan.rob.ALERTS_SP_KEY";
    private static String TAG = "AlarmManager";

    public static boolean checkFridgeTemperature(double temp, Context context){
        Alert alert;
        if(temp < 3){
            //Fridge cool mode is set to high
            alert = new Alert("Fridge", Alert.AlertType.TEMPERATURE, "The fridge is too cold. (" + (int)temp + "˚C)", Alert.MEDIUM_PRIORITY);
        }else if(temp > 31){ //TODO: Change to 11, this is just for the demo
            //Fridge it too warm (open/defect?)
            alert = new Alert("Fridge", Alert.AlertType.TEMPERATURE, "The fridge is too warm. (" + (int)temp + "˚C)", Alert.HIGH_PRIORITY);
        }else{
            //No Problem
            return false;
        }

        markAsDone(Alert.AlertType.TEMPERATURE, context); //Remove previous entry

        JSONArray mAlertsJSONArray = getAlertsJSONArray(context);
        //Add the Alert as JSONObject to the array
        mAlertsJSONArray.put(alert.getJSONObject());
        updateSettings(context, mAlertsJSONArray);

        EventBus.getDefault().post(alert);
        return true;
    }

    public static boolean checkNoiseLevel(float level, Context context){
        Alert alert;
        if(level > 768){
            alert = new Alert("Sound", Alert.AlertType.SOUND, "The room is very loud.", Alert.HIGH_PRIORITY);
        }else if(level > 512){
            alert = new Alert("Sound", Alert.AlertType.SOUND, "The room is noisy.", Alert.MEDIUM_PRIORITY);
        }else if(level > 256){
            alert = new Alert("Sound", Alert.AlertType.SOUND, "The room is mildly noisy.", Alert.LOW_PRIORITY);
        }else{
            return false;
        }

        markAsDone(Alert.AlertType.SOUND, context); //Remove previous entry

        JSONArray mAlertsJSONArray = getAlertsJSONArray(context);
        //Add the Alert as JSONObject to the array
        mAlertsJSONArray.put(alert.getJSONObject());
        updateSettings(context, mAlertsJSONArray);

        EventBus.getDefault().post(alert);
        return true;
    }

    public static boolean checkWindowOpen(float distance, Context context){
        Alert alert;
        if(distance < 1900){
            alert = new Alert("Window", Alert.AlertType.PROXIMITY, "The window is open.", Alert.LOW_PRIORITY);
        }else{
            return false;
        }

        markAsDone(Alert.AlertType.PROXIMITY, context); //Remove previous entry

        JSONArray mAlertsJSONArray = getAlertsJSONArray(context);
        //Add the Alert as JSONObject to the array
        mAlertsJSONArray.put(alert.getJSONObject());
        updateSettings(context, mAlertsJSONArray);

        EventBus.getDefault().post(alert);
        return true;
    }

    public static boolean checkLight(float level, Context context){
        Alert alert;
        if(level > 100){
            alert = new Alert("Light", Alert.AlertType.LIGHT, "The light is turned on.", Alert.LOW_PRIORITY);
        }else{
            return false;
        }

        markAsDone(Alert.AlertType.LIGHT, context); //Remove previous entry

        JSONArray mAlertsJSONArray = getAlertsJSONArray(context);
        //Add the Alert as JSONObject to the array
        mAlertsJSONArray.put(alert.getJSONObject());
        updateSettings(context, mAlertsJSONArray);

        EventBus.getDefault().post(alert);
        return true;
    }

    public static boolean checkIfGettingLaid(AccelGyroscope.Accelerometer accelerometer, Context context){
        Alert alert;
        if(accelerometer.x > 0.1 && accelerometer.y > 0.1){
            alert = new Alert("F$#&!", Alert.AlertType.MOVEMENT, "Someone is getting laid...", Alert.XXX_PRIORITY);
        }else{
            return false;
        }

        markAsDone(Alert.AlertType.MOVEMENT, context); //Remove previous entry

        JSONArray mAlertsJSONArray = getAlertsJSONArray(context);
        //Add the Alert as JSONObject to the array
        mAlertsJSONArray.put(alert.getJSONObject());
        updateSettings(context, mAlertsJSONArray);

        EventBus.getDefault().post(alert);
        return true;
    }

    /**
     * Deletes the alert from the sharedPrefs
     * @param alertType alertType to be removed
     * @param context required to access SharedPrefs
     */
    public static void markAsDone(Alert.AlertType alertType, Context context){
        JSONArray mAlertsJSONArray = getAlertsJSONArray(context);
        JSONObject mJSONObject;

        //Loop through alerts to see if a alertType alert already exists.
        for(int i = 0; i < mAlertsJSONArray.length(); i++){
            try {
                mJSONObject = mAlertsJSONArray.getJSONObject(i);
                Alert mTempAlert = new Alert(mJSONObject);
                if(mTempAlert.getAlertType() == alertType){
                    //Temperature alert already exists, delete so we can re-add later
                    mAlertsJSONArray.remove(i); //TODO: Fix so it supports API < 19
                    break; //As there can only be one entry of this alertType, break to skip other checks
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        updateSettings(context, mAlertsJSONArray);
    }


    public static JSONArray getAlertsJSONArray(Context context){
        SharedPreferences sharedPrefs = PreferenceManager
                .getDefaultSharedPreferences(context);
        String JSONString = sharedPrefs.getString(ALERTS_SP_KEY, "");
        JSONArray result = null;

        try {
            result = new JSONArray(JSONString);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        if(result == null){
            return new JSONArray();
        }

        return result;
    }

    private static void updateSettings(Context context, JSONArray array){
        //Save the new AlertsArray to sharedPrefs
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        sharedPrefs.edit().putString(ALERTS_SP_KEY, array.toString()).commit();
    }

    public static ArrayList<Alert> getAlertsList(Context context){
        ArrayList<Alert> result = new ArrayList<Alert>();
        JSONArray mArray = getAlertsJSONArray(context);
        for(int i = 0; i < mArray.length(); i++){
            try {
                result.add(new Alert(mArray.getJSONObject(i)));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

}
