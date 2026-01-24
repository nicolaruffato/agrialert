package com.agrialert.api;

import com.google.gson.annotations.SerializedName;

/**
 * Rappresenta una singola feature geografica restituita dall'API di geocodifica di Mapbox.
 */
public class MapboxFeature {
    /**
     * Il nome leggibile del luogo (es. indirizzo completo).
     */
    @SerializedName("place_name")
    public String placeName;

    /**
     * La geometria associata a questa feature, contenente le coordinate.
     */
    @SerializedName("geometry")
    public MapboxGeometry geometry;
}
