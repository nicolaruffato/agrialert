package com.agrialert.api;

import com.google.gson.annotations.SerializedName;

public class MapboxFeature {
    @SerializedName("place_name")
    public String placeName;

    @SerializedName("geometry")
    public MapboxGeometry geometry;
}
