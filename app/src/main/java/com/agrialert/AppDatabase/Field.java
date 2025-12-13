package com.agrialert.AppDatabase;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

@Entity(foreignKeys = @ForeignKey(entity = FieldsGroup.class,
        parentColumns = "name",
        childColumns = "groupName",
        onDelete = ForeignKey.SET_DEFAULT))
public class Field {

    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "adress")
    private String address;

    @ColumnInfo(name = "latitude")
    private Double latitude;

    @ColumnInfo(name = "longitude")
    private Double longitude;

    @ColumnInfo(name = "groupName", index = true, defaultValue = "default")
    private String groupName;


    public Field(String address, Double latitude, Double longitude, String groupName) {
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
        this.groupName = groupName;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    @Override
    public String toString() {
        return "Field{" +
                "groupName='" + groupName + '\'' +
                ", longitude=" + longitude +
                ", latitude=" + latitude +
                ", address='" + address + '\'' +
                ", id=" + id +
                '}';
    }
}
