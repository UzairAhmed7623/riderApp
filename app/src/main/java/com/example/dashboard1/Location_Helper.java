package com.example.dashboard1;

import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

public class Location_Helper {
    private double Longitude;
    private double Latitude;


    public Location_Helper(double longitude, double latitude) {
        Longitude = longitude;
        Latitude = latitude;
    }

    public double getLongitude() {
        return Longitude;
    }

    public void setLongitude(double longitude) {
        Longitude = longitude;
    }

    public double getLatitude() {
        return Latitude;
    }

    public void setLatitude(double latitude) {
        Latitude = latitude;
    }

}
