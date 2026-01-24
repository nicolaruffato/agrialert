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

    /**
     * Represents the current weather conditions returned by the Open-Meteo API.
     * Includes instantaneous measurements such as temperature, wind data, and weather status codes.
     */
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

    /**
     * Represents the hourly weather data provided by the Open-Meteo API.
     * This class contains lists of weather parameters indexed by time.
     */
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
