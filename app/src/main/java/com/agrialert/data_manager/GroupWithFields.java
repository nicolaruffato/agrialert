package com.agrialert.data_manager;

import androidx.room.Embedded;
import androidx.room.Relation;

import java.util.List;

public class GroupWithFields {
    @Embedded
    private FieldsGroup group;

    @Relation(
            parentColumn = "name",       // La colonna ID nella tabella Gruppi
            entityColumn = "groupName"   // La colonna che punta al gruppo nella tabella Campi
    )
    private List<Field> fields;

    public FieldsGroup getGroup() {
        return group;
    }

    protected void setGroup(FieldsGroup group) {
        this.group = group;
    }

    public List<Field> getFields() {
        return fields;
    }

    protected void setFields(List<Field> fields) {
        this.fields = fields;
    }

    @Override
    public String toString() {
        return "GroupWithFields{" +
                "group=" + group +
                ", fields=" + fields +
                '}';
    }
}
