package com.agrialert.ui.fields.groups;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import java.util.List;

public class GroupUiModel implements Parcelable {

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

    protected GroupUiModel(Parcel in) {
        id = in.readLong();
        name = in.readString();
        description = in.readString();
        iconRes = in.readInt();
        icons = in.readArrayList(Integer.class.getClassLoader());
    }

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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(name);
        dest.writeString(description);
        dest.writeInt(iconRes);
        dest.writeList(icons);
    }
}


