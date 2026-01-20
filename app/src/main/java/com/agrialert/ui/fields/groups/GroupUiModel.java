package com.agrialert.ui.fields.groups;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;

import java.util.List;

/**
 * UI Model representing a field group.
 * This model holds the information necessary to display a group's details,
 * including its name, description, and associated alert icons.
 * It implements {@link Parcelable} to allow passing between navigation destinations.
 */
public class GroupUiModel implements Parcelable {

    /** Unique identifier for the group. */
    public long id;

    /** The name of the group. */
    public String name;

    /** A brief description of the group's purpose or characteristics. */
    public String description;

    /** Drawable resource ID for the main icon representing the group. */
    @DrawableRes
    public int iconRes;

    /** List of drawable resource IDs for alert icons associated with the fields in this group. */
    public List<Integer> icons;

    /**
     * Constructs a new GroupUiModel.
     *
     * @param id          The unique group ID.
     * @param name        The name of the group.
     * @param description The group's description.
     * @param iconRes     The main icon resource ID.
     * @param icons       List of associated alert icon resource IDs.
     */
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

    /**
     * Constructor for creating a GroupUiModel from a Parcel.
     *
     * @param in The Parcel to read the object's data from.
     */
    protected GroupUiModel(Parcel in) {
        id = in.readLong();
        name = in.readString();
        description = in.readString();
        iconRes = in.readInt();
        icons = in.readArrayList(Integer.class.getClassLoader());
    }

    /**
     * Creator constant for generating instances of GroupUiModel from a Parcel.
     */
    public static final Creator<GroupUiModel> CREATOR = new Creator<>() {
        @Override
        public GroupUiModel createFromParcel(Parcel in) {
            return new GroupUiModel(in);
        }

        @Override
        public GroupUiModel[] newArray(int size) {
            return new GroupUiModel[size];
        }
    };

    /**
     * Describes the kinds of special objects contained in this Parcelable instance's marshaled representation.
     *
     * @return A bitmask indicating the set of special object types marshaled by this Parcelable object instance.
     */
    @Override
    public int describeContents() {
        return 0;
    }

    /**
     * Flattens this object in to a Parcel.
     *
     * @param dest  The Parcel in which the object should be written.
     * @param flags Additional flags about how the object should be written.
     */
    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(name);
        dest.writeString(description);
        dest.writeInt(iconRes);
        dest.writeList(icons);
    }
}
