package org.dhis2.domain.responses;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by ppajuelo on 14/12/2017.
 */

public class Attribute implements Parcelable {
    private String lastUpdated;
    private String displayName;
    private String created;
    private String valueType;
    private String attribute;
    private String value;

    protected Attribute(Parcel in) {
        lastUpdated = in.readString();
        displayName = in.readString();
        created = in.readString();
        valueType = in.readString();
        attribute = in.readString();
        value = in.readString();
    }

    public static final Creator<Attribute> CREATOR = new Creator<Attribute>() {
        @Override
        public Attribute createFromParcel(Parcel in) {
            return new Attribute(in);
        }

        @Override
        public Attribute[] newArray(int size) {
            return new Attribute[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(lastUpdated);
        parcel.writeString(displayName);
        parcel.writeString(created);
        parcel.writeString(valueType);
        parcel.writeString(attribute);
        parcel.writeString(value);
    }

    public String getLastUpdated() {
        return lastUpdated;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getCreated() {
        return created;
    }

    public String getValueType() {
        return valueType;
    }

    public String getAttribute() {
        return attribute;
    }

    public String getValue() {
        return value;
    }
}
