package com.agrialert.ui.alerts;

/**
 * UI Model representing an alert configuration setting.
 * This model holds the state for an alert type, including its enabled status
 * and up to two configurable thresholds.
 */
public class AlertSettingUiModel {

    /** The unique identifier for this alert setting. */
    public long id;
    /** The resource ID for the icon representing this alert type. */
    public int iconRes;
    /** The title of the alert setting. */
    public String title;
    /** A brief description of the alert setting. */
    public String description;

    /** Indicates whether this alert is currently enabled. */
    public boolean enabled;

    /** Whether this alert has a primary threshold to configure. */
    public boolean hasPrimaryThreshold;
    /** The label for the primary threshold. */
    public String primaryLabel;
    /** The current value of the primary threshold. */
    public int primaryValue;
    /** The unit of measurement for the primary threshold (e.g., "°C", "%"). */
    public String primaryUnit;

    /** Whether this alert has an optional secondary threshold. */
    public boolean hasSecondaryThreshold;
    /** The label for the secondary threshold. */
    public String secondaryLabel;
    /** The current value of the secondary threshold. */
    public int secondaryValue;
    /** The unit of measurement for the secondary threshold. */
    public String secondaryUnit;

    /**
     * Constructs a new AlertSettingUiModel with all fields.
     *
     * @param id                    Unique ID.
     * @param iconRes               Icon resource ID.
     * @param title                 Title string.
     * @param description           Description string.
     * @param enabled               Enabled state.
     * @param hasPrimaryThreshold   Whether it has a first threshold.
     * @param primaryLabel          Label for the first threshold.
     * @param primaryValue          Value for the first threshold.
     * @param primaryUnit           Unit for the first threshold.
     * @param hasSecondaryThreshold Whether it has a second threshold.
     * @param secondaryLabel        Label for the second threshold.
     * @param secondaryValue        Value for the second threshold.
     * @param secondaryUnit         Unit for the second threshold.
     */
    public AlertSettingUiModel(long id,
                               int iconRes,
                               String title,
                               String description,
                               boolean enabled,
                               boolean hasPrimaryThreshold,
                               String primaryLabel,
                               int primaryValue,
                               String primaryUnit,
                               boolean hasSecondaryThreshold,
                               String secondaryLabel,
                               int secondaryValue,
                               String secondaryUnit) {

        this.id = id;
        this.iconRes = iconRes;
        this.title = title;
        this.description = description;
        this.enabled = enabled;

        this.hasPrimaryThreshold = hasPrimaryThreshold;
        this.primaryLabel = primaryLabel;
        this.primaryValue = primaryValue;
        this.primaryUnit = primaryUnit;

        this.hasSecondaryThreshold = hasSecondaryThreshold;
        this.secondaryLabel = secondaryLabel;
        this.secondaryValue = secondaryValue;
        this.secondaryUnit = secondaryUnit;
    }

    /**
     * Gets the ID as an integer.
     * @return the integer ID.
     */
    public int getId() {
        return (int) id;
    }

    /**
     * Gets the original long ID.
     * @return the long ID.
     */
    public long getIdLong() {
        return id;
    }

    /**
     * Returns whether the alert is enabled.
     * @return true if enabled, false otherwise.
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Sets the enabled state of the alert.
     * @param enabled the new enabled state.
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * Checks if a primary threshold exists.
     * @return true if primary threshold is available.
     */
    public boolean hasFirstThreshold() {
        return hasPrimaryThreshold;
    }

    /**
     * Gets the current value of the primary threshold.
     * @return the primary value.
     */
    public int getFirstValue() {
        return primaryValue;
    }

    /**
     * Sets the value for the primary threshold.
     * @param v the new primary value.
     */
    public void setFirstValue(int v) {
        this.primaryValue = v;
    }

    /**
     * Gets the primary threshold value as a double.
     * @return the primary value in double precision.
     */
    public double getFirstValueDouble() {
        return (double) primaryValue;
    }

    /**
     * Gets the unit of the primary threshold.
     * @return the unit string.
     */
    public String getFirstUnit() {
        return primaryUnit;
    }

    /**
     * Checks if a secondary threshold exists.
     * @return true if secondary threshold is available.
     */
    public boolean hasSecond() {
        return hasSecondaryThreshold;
    }

    /**
     * Gets the current value of the secondary threshold.
     * @return the secondary value.
     */
    public int getSecondValue() {
        return secondaryValue;
    }

    /**
     * Sets the value for the secondary threshold.
     * @param v the new secondary value.
     */
    public void setSecondValue(int v) {
        this.secondaryValue = v;
    }

    /**
     * Gets the secondary threshold value as a double.
     * @return the secondary value in double precision.
     */
    public double getSecondValueDouble() {
        return (double) secondaryValue;
    }

    /**
     * Gets the unit of the secondary threshold.
     * @return the unit string.
     */
    public String getSecondUnit() {
        return secondaryUnit;
    }

}
