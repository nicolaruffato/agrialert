package com.agrialert.AppDatabase;

import androidx.room.Embedded;
import androidx.room.Relation;

import java.util.List;

public class GroupWithFieldsDB {
    @Embedded
    public FieldsGroupDB group;

    @Relation(
            parentColumn = "id",       // La colonna ID nella tabella Gruppi
            entityColumn = "groupId"   // La colonna che punta al gruppo nella tabella Campi
    )
    public List<FieldDB> fields;
}
