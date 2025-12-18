package com.agrialert.AppDatabase;

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
    private int defaultTreshold;

    public AlertType(String name, String description, int defaultTreshold) {
        this.name = name;
        this.description = description;
        this.defaultTreshold = defaultTreshold;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
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

    public int getDefaultTreshold() {
        return defaultTreshold;
    }

    public void setDefaultTreshold(int defaultTreshold) {
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
