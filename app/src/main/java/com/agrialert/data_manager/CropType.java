package com.agrialert.data_manager;

import android.content.Context;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;

import com.agrialert.R;

public enum CropType {
    VEGETABLES(R.string.crop_vegetables, R.drawable.ic_ortaggi),
    CEREALS(R.string.crop_cereals, R.drawable.ic_cereali),
    LEGUMES(R.string.crop_legumes, R.drawable.ic_leguminose),
    ORCHARDS(R.string.crop_orchards, R.drawable.ic_frutteti),
    OILSEEDS(R.string.crop_Oilseeds, R.drawable.ic_oleaginose),
    AROMATIC_AND_MEDICINAL(R.string.crop_aromatic_and_medicinal, R.drawable.ic_aromatiche),
    NONE(R.string.crop_none, R.drawable.ic_group_default);


    private final int resourceId;
    @DrawableRes
    private final int imageResId;


    CropType(int resourceId, @DrawableRes int imageResId) {
        this.resourceId = resourceId;
        this.imageResId = imageResId;
    }

    public int getResourceId() {
        return resourceId;
    }

    public int getImageResId() {
        return imageResId;
    }

    public static CropType getFromName(@NonNull String displayName, @NonNull Context ctx) {
        for (CropType type : values()) {
           if (ctx.getString(type.resourceId).equals(displayName)) {
               return type;
           }
        }
        return NONE;
    }

}
