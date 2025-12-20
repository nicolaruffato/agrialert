package com.agrialert.data_manager;

import androidx.room.Embedded;
import androidx.room.Relation;

public class AlertWithThreshold {
    @Embedded
    AlertTypeCrossRef crossRef; // Contiene il threshold e gli ID

    @Relation(
            parentColumn = "alertTypeId",
            entityColumn = "id"
    )
    private AlertType alertType; // Contiene i dettagli dell'alert



    public Double getThreshold() {
        if(crossRef.getThreshold() != null) {
            return crossRef.getThreshold();
        }
        else {
            return alertType.getDefaultTreshold();
        }
    }

    public AlertType getAlertType() {
        return alertType;
    }

    protected void setAlertType(AlertType alertType) {
        this.alertType = alertType;
    }

    @Override
    public String toString() {
        return "AlertWithThreshold{" +
                "crossRef=" + crossRef +
                ", alertType=" + alertType +
                '}';
    }
}