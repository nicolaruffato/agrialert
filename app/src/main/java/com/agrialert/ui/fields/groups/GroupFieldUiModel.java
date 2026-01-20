package com.agrialert.ui.fields.groups;

import androidx.annotation.DrawableRes;

/**
 * UI Model representing a field within the context of group management.
 * This model holds the information necessary to display a field in a selection list,
 * including its association with a group and its selection state.
 */
public class GroupFieldUiModel {

    /** Unique identifier for the field. */
    public long id;
    
    /** Drawable resource ID for the icon representing the crop type. */
    @DrawableRes
    public int iconRes;
    
    /** The physical address or location description of the field. */
    public String address;
    
    /** The name of the crop planted in the field. */
    public String crop;
    
    /** The name of the group the field currently belongs to. */
    public String groupName;
    
    /** Indicates whether this field is selected for group assignment or modification. */
    public boolean selected;

    /**
     * Constructs a new GroupFieldUiModel.
     *
     * @param id        The unique field ID.
     * @param iconRes   The icon resource ID.
     * @param address   The field's address.
     * @param crop      The type of crop.
     * @param groupName The name of its current group.
     * @param selected  Initial selection state.
     */
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
