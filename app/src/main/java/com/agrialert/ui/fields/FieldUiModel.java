package com.agrialert.ui.fields;

import java.util.List;

public class FieldUiModel {

    public long id;
    public String address;
    public String crop;          // es. "Ortaggi"
    public String groupName;     // es. "Gruppo: Prova"
    public int iconRes;          // icona grande della coltura
    public List<Integer> icons;  // iconcine degli alert associati (max 6)

    public FieldUiModel(long id,
                        String address,
                        String crop,
                        String groupName,
                        int iconRes,
                        List<Integer> icons) {
        this.id = id;
        this.address = address;
        this.crop = crop;
        this.groupName = groupName;
        this.iconRes = iconRes;
        this.icons = icons;
    }
}
