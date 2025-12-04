package com.agrialert.ui.fields;

import java.util.List;

public class FieldUiModel {

    public long id;                 // id del campo (in futuro dal DB)
    public String address;          // indirizzo / nome campo inserito dal cliente
    public String cropType;         // coltura (Ortaggi, Cereali, Leguminose, ecc.)
    public String groupName;        // nome gruppo (può essere null o vuoto)
    public int iconRes;             // icona principale in base alla coltura
    public List<Integer> alertIcons; // lista di icone alert/meteo (max 6)

    public FieldUiModel(long id,
                        String address,
                        String cropType,
                        String groupName,
                        int iconRes,
                        List<Integer> alertIcons) {
        this.id = id;
        this.address = address;
        this.cropType = cropType;
        this.groupName = groupName;
        this.iconRes = iconRes;
        this.alertIcons = alertIcons;
    }
}
