package com.inkhornsolutions.riderapp.EventBus;

public class DriverRequestRecieved {
    private String key;
    private String pickupLocation, pickupLocationString;
    private String destinationLocation, destinationLocationString;

    public DriverRequestRecieved() {
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getPickupLocation() {
        return pickupLocation;
    }

    public void setPickupLocation(String pickupLocation) {
        this.pickupLocation = pickupLocation;
    }

    public String getPickupLocationString() {
        return pickupLocationString;
    }

    public void setPickupLocationString(String pickupLocationString) {
        this.pickupLocationString = pickupLocationString;
    }

    public String getDestinationLocation() {
        return destinationLocation;
    }

    public void setDestinationLocation(String destinationLocation) {
        this.destinationLocation = destinationLocation;
    }

    public String getDestinationLocationString() {
        return destinationLocationString;
    }

    public void setDestinationLocationString(String destinationLocationString) {
        this.destinationLocationString = destinationLocationString;
    }
}
