package com.agrialert.ui.alerts;

import androidx.annotation.DrawableRes;

/**
 * UI Model representing an alert item in a list.
 * This model contains all the necessary data to display an alert's details,
 * including its type, location, timing, and resolution status.
 */
public class AlertUiModel {

    /** The unique identifier of the alert. */
    public long id;
    
    /** 
     * The type identifier of the alert (e.g., "VENTO_FORTE", "ONDATA_CALORE").
     * Corresponds to one of the predefined alert categories.
     */
    public String typeId;
    
    /** The display title of the alert (e.g., "High Wind"). */
    public String title;
    
    /** 
     * Descriptive text for the threshold that triggered the alert.
     * Example: "Wind > 50 Km/h".
     */
    public String thresholdText;
    
    /** The address or location description of the affected field. */
    public String fieldAddress;
    
    /** 
     * A human-readable label for when the alert is expected or occurred.
     * Example: "Today", "Tomorrow", "In 5 days".
     */
    public String timeLabel;
    
    /** Indicates whether the alert has been marked as resolved by the user. */
    public boolean isResolved;
    
    /** The drawable resource ID for the icon representing the alert type. */
    @DrawableRes
    public int iconRes;

    /**
     * Constructs a new AlertUiModel.
     *
     * @param id            Unique alert ID.
     * @param typeId        Type identifier.
     * @param title         Display title.
     * @param thresholdText Threshold description.
     * @param fieldAddress  Location of the field.
     * @param timeLabel     Timing label.
     * @param isResolved    Resolution status.
     * @param iconRes       Icon resource ID.
     */
    public AlertUiModel(long id,
                        String typeId,
                        String title,
                        String thresholdText,
                        String fieldAddress,
                        String timeLabel,
                        boolean isResolved,
                        int iconRes) {
        this.id = id;
        this.typeId = typeId;
        this.title = title;
        this.thresholdText = thresholdText;
        this.fieldAddress = fieldAddress;
        this.timeLabel = timeLabel;
        this.isResolved = isResolved;
        this.iconRes = iconRes;
    }
}
