package com.example.androidweatherapp.model;

import com.google.gson.annotations.SerializedName;

public class GeoLocation {
    private String name;
    private String state;
    private String country;
    private double lat;
    @SerializedName("lon")
    private double lon;

    public String getName() {
        return name;
    }

    public String getState() {
        return state;
    }

    public String getCountry() {
        return country;
    }

    public double getLat() {
        return lat;
    }

    public double getLon() {
        return lon;
    }
}


