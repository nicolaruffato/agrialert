package com.agrialert.ui.alerts;

public class AlertSettingUiModel {

    public long id;
    public int iconRes;
    public String title;
    public String description;

    public boolean enabled;

    // soglia 1
    public boolean hasPrimaryThreshold;
    public String primaryLabel;
    public int primaryValue;
    public String primaryUnit;

    // soglia 2 (opzionale)
    public boolean hasSecondaryThreshold;
    public String secondaryLabel;
    public int secondaryValue;
    public String secondaryUnit;

    public AlertSettingUiModel(long id,
                               int iconRes,
                               String title,
                               String description,
                               boolean enabled,
                               boolean hasPrimaryThreshold,
                               String primaryLabel,
                               int primaryValue,
                               String primaryUnit,
                               boolean hasSecondaryThreshold,
                               String secondaryLabel,
                               int secondaryValue,
                               String secondaryUnit) {

        this.id = id;
        this.iconRes = iconRes;
        this.title = title;
        this.description = description;
        this.enabled = enabled;

        this.hasPrimaryThreshold = hasPrimaryThreshold;
        this.primaryLabel = primaryLabel;
        this.primaryValue = primaryValue;
        this.primaryUnit = primaryUnit;

        this.hasSecondaryThreshold = hasSecondaryThreshold;
        this.secondaryLabel = secondaryLabel;
        this.secondaryValue = secondaryValue;
        this.secondaryUnit = secondaryUnit;
    }
}
