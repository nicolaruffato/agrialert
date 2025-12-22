package com.agrialert.data_manager;

import androidx.room.Entity;
import androidx.room.ForeignKey;

// ricordarsi di definire onDelete
@Entity(primaryKeys = {"alertTypeId", "fieldId"},
foreignKeys = {
        @ForeignKey(entity = AlertType.class, parentColumns = "id", childColumns = "alertTypeId"),
        @ForeignKey(entity = Field.class, parentColumns = "id", childColumns = "fieldId")})
class AlertTypeCrossRef {

    private int alertTypeId;
    private int fieldId;

    // Cambiare tipo a Double
    private Double threshold;

    AlertTypeCrossRef(int alertTypeId, int fieldId, Double threshold) {
        this.alertTypeId = alertTypeId;
        this.fieldId = fieldId;
        this.threshold = threshold;
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

    protected Double getThreshold() {
        return threshold;
    }

    protected void setThreshold(Double threshold) {
        this.threshold = threshold;
    }
}
