package com.agrialert.api;

import android.util.Pair;

import com.agrialert.BuildConfig;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiManager {
    private static final String weatherBaseUrl = "https://api.open-meteo.com/v1/";
    private static final String geoBaseUrl = "https://maps.googleapis.com/maps/api/";
    private static final WeatherService  weatherService = new Retrofit.Builder()
            .baseUrl(weatherBaseUrl)
                .addConverterFactory(GsonConverterFactory.create())
            .build().create(WeatherService.class);
    private static final GeoService geoService = new Retrofit.Builder()
            .baseUrl(geoBaseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .build().create(GeoService.class);

    private ApiManager() {}

    // TODO: move open-meteo api calls from alertManager to ApiManager
    public static WeatherResponse getWeatherForecast(double lat, double lon) throws IOException {
        Call<WeatherResponse> call = weatherService.getCurrentWeather(lat, lon, "temperature_2m, relativehumidity_2m, windspeed_10m, weathercode");
        return call.execute().body();
    }

    public static Pair<Double, Double> getCoordinatesFromAddress(String address) throws IOException {
        Call<GeoResponse> call = geoService.getCoordinates(address, BuildConfig.MAPS_API_KEY);
        GeoResponse response = call.execute().body();
        if (response != null && Objects.equals(response.status, "OK")) {
            return new Pair<>(response.result.geometry.location.lat, response.result.geometry.location.lng);
        } else {
            throw new IOException("Error getting coordinates");
        }
    }
}