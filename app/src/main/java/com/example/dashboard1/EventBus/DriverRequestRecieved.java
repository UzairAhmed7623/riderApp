package com.example.dashboard1.EventBus;

public class DriverRequestRecieved {
    private String key;
    private String pickupLocation;

    public DriverRequestRecieved(String key, String pickupLocation) {
        this.key = key;
        this.pickupLocation = pickupLocation;
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
}
