package com.agrialert.data_manager;

import com.agrialert.R;

public enum CropType {
    VEGETABLES(R.string.crop_vegetables),
    CEREALS(R.string.crop_cereals),
    LEGUMES(R.string.crop_legumes),
    ORCHARDS(R.string.crop_orchards),
    OILSEEDS(R.string.crop_Oilseeds),
    AROMATIC_AND_MEDICINAL(R.string.crop_aromatic_and_medicinal);


    private final int resourceId;

    CropType(int resourceId) {
        this.resourceId = resourceId;
    }

    public int getResourceId() {
        return resourceId;
    }
}
