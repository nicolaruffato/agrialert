package com.agrialert.data_manager;

import androidx.room.Embedded;
import androidx.room.Relation;
import java.util.List;

public class ActivatedAlerts {
    @Embedded
    private Field field;

    @Relation(
            entity = AlertTypeCrossRef.class, // Specifichiamo l'entità di partenza
            parentColumn = "id",              // ID di Field
            entityColumn = "fieldId"          // Colonna in AlertTypeCrossRef
    )
    private List<AlertWithThreshold> alerts;

    public Field getField() {
        return field;
    }

    protected void setField(Field field) {
        this.field = field;
    }

    public List<AlertWithThreshold> getAlerts() {
        return alerts;
    }

    protected void setAlerts(List<AlertWithThreshold> alerts) {
        this.alerts = alerts;
    }

    @Override
    public String toString() {
        return "ActivatedAlerts{" +
                "field=" + field +
                ", alerts=" + alerts +
                '}';
    }
}