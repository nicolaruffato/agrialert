package com.agrialert.api;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class MapboxGeometry {
    @SerializedName("coordinates")
    public List<Double> coordinates;
}
