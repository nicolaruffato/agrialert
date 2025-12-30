package com.agrialert.data_manager;
public class Threshold {

    private Double threshold1;
    private Double threshold2;


    public Threshold(Double threshold) {
        this.threshold1 = threshold;
        this.threshold2 = null;
    }

    public Threshold(Double threshold1, Double threshold2) {
        this.threshold1 = threshold1;
        this.threshold2 = threshold2;
    }

    public Double getThreshold1() {
        return threshold1;
    }

    public void setThreshold1(Double threshold1) {
        this.threshold1 = threshold1;
    }

    public Double getThreshold2() {
        return threshold2;
    }

    public void setThreshold2(Double threshold2) {
        this.threshold2 = threshold2;
    }

    @Override
    public String toString() {
        return "Threshold{" +
                "threshold1=" + threshold1 +
                ", threshold2=" + threshold2 +
                '}';
    }
}
