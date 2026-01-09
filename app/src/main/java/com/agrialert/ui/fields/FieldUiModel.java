package com.agrialert.ui.fields;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import java.util.List;

public class FieldUiModel implements Parcelable {

    public long id;
    public String address;
    public Double latitude;
    public Double longitude;
    public String crop;          // es. "Ortaggi"
    public String groupName;     // es. "Gruppo: Prova"
    public int iconRes;          // icona grande della coltura
    public List<Integer> icons;  // iconcine degli alert associati (max 6)

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

    protected FieldUiModel(Parcel in) {
        id = in.readLong();
        address = in.readString();
        crop = in.readString();
        groupName = in.readString();
        iconRes = in.readInt();
    }

    public static final Creator<FieldUiModel> CREATOR = new Creator<FieldUiModel>() {
        @Override
        public FieldUiModel createFromParcel(Parcel in) {
            return new FieldUiModel(in);
        }

        @Override
        public FieldUiModel[] newArray(int size) {
            return new FieldUiModel[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(address);
        dest.writeString(crop);
        dest.writeString(groupName);
        dest.writeInt(iconRes);
    }
}
