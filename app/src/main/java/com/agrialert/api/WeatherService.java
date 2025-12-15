package com.agrialert.api;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface WeatherService {
    @GET("forecast")
    Call<WeatherResponse> getCurrentWeather(
        @Query("latitude") double latitude,
        @Query("longitude") double longitude,
        @Query("hourly") String hourly
    );
}