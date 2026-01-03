package com.agrialert.data_manager;

/**
 * Represents a threshold configuration containing boundary values.
 * This class is used to define limits for monitoring data, supporting both
 * single-point thresholds and range-based thresholds.
 */
public class Threshold {

    private Double threshold1;
    private Double threshold2;


    /**
     * Constructs a Threshold object with a single boundary value.
     *
     * @param threshold the primary threshold value to be set
     */
    public Threshold(Double threshold) {
        this.threshold1 = threshold;
        this.threshold2 = null;
    }

    /**
     * Constructs a new Threshold range based on two boundary values.
     *
     * @param threshold1 the first threshold value
     * @param threshold2 the second threshold value
     */
    public Threshold(Double threshold1, Double threshold2) {
        this.threshold1 = threshold1;
        this.threshold2 = threshold2;
    }

    /**
     * Gets the primary threshold value.
     *
     * @return the value of the first threshold
     */
    public Double getThreshold1() {
        return threshold1;
    }

    /**
     * Sets the primary threshold value.
     *
     * @param threshold1 the first threshold value to be set
     */
    public void setThreshold1(Double threshold1) {
        this.threshold1 = threshold1;
    }

    /**
     * Gets the second threshold value.
     *
     * @return the value of the second threshold, or null if only one threshold is defined
     */
    public Double getThreshold2() {
        return threshold2;
    }

    /**
     * Sets the value of the second threshold.
     *
     * @param threshold2 the value to be set as the second threshold
     */
    public void setThreshold2(Double threshold2) {
        this.threshold2 = threshold2;
    }

    /**
     * Returns a string representation of the Threshold object.
     *
     * @return a string representation of the object.
     */
    @Override
    public String toString() {
        return "Threshold{" +
                "threshold1=" + threshold1 +
                ", threshold2=" + threshold2 +
                '}';
    }
}
