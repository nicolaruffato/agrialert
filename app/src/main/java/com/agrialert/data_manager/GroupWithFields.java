package com.agrialert.data_manager;

import androidx.room.Embedded;
import androidx.room.Relation;

import java.util.List;

/**
 * Represents a relationship between a group and its associated fields.
 * <p>
 * This class is used to perform a join-like operation, retrieving a
 * {@link FieldsGroup} along with all its related {@link Field} entities.
 */
public class GroupWithFields {
    @Embedded
    private FieldsGroup group;

    @Relation(
            parentColumn = "name",       // La colonna ID nella tabella Gruppi
            entityColumn = "groupName"   // La colonna che punta al gruppo nella tabella Campi
    )
    private List<Field> fields;

    /**
     * Gets the fields group entity associated with this relation.
     *
     * @return the {@link FieldsGroup} object containing the group's metadata.
     */
    public FieldsGroup getGroup() {
        return group;
    }

    /**
     * Sets the group associated with these fields.
     * This method should not be used in production code.
     *
     * @param group the {@link FieldsGroup} entity to be set as the parent
     */
    protected void setGroup(FieldsGroup group) {
        this.group = group;
    }

    /**
     * Gets the list of fields associated with this group.
     *
     * @return A List of {@link Field} objects belonging to the group.
     */
    public List<Field> getFields() {
        return fields;
    }

    /**
     * Sets the list of fields associated with this group.
     * This method should not be used in production code.
     *
     * @param fields The list of {@link Field} objects to assign to this group.
     */
    protected void setFields(List<Field> fields) {
        this.fields = fields;
    }

    /**
     * Returns a string representation of the GroupWithFields object.
     *
     * @return A string containing the group and fields data.
     */
    @Override
    public String toString() {
        return "GroupWithFields{" +
                "group=" + group +
                ", fields=" + fields +
                '}';
    }
}
