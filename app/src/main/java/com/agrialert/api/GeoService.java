package com.agrialert.api;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface GeoService {

    // Geocoding: From address to coordinates
    @GET("mapbox.places/{query}.json")
    Call<MapboxGeocodingResponse> getCoordinates(
            @Path("query") String query,
            @Query("access_token") String accessToken
    );

    // Reverse Geocoding: From coordinates to address
    @GET("mapbox.places/{longitude},{latitude}.json")
    Call<MapboxGeocodingResponse> getAddress(
            @Path("longitude") double longitude,
            @Path("latitude") double latitude,
            @Query("access_token") String accessToken
    );
}