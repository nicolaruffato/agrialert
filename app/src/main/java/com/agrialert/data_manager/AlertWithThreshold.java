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



    public Pair<Double, Double> getThreshold() {
        if(crossRef.getThreshold1() != null) {
            return new Pair<>(crossRef.getThreshold1(), crossRef.getThreshold2());
        }
        else {
            return new Pair<>(alertType.getDefaultTreshold1(), alertType.getDefaultTreshold2());
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