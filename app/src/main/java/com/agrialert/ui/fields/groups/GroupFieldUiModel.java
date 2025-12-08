package com.agrialert.ui.fields.groups;

public class GroupFieldUiModel {

    public long id;
    public int iconRes;
    public String address;
    public String crop;
    public String groupName; // gruppo attuale del campo (se già assegnato)
    public boolean selected;

    public GroupFieldUiModel(long id,
                             int iconRes,
                             String address,
                             String crop,
                             String groupName,
                             boolean selected) {
        this.id = id;
        this.iconRes = iconRes;
        this.address = address;
        this.crop = crop;
        this.groupName = groupName;
        this.selected = selected;
    }
}
