package com.agrialert.ui.fields.groups;

import java.util.List;

public class GroupUiModel {

    public long id;
    public String name;
    public String description;
    public int iconRes;         // icona grande a sinistra
    public List<Integer> icons; // piccole iconcine meteo sotto al testo

    public GroupUiModel(long id, String name, String description, int iconRes, List<Integer> icons) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.iconRes = iconRes;
        this.icons = icons;
    }
}

