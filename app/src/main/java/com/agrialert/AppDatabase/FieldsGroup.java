package com.agrialert.AppDatabase;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import org.jetbrains.annotations.NotNull;

@Entity
public class FieldsGroup {

    @PrimaryKey
    @NotNull
    private String name;
    private String description;

    public FieldsGroup(@NotNull String name, String description) {
        this.name = name;
        this.description = description;
    }


    @NotNull
    public String getName() {
        return name;
    }


    protected void setName(@NotNull String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return "FieldsGroup{" +
                "name='" + name + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}
