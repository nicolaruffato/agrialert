package com.agrialert.ui.fields.groups;

import java.util.List;

public class GroupUiModel {

    public long id;
    public String name;
    public String description;
    public int iconRes;          // icona grande
    public List<Integer> icons;  // icone alert associate

    public GroupUiModel(long id,
                        String name,
                        String description,
                        int iconRes,
                        List<Integer> icons) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.iconRes = iconRes;
        this.icons = icons;
    }
}


