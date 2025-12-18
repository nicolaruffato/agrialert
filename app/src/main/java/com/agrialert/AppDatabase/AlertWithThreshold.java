package com.agrialert.AppDatabase;

import androidx.room.Embedded;
import androidx.room.Relation;

public class AlertWithThreshold {
    @Embedded
    protected AlertTypeCrossRef crossRef; // Contiene il threshold e gli ID

    @Relation(
            parentColumn = "alertTypeId",
            entityColumn = "id"
    )
    public AlertType alertType; // Contiene i dettagli dell'alert

    @Override
    public String toString() {
        return "AlertWithThreshold{" +
                "crossRef=" + crossRef +
                ", alertType=" + alertType +
                '}';
    }
}