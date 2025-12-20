package com.agrialert.data_manager;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class AlertType {

    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "name")
    private String name;

    @ColumnInfo(name = "description")
    private String description;

    @ColumnInfo(name = "defaultTreshold")
    private Double defaultTreshold;

    public AlertType(String name, String description, Double defaultTreshold) {
        this.name = name;
        this.description = description;
        this.defaultTreshold = defaultTreshold;
    }

    public int getId() {
        return id;
    }

    protected void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Double getDefaultTreshold() {
        return defaultTreshold;
    }

    protected void setDefaultTreshold(Double defaultTreshold) {
        this.defaultTreshold = defaultTreshold;
    }

    @Override
    public String toString() {
        return "AlertType{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}
