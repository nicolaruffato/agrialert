package com.agrialert.api;

import java.util.List;

public class GeoResponse {

    public List<Result> results;
    public String status;

    public static class Result {
        public Geometry geometry;
        public String formatted_address;
    }

    public static class Geometry {
        public Location location;
    }

    public static class Location {
        public double lat;
        public double lng;
    }
}
