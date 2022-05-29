package com.iot.termproject.data.entity;

public class LocationDistance implements Comparable<LocationDistance> {
    private double distance;
    private double latitude;
    private double longitude;
    private String name;

    // Constructor
    public LocationDistance(double distance, double latitude, double longitude, String name) {
        this.distance = distance;
        this.latitude = latitude;
        this.longitude = longitude;
        this.name = name;
    }

    public double getDistance() {
        return distance;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public String getName() {
        return name;
    }

    @Override
    public int compareTo(LocationDistance locationDistance) {
        double distance = locationDistance.getDistance();

        // 오름차순
        if (this.distance == distance) {
            return 0;
        } else if (this.distance > distance) {
            return 1;
        } else {
            return -1;
        }
    }
}
