package com.agrialert.api;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Retrofit service interface for interacting with the Mapbox Geocoding API.
 * Provides methods for forward geocoding (address to coordinates) and
 * reverse geocoding (coordinates to address).
 */
public interface MapboxService {

    /**
     * Performs forward geocoding to convert a location name or address into geographic coordinates.
     *
     * @param query The location name or address to search for (e.g., "1600 Pennsylvania Ave NW").
     * @param accessToken The Mapbox API access token used for authentication.
     * @return A {@link Call} object that, when executed, returns a {@link MapboxGeocodingResponse}
     *         containing the geographic data.
     */
    @GET("mapbox.places/{query}.json")
    Call<MapboxGeocodingResponse> getCoordinates(
            @Path("query") String query,
            @Query("access_token") String accessToken
    );

    /**
     * Performs reverse geocoding to retrieve address information for a specific set of coordinates.
     *
     * @param longitude   The longitude coordinate of the location.
     * @param latitude    The latitude coordinate of the location.
     * @param accessToken The Mapbox API access token for authentication.
     * @return A {@link Call} object that, when executed, returns a {@link MapboxGeocodingResponse}.
     */
    @GET("mapbox.places/{longitude},{latitude}.json")
    Call<MapboxGeocodingResponse> getAddress(
            @Path("longitude") double longitude,
            @Path("latitude") double latitude,
            @Query("access_token") String accessToken
    );
}