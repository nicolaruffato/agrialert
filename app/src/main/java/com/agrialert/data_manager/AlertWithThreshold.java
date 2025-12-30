package com.agrialert.data_manager;

import androidx.core.util.Pair;
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



    public Threshold getThreshold() {
        if(crossRef.getThreshold1() != null) {
            return new Threshold(crossRef.getThreshold1(), crossRef.getThreshold2());
        }
        else {
            return alertType.getDefaultThreshold();
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