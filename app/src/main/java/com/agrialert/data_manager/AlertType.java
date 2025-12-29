package com.agrialert.data_manager;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity
public class AlertType {

    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "name")
    private String name;

    @ColumnInfo(name = "description")
    private String description;

    @ColumnInfo(name = "defaultTreshold1")
    private Double defaultTreshold1;

    @ColumnInfo(name = "defaultTreshold2", defaultValue = "NULL")
    private Double defaultTreshold2;

    public AlertType(String name, String description, Double defaultTreshold1, Double defaultTreshold2) {
        this.name = name;
        this.description = description;
        this.defaultTreshold1 = defaultTreshold1;
        this.defaultTreshold2 = defaultTreshold2;
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

    public Double getDefaultTreshold1() {
        return defaultTreshold1;
    }

    public void setDefaultTreshold1(Double defaultTreshold1) {
        this.defaultTreshold1 = defaultTreshold1;
    }

    public Double getDefaultTreshold2() {
        return defaultTreshold2;
    }

    public void setDefaultTreshold2(Double defaultTreshold2) {
        this.defaultTreshold2 = defaultTreshold2;
    }

    @Override
    public String toString() {
        return "AlertType{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", defaultTreshold1=" + defaultTreshold1 +
                ", defaultTreshold2=" + defaultTreshold2 +
                '}';
    }
}
