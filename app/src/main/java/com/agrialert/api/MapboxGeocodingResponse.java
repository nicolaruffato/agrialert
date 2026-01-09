package com.agrialert.api;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class MapboxGeocodingResponse {
    @SerializedName("features")
    public List<MapboxFeature> features;
}
