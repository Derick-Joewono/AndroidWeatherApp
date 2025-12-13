package com.example.androidweatherapp.api;

import com.example.androidweatherapp.model.WeatherResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface ApiService {

    @GET("weather")
    Call<WeatherResponse> getWeather(
            @Query("q") String city,
            @Query("appid") String apiKey
    );
}
