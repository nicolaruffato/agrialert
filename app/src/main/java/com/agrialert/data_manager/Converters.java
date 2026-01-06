package com.agrialert.data_manager;

import androidx.room.TypeConverter;

/**
 * This class provides methods to convert {@link CropType} enums to their
 * {@link String} representation for database storage and vice versa.
 */
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
