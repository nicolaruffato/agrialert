package com.agrialert.api;

import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * Rappresenta la geometria di una feature di Mapbox.
 * Contiene le coordinate geografiche del punto.
 */
public class MapboxGeometry {
    /**
     * Lista di coordinate geografiche.
     * Generalmente in formato [longitudine, latitudine].
     */
    @SerializedName("coordinates")
    public List<Double> coordinates;
}
