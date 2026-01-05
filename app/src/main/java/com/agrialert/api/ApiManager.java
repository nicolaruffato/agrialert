package com.agrialert.api;

import android.util.Log;
import android.util.Pair;

import com.agrialert.BuildConfig;

import java.io.IOException;
import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiManager {
    private static final String weatherBaseUrl = "https://api.open-meteo.com/v1/";
    private static final String geoBaseUrl = "https://api.mapbox.com/geocoding/v5/"; // Corrected Base URL
    private static final WeatherService weatherService = new Retrofit.Builder()
            .baseUrl(weatherBaseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .build().create(WeatherService.class);
    private static final GeoService geoService = new Retrofit.Builder()
            .baseUrl(geoBaseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .build().create(GeoService.class);

    private ApiManager() {}

    public static WeatherResponse getWeatherForecast(double lat, double lon) throws IOException {
        Call<WeatherResponse> call = weatherService.getCurrentWeather(lat, lon, "temperature_2m,relativehumidity_2m,windspeed_10m,weathercode");
        return call.execute().body();
    }

    public static Single<Pair<Double, Double>> getCoordinatesFromAddress(String address) throws IOException {
        return Single.fromCallable(() -> {

            Call<MapboxGeocodingResponse> call = geoService.getCoordinates(address, BuildConfig.MAPBOX_API_KEY);
            MapboxGeocodingResponse response = call.execute().body();

            if (response != null && response.features != null && !response.features.isEmpty()) {
                List<Double> coords = response.features.get(0).geometry.coordinates;
                // Mapbox returns [longitude, latitude]
                return new Pair<>(coords.get(1), coords.get(0)); // Return as (lat, lon)
            } else {
                throw new IOException("Error getting coordinates or no results found");
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    public static Single<String> getAddressFromCoordinates(double lat, double lon) throws IOException {
        return Single.fromCallable(() -> {

            Call<MapboxGeocodingResponse> call = geoService.getAddress(lon, lat, BuildConfig.MAPBOX_API_KEY); // Note: lon, lat order
            MapboxGeocodingResponse response = call.execute().body();

            if (response != null && response.features != null && !response.features.isEmpty()) {
                return response.features.get(0).placeName;
            } else {
                throw new IOException("Error getting address or no results found");
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }
}
