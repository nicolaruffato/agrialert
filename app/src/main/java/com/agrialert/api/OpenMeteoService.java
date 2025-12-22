package com.agrialert.api;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface OpenMeteoService {

    @GET("v1/forecast")
    Call<WeatherApiResponse> getForecast(
            @Query("latitude") double latitude,
            @Query("longitude") double longitude,
            @Query("current_weather") boolean currentWeather,
            @Query("hourly") String hourlyParams,
            @Query("forecast_days") int forecastDays
    );
}
