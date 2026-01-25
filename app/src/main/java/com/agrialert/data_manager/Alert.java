package com.agrialert.data_manager;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Room entity representing a weather alert associated with a field.
 * <p>
 * Persisted in the {@code alerts} table. All timestamps are expressed as epoch milliseconds.
 * {@link #forecastAt} represents the expected start time of the weather event; {@link #durationMs}
 * (when &gt; 0) represents an estimated event duration used by the UI/notifications.
 * </p>
 */
@Entity(tableName = "alerts")
public class Alert {

    /** Auto-generated primary key. */
    @PrimaryKey(autoGenerate = true)
    private long id;

    /** Identifier of the field this alert refers to. */
    private long fieldId;

    /** Optional group name, used for UI display and notification aggregation. */
    private String groupName;

    /** Alert type identifier ({@link AlertType}). */
    private int typeId;

    /** User-facing title shown in UI/notifications. */
    private String title;

    /** User-facing description. */
    private String description;

    /** Field address/label for UI display. */
    private String fieldAddress;

    /** Forecast timestamp (ms) for the weather event that triggers the alert. */
    private long forecastAt;

    /** Estimated event duration in milliseconds (0 if unknown). */
    private long durationMs;

    /** Creation/insert timestamp (ms). */
    private long createdAt;

    /** Whether the alert is marked as resolved. */
    private boolean resolved;

    /** Resolution timestamp (ms), or {@code 0} when not resolved. */
    private long resolvedAt;

    /** Icon resource id associated with the alert. */
    private int iconRes;

    /**
     * No-arg constructor required by Room.
     */
    public Alert() {
    }

    /**
     * Creates an alert with the main fields initialized.
     * <p>
     * {@link #resolvedAt} is initialized to {@code createdAt} when {@code resolved} is {@code true},
     * otherwise it is set to {@code 0}.
     * </p>
     *
     * @param fieldId      field id
     * @param groupName    optional group name
     * @param typeId       alert type id
     * @param title        user-facing title
     * @param description  user-facing description
     * @param fieldAddress field address/label
     * @param forecastAt   forecast time of the event (ms)
     * @param createdAt    creation time (ms)
     * @param resolved     initial resolved state
     * @param iconRes      icon resource id
     */
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

    public long getDurationMs() {
        return durationMs;
    }

    public void setDurationMs(long durationMs) {
        this.durationMs = durationMs;
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
