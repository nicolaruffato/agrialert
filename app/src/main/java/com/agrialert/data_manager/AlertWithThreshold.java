package com.agrialert.data_manager;

import androidx.room.Embedded;
import androidx.room.Relation;

/**
 * Represents a data transfer object (DTO) that combines an {@link AlertType} with its
 * specific threshold settings.
 */
public class AlertWithThreshold {
    @Embedded
    AlertTypeCrossRef crossRef;

    @Relation(
            parentColumn = "alertTypeId",
            entityColumn = "id"
    )
    private AlertType alertType;



    /**
     * Retrieves the threshold for this alert.
     * <p>
     * If a specific threshold was defined by the user, it returns it.
     * Otherwise, it returns the default threshold associated with the alert type.
     * </p>
     *
     * @return the specific {@link Threshold} if defined, or the {@link AlertType}'s default threshold.
     */
    public Threshold getThreshold() {
        if(crossRef.getThreshold1() != null) {
            return new Threshold(crossRef.getThreshold1(), crossRef.getThreshold2());
        }
        else {
            return alertType.getDefaultThreshold();
        }
    }

    /**
     * Gets the alert type details.
     *
     * @return the {@link AlertType} object containing alert metadata and default values.
     */
    public AlertType getAlertType() {
        return alertType;
    }

    /**
     * Sets the alert type associated with this threshold.
     * This method should not be used in production code.
     *
     * @param alertType the alert type containing the specific details to be set
     */
    protected void setAlertType(AlertType alertType) {
        this.alertType = alertType;
    }

    /**
     * Returns a string representation of the {@code AlertWithThreshold} object.
     *
     * @return a string representation of this object
     */
    @Override
    public String toString() {
        return "AlertWithThreshold{" +
                "crossRef=" + crossRef +
                ", alertType=" + alertType +
                '}';
    }
}