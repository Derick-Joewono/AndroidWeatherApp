package com.example.androidweatherapp.api;

import com.example.androidweatherapp.model.GeoLocation;
import com.example.androidweatherapp.model.WeatherResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface ApiService {

    @GET("data/2.5/weather")
    Call<WeatherResponse> getWeather(
            @Query("q") String city,
            @Query("appid") String apiKey
    );

    @GET("geo/1.0/direct")
    Call<List<GeoLocation>> searchCity(
            @Query("q") String query,
            @Query("limit") int limit,
            @Query("appid") String apiKey
    );
}
