package com.agrialert.AppDatabase;

import androidx.room.Entity;
import androidx.room.ForeignKey;

// ricordarsi di definire onDelete
@Entity(primaryKeys = {"alertTypeId", "fieldId"},
foreignKeys = {
        @ForeignKey(entity = AlertType.class, parentColumns = "id", childColumns = "alertTypeId"),
        @ForeignKey(entity = Field.class, parentColumns = "id", childColumns = "fieldId")})
public class AlertTypeCrossRef {

    protected int alertTypeId;
    protected int fieldId;

    // Cambiare tipo a Double
    protected Double threshold;

    public AlertTypeCrossRef(int alertTypeId, int fieldId, Double threshold) {
        this.alertTypeId = alertTypeId;
        this.fieldId = fieldId;
        this.threshold = threshold;
    }
}
