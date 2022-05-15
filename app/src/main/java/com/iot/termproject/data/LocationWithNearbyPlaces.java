package com.iot.termproject.data;

import java.util.ArrayList;

public class LocationWithNearbyPlaces {
    private String location;
    private ArrayList<LocationDistance> places;

    // Constructor
    public LocationWithNearbyPlaces(String location, ArrayList<LocationDistance> places) {
        this.location = location;
        this.places = places;
    }

    public String getLocation() {
        return location;
    }

    public ArrayList<LocationDistance> getPlaces() {
        return places;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setPlaces(ArrayList<LocationDistance> places) {
        this.places = places;
    }
}
