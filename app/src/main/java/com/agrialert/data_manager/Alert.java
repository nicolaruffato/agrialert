package com.agrialert.data_manager;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Entità Room e modello pubblico per gli alert (getter/setter).
 */
@Entity(tableName = "alerts")
public class Alert {

    @PrimaryKey(autoGenerate = true)
    private long id;

    /** Identificatore del campo a cui si riferisce l'alert */
    private long fieldId;

    /** Nome del gruppo (opzionale) utile per la UI */
    private String groupName;

    private int typeId;
    private String title;
    private String description;
    private String fieldAddress;
    /** Timestamp (ms) previsto per l'evento meteo che genera l'alert */
    private long forecastAt;
    private long createdAt;
    private boolean resolved;
    /** Timestamp (ms) in cui l'alert è stato marcato come risolto (0 se non risolto). */
    private long resolvedAt;
    private int iconRes;

    public Alert() {
    }

    public Alert(long fieldId,
                 String groupName,
                 int typeId,
                 String title,
                 String description,
                 String fieldAddress,
                 long forecastAt,
                 long createdAt,
                 boolean resolved,
                 int iconRes) {
        this.fieldId = fieldId;
        this.groupName = groupName;
        this.typeId = typeId;
        this.title = title;
        this.description = description;
        this.fieldAddress = fieldAddress;
        this.forecastAt = forecastAt;
        this.createdAt = createdAt;
        this.resolved = resolved;
        this.resolvedAt = resolved ? createdAt : 0L;
        this.iconRes = iconRes;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getFieldId() {
        return fieldId;
    }

    public void setFieldId(long fieldId) {
        this.fieldId = fieldId;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public int getTypeId() {
        return typeId;
    }

    public void setTypeId(int typeId) {
        this.typeId = typeId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getFieldAddress() {
        return fieldAddress;
    }

    public void setFieldAddress(String fieldAddress) {
        this.fieldAddress = fieldAddress;
    }

    public long getForecastAt() {
        return forecastAt;
    }

    public void setForecastAt(long forecastAt) {
        this.forecastAt = forecastAt;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isResolved() {
        return resolved;
    }

    public void setResolved(boolean resolved) {
        this.resolved = resolved;
    }

    public long getResolvedAt() {
        return resolvedAt;
    }

    public void setResolvedAt(long resolvedAt) {
        this.resolvedAt = resolvedAt;
    }

    public int getIconRes() {
        return iconRes;
    }

    public void setIconRes(int iconRes) {
        this.iconRes = iconRes;
    }
}
