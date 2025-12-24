package com.agrialert.data_manager;

import androidx.room.TypeConverter;

class Converters {

    @TypeConverter
    public static String fromCropType(CropType value) {
        return value == null ? null : value.name();
    }

    @TypeConverter
    public static CropType toCropType(String value) {
        return value == null ? null : CropType.valueOf(value);
    }

}
