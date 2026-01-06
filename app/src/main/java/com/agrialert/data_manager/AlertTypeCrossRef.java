package com.agrialert.data_manager;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;

// ricordarsi di definire onDelete




/**
 * Represents a cross-reference join table between {@link AlertType} and {@link Field}.
 * This entity establishes a many-to-many relationship, allowing specific alert configurations
 * to be associated with different fields, including customizable threshold values.
 * This class should not be exposed outside its package.
 *
 * <p>The primary key is a composite of {@code fieldId} and {@code alertTypeId}.</p>
 */
@Entity(primaryKeys = {"fieldId", "alertTypeId"},
foreignKeys = {
        @ForeignKey(entity = AlertType.class, parentColumns = "id", childColumns = "alertTypeId"),
        @ForeignKey(entity = Field.class, parentColumns = "id", childColumns = "fieldId")},
        indices = {@Index("alertTypeId"), @Index("fieldId")}
)
class AlertTypeCrossRef {

    private int alertTypeId;
    private int fieldId;

    private Double threshold1;
    private Double threshold2;

    AlertTypeCrossRef(int alertTypeId, int fieldId, Double threshold1, Double threshold2) {
        this.alertTypeId = alertTypeId;
        this.fieldId = fieldId;
        this.threshold1 = threshold1;
        this.threshold2 = threshold2;
    }

    protected int getAlertTypeId() {
        return alertTypeId;
    }

    protected void setAlertTypeId(int alertTypeId) {
        this.alertTypeId = alertTypeId;
    }

    protected int getFieldId() {
        return fieldId;
    }

    protected void setFieldId(int fieldId) {
        this.fieldId = fieldId;
    }

    protected Double getThreshold1() {
        return threshold1;
    }

    protected void setThreshold1(Double threshold1) {
        this.threshold1 = threshold1;
    }

    protected Double getThreshold2() {
        return threshold2;
    }

    protected void setThreshold2(Double threshold2) {
        this.threshold2 = threshold2;
    }
}
