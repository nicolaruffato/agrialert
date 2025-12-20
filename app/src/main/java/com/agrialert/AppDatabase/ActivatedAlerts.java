package com.agrialert.AppDatabase;

import androidx.room.Embedded;
import androidx.room.Relation;
import java.util.List;

public class ActivatedAlerts {
    @Embedded
    protected Field field;

    @Relation(
            entity = AlertTypeCrossRef.class, // Specifichiamo l'entità di partenza
            parentColumn = "id",              // ID di Field
            entityColumn = "fieldId"          // Colonna in AlertTypeCrossRef
    )
    protected List<AlertWithThreshold> alerts;

    public Field getField() {
        return field;
    }

    public List<AlertWithThreshold> getAlerts() {
        return alerts;
    }

    @Override
    public String toString() {
        return "ActivatedAlerts{" +
                "field=" + field +
                ", alerts=" + alerts +
                '}';
    }
}