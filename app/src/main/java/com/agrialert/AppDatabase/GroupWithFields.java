package com.agrialert.AppDatabase;

import androidx.room.Embedded;
import androidx.room.Relation;

import java.util.List;

public class GroupWithFields {
    @Embedded
    public FieldsGroup group;

    @Relation(
            parentColumn = "name",       // La colonna ID nella tabella Gruppi
            entityColumn = "groupName"   // La colonna che punta al gruppo nella tabella Campi
    )
    public List<Field> fields;

    @Override
    public String toString() {
        return "GroupWithFields{" +
                "group=" + group +
                ", fields=" + fields +
                '}';
    }
}
