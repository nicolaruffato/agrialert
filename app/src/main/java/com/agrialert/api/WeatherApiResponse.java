package com.agrialert.api;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Modello minimo per la risposta Open-Meteo.
 */
public class WeatherApiResponse {

    @SerializedName("current_weather")
    public CurrentWeather currentWeather;

    @SerializedName("hourly")
    public Hourly hourly;

    public static class CurrentWeather {
        @SerializedName("temperature")
        public double temperature;

        @SerializedName("windspeed")
        public double windspeed;

        @SerializedName("winddirection")
        public double winddirection;

        @SerializedName("weathercode")
        public int weathercode;

        @SerializedName("time")
        public String time;
    }

    public static class Hourly {
        @SerializedName("time")
        public List<String> time;

        @SerializedName("temperature_2m")
        public List<Double> temperature2m;

        @SerializedName("relativehumidity_2m")
        public List<Double> relativeHumidity2m;

        @SerializedName("precipitation")
        public List<Double> precipitation;

        @SerializedName("windspeed_10m")
        public List<Double> windSpeed10m;
    }
}
