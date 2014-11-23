package com.rob.bryan.steven.hackathon2014.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.rob.bryan.steven.hackathon2014.object.Alert;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by robdeknegt on 23/11/14.
 */
public class AlarmManager {

    public static final String ALERTS_SP_KEY = "com.steven.bryan.rob.ALERTS_SP_KEY";
    private static String TAG = "AlarmManager";

    public static void checkFridgeTemperature(double temp, Context context){
        Alert alert;
        if(temp < 3){
            //Fridge cool mode is set to high
            alert = new Alert("Fridge", Alert.AlertType.TEMPERATURE, "Fridge is too cold", Alert.MEDIUM_PRIORITY);
        }else if(temp > 11){
            //Fridge it too warm (open/defect?)
            alert = new Alert("Fridge", Alert.AlertType.TEMPERATURE, "Fridge is too warm", Alert.HIGH_PRIORITY);
        }else{
            //No Problem
            return;
        }

        JSONArray mAlertsJSONArray = getAlertsJSONArray(context);
        JSONObject mJSONObject;

        //Loop through alerts to see if a temperature alert already exists.
        for(int i = 0; i < mAlertsJSONArray.length(); i++){
            try {
                mJSONObject = (JSONObject) mAlertsJSONArray.get(i);
                Alert mTempAlert = new Alert(mJSONObject);
                if(mTempAlert.getAlertType() == Alert.AlertType.TEMPERATURE){
                    //Temperature alert already exists, delete so we can re-add later
                    mAlertsJSONArray.remove(i); //TODO: Fix so it supports API < 19
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        //Add the Alert as JSONObject to the array
        mAlertsJSONArray.put(alert.getJSONObject());

        updateSettings(context, mAlertsJSONArray);
    }

    public static void checkNoiseLevel(int level, Context context){
        Alert alert;
        if(level > 768){
            alert = new Alert("Sound", Alert.AlertType.SOUND, "The room is very loud", Alert.HIGH_PRIORITY);
        }else if(level > 512){
            alert = new Alert("Sound", Alert.AlertType.SOUND, "The room is noisy", Alert.MEDIUM_PRIORITY);
        }else if(level > 256){
            alert = new Alert("Sound", Alert.AlertType.SOUND, "The room is mildly noisy", Alert.LOW_PRIORITY);
        }else{
            return;
        }

        JSONArray mAlertsJSONArray = getAlertsJSONArray(context);
        JSONObject mJSONObject;

        //Loop through alerts to see if a temperature alert already exists.
        for(int i = 0; i < mAlertsJSONArray.length(); i++){
            try {
                mJSONObject = (JSONObject) mAlertsJSONArray.get(i);
                Alert mTempAlert = new Alert(mJSONObject);
                if(mTempAlert.getAlertType() == Alert.AlertType.SOUND){
                    //Temperature alert already exists, delete so we can re-add later
                    mAlertsJSONArray.remove(i); //TODO: Fix so it supports API < 19
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        //Add the Alert as JSONObject to the array
        mAlertsJSONArray.put(alert.getJSONObject());

        updateSettings(context, mAlertsJSONArray);
    }

    /**
     * Deletes the alert from the sharedPrefs
     * @param type
     * @param context
     */
    public static void markAsDone(Alert.AlertType type, Context context){

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
}
