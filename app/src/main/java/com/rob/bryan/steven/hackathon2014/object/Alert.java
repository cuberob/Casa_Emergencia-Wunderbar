package com.rob.bryan.steven.hackathon2014.object;

import android.content.Context;
import android.text.format.DateUtils;

import com.rob.bryan.steven.hackathon2014.R;

import org.json.JSONException;
import org.json.JSONObject;

public class Alert {

    public static final String JSON_NAME_KEY = "name";
    public static final String JSON_TIME_KEY = "time";
    public static final String JSON_TYPE_KEY = "type";
    public static final String JSON_DESCRIPTION_KEY = "description";
    public static final String JSON_PRIORITY_KEY = "priority";
    public static final int HIGH_PRIORITY = 3, MEDIUM_PRIORITY = 2, LOW_PRIORITY = 1;
    private long alertTime;
    private String name, description;
    private int priority;
    private AlertType alertType;

    public enum AlertType {
        TEMPERATURE, SOUND, PROXIMITY, LIGHT, MOVEMENT
    }

    public Alert(String name, AlertType type, int priority){
        this.name = name;
        this.alertType = type;
        this.priority = priority;
        alertTime = System.currentTimeMillis();
    }

    public Alert(String name, AlertType alertType, String description, int priority){
        this.name = name;
        this.alertType = alertType;
        this.description = description;
        this.priority = priority;
        alertTime = System.currentTimeMillis();
    }

    public Alert(JSONObject object){
        try {
            this.name = object.getString(JSON_NAME_KEY);
            this.alertTime = object.getLong(JSON_TIME_KEY);
            this.alertType = getAlertTypeFromString(object.getString(JSON_TYPE_KEY));
            this.priority = object.getInt(JSON_PRIORITY_KEY);
            if(object.has(JSON_DESCRIPTION_KEY)){
                this.description = object.getString(JSON_DESCRIPTION_KEY);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public void setName(String name){
        this.name = name;
    }

    public void setDescription(String description){
        this.description = description;
    }

    public void setAlertTime(long time){
        this.alertTime = time;
    }

    public void setAlertType(AlertType alertType) {
        this.alertType = alertType;
    }

    public long getAlertTime(){
        return alertTime;
    }

    public String getName(){
        return name;
    }

    public String getDescription(){
        if(description == null){
            return "";
        }
        return description;
    }

    public  AlertType getAlertType(){
        return alertType;
    }

    public int getIconID(){
        switch(alertType){
            case TEMPERATURE:
                return R.drawable.temp_icon;
            case MOVEMENT:
                return R.drawable.movement_icon;
            case SOUND:
                return R.drawable.sound_icon;
            case PROXIMITY:
                return R.drawable.proximity_icon;
            case LIGHT:
                return R.drawable.light_icon;
            default:
                return android.R.drawable.btn_star_big_on;
        }
    }

    public static AlertType getAlertTypeFromString(String str){
        if(str.equals("Temperature")){
            return AlertType.TEMPERATURE;
        }
        if(str.equals("Movement")){
            return AlertType.MOVEMENT;
        }
        if(str.equals("Sound")){
            return AlertType.SOUND;
        }
        if(str.equals("Proximity")){
            return AlertType.PROXIMITY;
        }
        if(str.equals("Light")){
            return AlertType.LIGHT;
        }
        return null;
    }

    public String getAlertTypeString(){
        switch (alertType) {
            case TEMPERATURE:
                return "Temperature";
            case MOVEMENT:
                return "Movement";
            case SOUND:
                return "Sound";
            case PROXIMITY:
                return "Proximity";
            case LIGHT:
                return "Light";
            default:
                return "None";
        }
    }

    public JSONObject getJSONObject(){
        JSONObject result = new JSONObject();
        try {
            result.put(JSON_NAME_KEY, name);
            if(description != null) {
                result.put(JSON_DESCRIPTION_KEY, description);
            }
            result.put(JSON_PRIORITY_KEY, priority);
            result.put(JSON_TYPE_KEY, getAlertTypeString());
            result.put(JSON_TIME_KEY, alertTime);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return result;
    }

    public int getPriorityColor(){
        switch(priority){
            case(LOW_PRIORITY):
                return R.color.yellow_600;
            case(MEDIUM_PRIORITY):
                return R.color.orange_600;
            case(HIGH_PRIORITY):
                return R.color.red_600;
            default:
                return R.color.red_600;
        }
    }

}
