package com.rob.bryan.steven.hackathon2014.object;

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

    Alert(String name, AlertType type){
        this.name = name;
        this.alertType = type;
        alertTime = System.currentTimeMillis();
    }

    Alert(String name, AlertType alertType, String description){
        this.name = name;
        this.alertType = alertType;
        this.description = description;
        alertTime = System.currentTimeMillis();
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

}
