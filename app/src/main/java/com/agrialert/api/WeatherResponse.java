package com.agrialert.api;

import java.util.ArrayList;

public class WeatherResponse {

    public double latitude;

    public double longitude;

    public Hourly hourly;

    public static class Hourly {
        public ArrayList<String> time;
        public ArrayList<Double> relativehumidity_2m;
        public ArrayList<Integer> temperature_2m;
        public ArrayList<Double> windspeed_10m;;
        public ArrayList<Integer>  weathercode;
    }
}