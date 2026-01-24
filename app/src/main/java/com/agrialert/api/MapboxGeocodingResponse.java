package com.agrialert.api;

import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * Rappresenta la risposta principale restituita dall'API di geocodifica di Mapbox.
 * Contiene una lista di feature geografiche che corrispondono alla query di ricerca.
 */
public class MapboxGeocodingResponse {
    /**
     * La lista di feature geografiche trovate.
     */
    @SerializedName("features")
    public List<MapboxFeature> features;
}
