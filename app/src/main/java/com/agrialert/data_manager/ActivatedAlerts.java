package com.agrialert.data_manager;

import androidx.room.Embedded;
import androidx.room.Relation;
import java.util.List;

/**
 * Represents a data transfer object (DTO) that models the relationship between a {@link Field}
 * and its associated active alerts.
 */
public class ActivatedAlerts {
    @Embedded
    private Field field;

    @Relation(
            entity = AlertTypeCrossRef.class,
            parentColumn = "id",
            entityColumn = "fieldId"
    )
    private List<AlertWithThreshold> alerts;

    /**
     * Gets the field associated with the activated alerts.
     *
     * @return the {@link Field} entity.
     */
    public Field getField() {
        return field;
    }

    /**
     * Sets the field associated with these activated alerts.
     * This method should not be used in production code.
     *
     * @param field The {@link Field} object to be embedded in this instance.
     */
    protected void setField(Field field) {
        this.field = field;
    }

    /**
     * Gets the list of alerts with their associated thresholds currently
     * activated for the field.
     *
     * @return A list of {@link AlertWithThreshold} objects associated with this field.
     */
    public List<AlertWithThreshold> getAlerts() {
        return alerts;
    }

    /**
     * Sets the list of alerts associated with their respective thresholds for a specific field.
     * This method should not be used in production code.
     *
     * @param alerts the list of {@link AlertWithThreshold} objects to associate with the field
     */
    protected void setAlerts(List<AlertWithThreshold> alerts) {
        this.alerts = alerts;
    }

    /**
     * Returns a string representation of the ActivatedAlerts object.
     *
     * @return a string representation of the object.
     */
    @Override
    public String toString() {
        return "ActivatedAlerts{" +
                "field=" + field +
                ", alerts=" + alerts +
                '}';
    }
}