package com.agrialert.data_manager;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Represents a type of alert.
 * This entity defines the characteristics of an alert, including its name,
 * description, and default threshold values used to trigger notifications.
 */
@Entity
public class AlertType {

    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "name")
    private String name;

    @ColumnInfo(name = "description")
    private String description;

    @ColumnInfo(name = "defaultTreshold1")
    private Double defaultTreshold1;

    @ColumnInfo(name = "defaultTreshold2", defaultValue = "NULL")
    private Double defaultTreshold2;


    /**
     * Constructs a new AlertType with the specified name, description, and default thresholds.
     *
     * @param name             The name of the alert type.
     * @param description      A brief description of the alert type.
     * @param defaultTreshold  The default threshold values for this alert type.
     */
    public AlertType(String name, String description, Threshold defaultTreshold) {
        this.name = name;
        this.description = description;
        this.defaultTreshold1 = defaultTreshold.getThreshold1();
        this.defaultTreshold2 = defaultTreshold.getThreshold2();
    }

    /**
     * Gets the unique identifier for this alert type.
     *
     * @return The unique ID of the alert type.
     */
    public int getId() {
        return id;
    }

    /**
     * Sets the unique identifier for this alert type.
     * This method should not be used in production code.
     *
     * @param id the unique ID to assign to the alert type
     */
    protected void setId(int id) {
        this.id = id;
    }

    /**
     * Gets the name of the alert type.
     *
     * @return the name of this alert type.
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the alert type.
     *
     * @param name the name to be assigned to this alert type
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the description of this alert type.
     *
     * @return the alert type description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the description of the alert type.
     *
     * @param description A string containing the description of the alert type.
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Sets the default threshold values for this alert type using a Threshold object.
     * This updates both the primary and secondary threshold values.
     *
     * @param newThreshold The Threshold object containing the new primary and secondary values.
     */
    public void setDefaultThreshold(Threshold newThreshold) {
        this.defaultTreshold1 = newThreshold.getThreshold1();
        this.defaultTreshold2 = newThreshold.getThreshold2();
    }

    /**
     * Retrieves the default threshold values for this alert type.
     *
     * @return A {@link Threshold} object containing the default threshold values.
     */
    public Threshold getDefaultThreshold() {
        return new Threshold(defaultTreshold1, defaultTreshold2);
    }

    /**
     * Gets the second default threshold value for this alert type.
     * This method should not be used in production code.
     *
     * @return the second default threshold, or {@code null} if not set.
     */
    protected Double getDefaultTreshold2() {
        return defaultTreshold2;
    }

    /**
     * Sets the second default threshold value for this alert type.
     * This method should not be used in production code.
     *
     * @param defaultTreshold2 the secondary threshold value to be used as a default
     */
    protected void setDefaultTreshold2(Double defaultTreshold2) {
        this.defaultTreshold2 = defaultTreshold2;
    }

    /**
     * Gets the first default threshold value for this alert type.
     * This method should not be used in production code.
     *
     * @return the default value for the first threshold level.
     */
    protected Double getDefaultTreshold1() {
        return defaultTreshold1;
    }

    /**
     * Sets the first default threshold value for this alert type.
     * This method should not be used in production code.
     *
     * @param defaultTreshold1 The primary threshold value to be used as a default.
     */
    protected void setDefaultTreshold1(Double defaultTreshold1) {
        this.defaultTreshold1 = defaultTreshold1;
    }

    /**
     * Returns a string representation of the AlertType object.
     *
     * @return A string containing the details of the alert type.
     */
    @Override
    public String toString() {
        return "AlertType{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", defaultTreshold1=" + defaultTreshold1 +
                ", defaultTreshold2=" + defaultTreshold2 +
                '}';
    }
}
