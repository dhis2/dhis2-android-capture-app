package com.dhis2.domain.responses;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by ppajuelo on 14/12/2017.
 */

public class Relationship implements Parcelable {
    private String trackedEntityInstanceB;
    private String trackedEntityInstanceA;
    private String displayName;
    private TrackedEntityInstance relative;

    protected Relationship(Parcel in) {
        trackedEntityInstanceB = in.readString();
        trackedEntityInstanceA = in.readString();
        displayName = in.readString();
        relative = in.readParcelable(TrackedEntityInstance.class.getClassLoader());
    }

    public static final Creator<Relationship> CREATOR = new Creator<Relationship>() {
        @Override
        public Relationship createFromParcel(Parcel in) {
            return new Relationship(in);
        }

        @Override
        public Relationship[] newArray(int size) {
            return new Relationship[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(trackedEntityInstanceB);
        parcel.writeString(trackedEntityInstanceA);
        parcel.writeString(displayName);
        parcel.writeParcelable(relative, i);
    }

    public String getTrackedEntityInstanceB() {
        return trackedEntityInstanceB;
    }

    public String getTrackedEntityInstanceA() {
        return trackedEntityInstanceA;
    }

    public String getDisplayName() {
        return displayName;
    }

    public TrackedEntityInstance getRelative() {
        return relative;
    }
}
