package com.agrialert.ui.alerts;

public class AlertUiModel {

    public long id;              // id univoco dell'alert
    public String typeId;        // es. "VENTO_FORTE", "ONDATA_CALORE", ... (le 10 tipologie del D3)
    public String title;         // es. "Vento Forte"
    public String thresholdText; // soglia di default, es. "Vento > 50 Km/h"
    public String fieldAddress;  // es. "Via Verdirdi, 15 - Mestre (VE)"
    public String timeLabel;     // es. "Oggi", "Domani", "Tra 5 giorni"
    public boolean isResolved;   // stato dello switch
    public int iconRes;          // icona del tipo di alert (vento, gelo, ecc.)

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
