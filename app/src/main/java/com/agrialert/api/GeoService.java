package com.agrialert.api;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface GeoService {
    @GET("geocode/json")
    Call<GeoResponse> getCoordinates(
            @Query("address") String address,
            @Query("key") String apiKey
    );
}
