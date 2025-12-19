package com.example.androidweatherapp.model;

import com.google.gson.annotations.SerializedName;

public class Main {
    private double temp;
    private int humidity;
    @SerializedName("feels_like")
    private double feelsLike;

    public double getTemp() {
        return temp;
    }

    public int getHumidity() {
        return humidity;
    }

    public double getFeelsLike() {
        return feelsLike;
    }
}
