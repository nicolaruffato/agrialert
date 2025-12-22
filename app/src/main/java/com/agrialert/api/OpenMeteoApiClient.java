package com.agrialert.api;

import android.util.Log;

import okhttp3.OkHttpClient;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Client HTTP per interrogare Open-Meteo.
 */
public class OpenMeteoApiClient {

    private static final String TAG = "OpenMeteoApiClient";
    private static final String BASE_URL = "https://api.open-meteo.com/";
    private final OpenMeteoService service;

    public OpenMeteoApiClient() {
        OkHttpClient client = new OkHttpClient.Builder().build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        service = retrofit.create(OpenMeteoService.class);
    }

    public WeatherApiResponse fetchWeather(double latitude, double longitude) {
        try {
            Response<WeatherApiResponse> response = service
                    .getForecast(
                            latitude,
                            longitude,
                            true,
                            "temperature_2m,relativehumidity_2m,precipitation,windspeed_10m",
                            1
                    )
                    .execute();

            if (response.isSuccessful()) {
                return response.body();
            } else {
                Log.w(TAG, "Errore Open-Meteo: " + response.code());
            }
        } catch (Exception e) {
            Log.e(TAG, "Impossibile contattare Open-Meteo", e);
        }
        return null;
    }
}
