package com.dhis2.domain.responses;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

/**
 * Created by ppajuelo on 18/01/2018.
 */

public class TEIResponse implements Parcelable {

    private List<TrackedEntityInstance> trackedEntityInstances;

    protected TEIResponse(Parcel in) {
        trackedEntityInstances = in.createTypedArrayList(TrackedEntityInstance.CREATOR);
    }

    public TEIResponse() {

    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeTypedList(trackedEntityInstances);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<TEIResponse> CREATOR = new Creator<TEIResponse>() {
        @Override
        public TEIResponse createFromParcel(Parcel in) {
            return new TEIResponse(in);
        }

        @Override
        public TEIResponse[] newArray(int size) {
            return new TEIResponse[size];
        }
    };

    public List<TrackedEntityInstance> getTrackedEntityInstances() {
        return trackedEntityInstances;
    }
}
