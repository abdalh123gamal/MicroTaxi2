package com.abdalh.microtaxi.ui.HistoryRecycleView;

public class HistoryModel {

    private String rideId;
    private String time;

    public HistoryModel(String rideId, String time) {
        this.rideId = rideId;
        this.time = time;
    }

    public String getRideId() {
        return rideId;
    }

    public void setRideId(String rideId) {
        this.rideId = rideId;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
