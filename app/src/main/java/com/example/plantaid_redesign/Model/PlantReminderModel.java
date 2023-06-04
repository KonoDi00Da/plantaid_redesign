package com.example.plantaid_redesign.Model;

public class PlantReminderModel {
    public String plantName, reminderType, date, time, userKey, reminderKey, notificationID, requestCode;

    public int water, repot, fertilize, custom;

    public PlantReminderModel(){

    }


    public PlantReminderModel(String plantName, String reminderType, String date, String time, String userKey, String reminderKey, String notificationID, String requestCode, int water, int repot, int fertilize, int custom) {
        this.plantName = plantName;
        this.reminderType = reminderType;
        this.date = date;
        this.time = time;
        this.userKey = userKey;
        this.reminderKey = reminderKey;
        this.notificationID = notificationID;
        this.requestCode = requestCode;
        this.water = water;
        this.repot = repot;
        this.fertilize = fertilize;
        this.custom = custom;
    }

    public int getWater() {
        return water;
    }

    public void setWater(int water) {
        this.water = water;
    }

    public int getRepot() {
        return repot;
    }

    public void setRepot(int repot) {
        this.repot = repot;
    }

    public int getFertilize() {
        return fertilize;
    }

    public void setFertilize(int fertilize) {
        this.fertilize = fertilize;
    }

    public int getCustom() {
        return custom;
    }

    public void setCustom(int custom) {
        this.custom = custom;
    }

    public String getNotificationID() {
        return notificationID;
    }

    public void setNotificationID(String notificationID) {
        this.notificationID = notificationID;
    }

    public String getRequestCode() {
        return requestCode;
    }

    public void setRequestCode(String requestCode) {
        this.requestCode = requestCode;
    }

    public String getUserKey() {
        return userKey;
    }

    public void setUserKey(String userKey) {
        this.userKey = userKey;
    }

    public String getReminderKey() {
        return reminderKey;
    }

    public void setReminderKey(String reminderKey) {
        this.reminderKey = reminderKey;
    }

    public String getPlantName() {
        return plantName;
    }

    public void setPlantName(String plantName) {
        this.plantName = plantName;
    }

    public String getReminderType() {
        return reminderType;
    }

    public void setReminderType(String reminderType) {
        this.reminderType = reminderType;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
