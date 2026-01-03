package com.agrialert.data_manager;

import com.agrialert.R;

/**
 * Represents the various categories of crops supported by the application.
 * Each enum constant is associated with a specific string resource ID for localization.
 */
public enum CropType {
    VEGETABLES(R.string.crop_vegetables),
    CEREALS(R.string.crop_cereals),
    LEGUMES(R.string.crop_legumes),
    ORCHARDS(R.string.crop_orchards),
    OILSEEDS(R.string.crop_Oilseeds),
    AROMATIC_AND_MEDICINAL(R.string.crop_aromatic_and_medicinal),
    NONE(R.string.crop_none);


    private final int resourceId;

    CropType(int resourceId) {
        this.resourceId = resourceId;
    }

    public int getResourceId() {
        return resourceId;
    }
}
