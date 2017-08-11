package com.innovify.vehicletracker.models;

import java.io.Serializable;

/**
 * Created by Akshay.Panchal on 11-Aug-17.
 */

public class LocationModel implements Serializable {
    private int id;
    private String timeAndDate;
    private double latitude;
    private double longitude;
    private int currentFrequency;
    private int nextFrequency;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTimeAndDate() {
        return timeAndDate;
    }

    public void setTimeAndDate(String timeAndDate) {
        this.timeAndDate = timeAndDate;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public int getCurrentFrequency() {
        return currentFrequency;
    }

    public void setCurrentFrequency(int currentFrequency) {
        this.currentFrequency = currentFrequency;
    }

    public int getNextFrequency() {
        return nextFrequency;
    }

    public void setNextFrequency(int nextFrequency) {
        this.nextFrequency = nextFrequency;
    }
}
