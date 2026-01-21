package com.agrialert.ui.fields;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;

import java.util.List;

/**
 * UI Model representing an agricultural field.
 * This model holds the data needed to display field information in the UI,
 * including its location, crop type, and associated group and alerts.
 * It implements {@link Parcelable} to allow passing between navigation destinations.
 */
public class FieldUiModel implements Parcelable {

    /** The unique identifier of the field. */
    public long id;
    
    /** The physical address or location name of the field. */
    public String address;
    
    /** The latitude coordinate of the field. */
    public Double latitude;
    
    /** The longitude coordinate of the field. */
    public Double longitude;
    
    /** The type of crop planted in the field (e.g., "Vegetables"). */
    public String crop;
    
    /** The name of the group this field belongs to (e.g., "Group: Test"). */
    public String groupName;
    
    /** The drawable resource ID for the crop's icon. */
    @DrawableRes
    public int iconRes;
    
    /** A list of drawable resource IDs for alert icons associated with this field (max 6). */
    public List<Integer> icons;

    /**
     * Constructs a new FieldUiModel without coordinates.
     *
     * @param id        The field's unique ID.
     * @param address   The field's address.
     * @param crop      The crop type name.
     * @param groupName The name of the field's group.
     * @param iconRes   The icon resource ID for the crop.
     * @param icons     The list of associated alert icons.
     */
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

    /**
     * Constructs a new FieldUiModel with coordinates.
     *
     * @param id        The field's unique ID.
     * @param address   The field's address.
     * @param lat       The field's latitude.
     * @param lon       The field's longitude.
     * @param crop      The crop type name.
     * @param groupName The name of the field's group.
     * @param iconRes   The icon resource ID for the crop.
     * @param icons     The list of associated alert icons.
     */
    public FieldUiModel(long id,
                        String address, double lat, double lon,
                        String crop,
                        String groupName,
                        int iconRes,
                        List<Integer> icons) {
        this.id = id;
        this.address = address;
        this.latitude = lat;
        this.longitude = lon;
        this.crop = crop;
        this.groupName = groupName;
        this.iconRes = iconRes;
        this.icons = icons;
    }

    /**
     * Constructor used by the Parcelable CREATOR to reconstruct the object from a Parcel.
     *
     * @param in The Parcel containing the object's data.
     */
    protected FieldUiModel(Parcel in) {
        id = in.readLong();
        address = in.readString();
        crop = in.readString();
        groupName = in.readString();
        iconRes = in.readInt();
    }

    /**
     * Creator constant for generating instances of FieldUiModel from a Parcel.
     */
    public static final Creator<FieldUiModel> CREATOR = new Creator<>() {
        @Override
        public FieldUiModel createFromParcel(Parcel in) {
            return new FieldUiModel(in);
        }

        @Override
        public FieldUiModel[] newArray(int size) {
            return new FieldUiModel[size];
        }
    };

    /**
     * Describes the kinds of special objects contained in this Parcelable instance.
     *
     * @return A bitmask indicating the set of special object types marshaled by this Parcelable.
     */
    @Override
    public int describeContents() {
        return 0;
    }

    /**
     * Flattens this object into a Parcel.
     *
     * @param dest  The Parcel in which the object should be written.
     * @param flags Additional flags about how the object should be written.
     */
    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(address);
        dest.writeString(crop);
        dest.writeString(groupName);
        dest.writeInt(iconRes);
    }
}
