package com.agrialert.api;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Retrofit service interface for interacting with the Open-Meteo Weather API.
 * Provides methods to retrieve meteorological forecasts and current weather conditions
 * based on geographic coordinates.
 */
public interface OpenMeteoService {

    /**
     * Fetches weather forecast data from the Open-Meteo API.
     *
     * @param latitude Geographical WGS84 coordinate of the location.
     * @param longitude Geographical WGS84 coordinate of the location.
     * @param currentWeather Whether to include current weather conditions in the response.
     * @param hourlyParams A comma-separated list of hourly weather variables to retrieve.
     * @param forecastDays Number of days for which the forecast is requested.
     * @return A {@link Call} object for the weather API response.
     */
    @GET("v1/forecast")
    Call<WeatherApiResponse> getForecast(
            @Query("latitude") double latitude,
            @Query("longitude") double longitude,
            @Query("current_weather") boolean currentWeather,
            @Query("hourly") String hourlyParams,
            @Query("forecast_days") int forecastDays
    );
}
