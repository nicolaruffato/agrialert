package com.agrialert.data_manager;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import org.jetbrains.annotations.NotNull;

/**
 * Represents a group of fields data within the data management system.
 * This class is used to persist grouping information,
 * uniquely identified by its name.
 */
@Entity
public class FieldsGroup {

    @PrimaryKey
    @NotNull
    private String name;
    private String description;

    /**
     * Constructs a new FieldsGroup with the specified name and description.
     * Must be unique.
     *
     * @param name        The unique name of the fields group, cannot be null.
     * @param description A brief description of the fields group.
     */
    public FieldsGroup(@NotNull String name, String description) {
        this.name = name;
        this.description = description;
    }


    /**
     * Gets the unique name of the fields group.
     *
     * @return The name of the group, which serves as the primary identifier.
     */
    @NotNull
    public String getName() {
        return name;
    }


    /**
     * Sets the unique name of this fields group.
     * This method should not be used in production code.
     *
     * @param name The new name to be assigned, cannot be null.
     */
    protected void setName(@NotNull String name) {
        this.name = name;
    }

    /**
     * Gets the description of the fields group.
     *
     * @return The description string.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the description of the fields group.
     *
     * @param description A brief description of the fields group.
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Returns a string representation of the FieldsGroup object.
     *
     * @return A string containing the group's name and description.
     */
    @Override
    public String toString() {
        return "FieldsGroup{" +
                "name='" + name + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}
