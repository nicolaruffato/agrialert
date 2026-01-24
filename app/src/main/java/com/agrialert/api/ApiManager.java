package com.agrialert.api;

import android.util.Log;
import android.util.Pair;

import com.agrialert.BuildConfig;

import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Manages API interactions for the AgriAlert application, providing centralized access to
 * weather forecasting and geocoding services.
 *
 * <p>This class utilizes Retrofit to communicate with the Open-Meteo API for weather data
 * and the Mapbox API for converting between physical addresses and geographic coordinates.</p>
 *
 * <p>Key functionalities include:</p>
 * <ul>
 *     <li>Retrieving real-time weather forecasts based on latitude and longitude.</li>
 *     <li>Forward geocoding: Converting an address string into geographic coordinates.</li>
 *     <li>Reverse geocoding: Converting geographic coordinates into a human-readable address.</li>
 * </ul>
 *
 * <p>The class uses RxJava for asynchronous operations to ensure network calls do not
 * block the main application thread.</p>
 */
public class ApiManager {
    /**
     * The base URL for the Mapbox Geocoding API v5.
     */
    private static final String geoBaseUrl = "https://api.mapbox.com/geocoding/v5/";
    /**
     * Retrofit service instance for interacting with the Mapbox Geocoding API.
     * This service is used to perform geocoding and reverse geocoding requests.
     */
    private static final MapboxService geoService = new Retrofit.Builder()
            .baseUrl(geoBaseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .build().create(MapboxService.class);

    /**
     * Private constructor to prevent instantiation of this utility class.
     */
    private ApiManager() {}

    /**
     * Fetches the geographic coordinates (longitude and latitude) for a given address string.
     * This method performs a network request to the Mapbox Geocoding API on a background thread.
     *
     * @param address The physical address or place name to be geocoded.
     * @return A {@link Single} emitting a {@link Pair} where the first element is the longitude (Double)
     *         and the second element is the latitude (Double). If the address cannot be resolved,
     *         returns a Pair containing null values.
     */
    public static Single<Pair<?, ?>> getCoordinatesFromAddress(String address) {
        return Single.fromCallable(() -> {
            Call<MapboxGeocodingResponse> call = geoService.getCoordinates(address, BuildConfig.MAPBOX_API_KEY);
            MapboxGeocodingResponse response = call.execute().body();

            if (response != null && response.features != null && !response.features.isEmpty()) {
                List<Double> coords = response.features.get(0).geometry.coordinates;
                // Mapbox returns [longitude, latitude]
                Log.e("ApiManger", "Coordinates: " + coords.get(0) + ", " + coords.get(1));
                return new Pair<>(coords.get(0), coords.get(1)); // Return as (lat, lon)
            } else {
                // Could not find
                Log.e("ApiManager", "Error getting coordinates");
                return new Pair<>(null, null);
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    /**
     * Performs reverse geocoding to retrieve a physical address string from geographical coordinates.
     *
     * @param lat The latitude of the location.
     * @param lon The longitude of the location.
     * @return A {@link Single} emitting the address as a {@link String}, or an empty string if no address is found.
     */
    public static Single<String> getAddressFromCoordinates(double lat, double lon) {
        return Single.fromCallable(() -> {

            Call<MapboxGeocodingResponse> call = geoService.getAddress(lon, lat, BuildConfig.MAPBOX_API_KEY); // Note: lon, lat order
            MapboxGeocodingResponse response = call.execute().body();

            if (response != null && response.features != null && !response.features.isEmpty()) {
                return response.features.get(0).placeName;
            } else {
                return "";
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }
}
