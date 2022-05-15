package com.iot.termproject.data;

public class LocationDistance implements Comparable<LocationDistance> {
    private double distance;
    private String location;
    private String name;

    // Constructor
    public LocationDistance(double distance, String location, String name) {
        this.distance = distance;
        this.location = location;
        this.name = name;
    }

    public double getDistance() {
        return distance;
    }

    public String getLocation() {
        return location;
    }

    public String getName() {
        return name;
    }

    @Override
    public int compareTo(LocationDistance locationDistance) {
        double distance = locationDistance.getDistance();

        // 오름차순
        if(this.distance == distance) {
            return 0;
        } else if(this.distance > distance) {
            return 1;
        } else {
            return -1;
        }
    }
}
