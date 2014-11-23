package com.rob.bryan.steven.hackathon2014.object;

import android.content.Context;
import android.text.format.DateUtils;

import com.rob.bryan.steven.hackathon2014.R;

/**
 * Created by robdeknegt on 23/11/14.
 */
public class Alert {

    private long alertTime;
    private String name, description;
    private AlertType alertType;

    public enum AlertType {
        TEMPERATURE, SOUND, PROXIMITY, LIGHT, PRESSURE, MOVEMENT
    }

    public Alert(String name, AlertType type){
        this.name = name;
        this.alertType = type;
        alertTime = System.currentTimeMillis();
    }

    public Alert(String name, AlertType alertType, String description){
        this.name = name;
        this.alertType = alertType;
        this.description = description;
        alertTime = System.currentTimeMillis();
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
            case PRESSURE:
                return R.drawable.pressure_icon;
            default:
                return android.R.drawable.btn_star_big_on;
        }
    }

}
