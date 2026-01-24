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

    /**
     * Tag for logging purposes.
     */
    private static final String TAG = "OpenMeteoApiClient";
    /**
     * Base URL for the Open-Meteo API.
     */
    private static final String BASE_URL = "https://api.open-meteo.com/";
    /**
     * Number of days to forecast.
    */
    private static final int FORECAST_DAYS = 7;
    /**
     * Servizio Retrofit per definire le chiamate API verso l'endpoint di Open-Meteo.
     */
    private final OpenMeteoService service;

    /**
     * Inizializza il client API configurando Retrofit con l'URL di base e il convertitore JSON (Gson).
     * Crea un'istanza del servizio OpenMeteoService per gestire le chiamate di rete.
     */
    public OpenMeteoApiClient() {
        OkHttpClient client = new OkHttpClient.Builder().build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        service = retrofit.create(OpenMeteoService.class);
    }

    /**
     * Fetches the 7-day weather forecast for a specific geographic location.
     *
     * Requests data for temperature (2m), relative humidity (2m), precipitation,
     * and wind speed (10m) from the Open-Meteo API.
     *
     * @param latitude  The latitude coordinate of the location.
     * @param longitude The longitude coordinate of the location.
     * @return A {@link WeatherApiResponse} object containing the forecast data if successful,
     *         or {@code null} if the request fails or an exception occurs.
     */
    public WeatherApiResponse fetchWeather(double latitude, double longitude) {
        try {
            Response<WeatherApiResponse> response = service
                    .getForecast(
                            latitude,
                            longitude,
                            true,
                            "temperature_2m,relativehumidity_2m,precipitation,windspeed_10m",
                            FORECAST_DAYS
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
