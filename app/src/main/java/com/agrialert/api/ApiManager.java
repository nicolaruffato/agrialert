package com.agrialert.api;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiManager {
    // Dipende se vogliamo avere istanza o fare solamente un classe statica
    private static ApiManager instance;
    private final WeatherService weatherService;
    private final GeoService geoService;

    private static final String weatherBaseUrl = "https://api.open-meteo.com/v1/";
    private static final String geoBaseUrl = "https://maps.googleapis.com/maps/api/";

    private ApiManager() {
        Retrofit weatherRetrofit = new Retrofit.Builder()
                .baseUrl(weatherBaseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        Retrofit geoRetrofit = new Retrofit.Builder()
                .baseUrl(geoBaseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        weatherService = weatherRetrofit.create(WeatherService.class);
        geoService = geoRetrofit.create(GeoService.class);
    }

    public static synchronized ApiManager getInstance() {
        if (instance == null) {
            instance = new ApiManager();
        }
        return instance;
    }

    // TODO: gestire eccezioni lato ApiManager
    public WeatherResponse getWeatherForecast(double lat, double lon) throws IOException {
        Call<WeatherResponse> call = weatherService.getCurrentWeather(lat, lon, "temperature_2m, relativehumidity_2m, windspeed_10m, weathercode");
        return call.execute().body();
    }
    public GeoResponse getCoordinatesFromAddress(String address) throws IOException {
        Call<GeoResponse> call = geoService.getCoordinates(address, "API_KEY");
        return call.execute().body();
    }

    // Non serve se utilizziamo cifratura del database room?
    public void encryptFieldData() {

    }
}