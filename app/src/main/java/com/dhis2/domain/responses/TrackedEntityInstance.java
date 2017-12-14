package com.dhis2.domain.responses;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

/**
 * Created by ppajuelo on 14/12/2017.
 */

public class TrackedEntityInstance implements Parcelable {

    private String trackedEntity;
    private String created;
    private String orgUnit;
    private String trackedEntityInstance;
    private String lastUpdated;
    private boolean deleted;
    private List<org.hisp.dhis.android.core.enrollment.Enrollment> enrollments;
    private List<Relationship> relationships;
    private List<Attribute> attributes;


    protected TrackedEntityInstance(Parcel in) {
        trackedEntity = in.readString();
        created = in.readString();
        orgUnit = in.readString();
        trackedEntityInstance = in.readString();
        lastUpdated = in.readString();
        deleted = in.readByte() != 0;
        relationships = in.createTypedArrayList(Relationship.CREATOR);
        attributes = in.createTypedArrayList(Attribute.CREATOR);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(trackedEntity);
        dest.writeString(created);
        dest.writeString(orgUnit);
        dest.writeString(trackedEntityInstance);
        dest.writeString(lastUpdated);
        dest.writeByte((byte) (deleted ? 1 : 0));
        dest.writeTypedList(relationships);
        dest.writeTypedList(attributes);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<TrackedEntityInstance> CREATOR = new Creator<TrackedEntityInstance>() {
        @Override
        public TrackedEntityInstance createFromParcel(Parcel in) {
            return new TrackedEntityInstance(in);
        }

        @Override
        public TrackedEntityInstance[] newArray(int size) {
            return new TrackedEntityInstance[size];
        }
    };

    public String getTrackedEntity() {
        return trackedEntity;
    }

    public String getCreated() {
        return created;
    }

    public String getOrgUnit() {
        return orgUnit;
    }

    public String getTrackedEntityInstance() {
        return trackedEntityInstance;
    }

    public String getLastUpdated() {
        return lastUpdated;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public List<org.hisp.dhis.android.core.enrollment.Enrollment> getEnrollments() {
        return enrollments;
    }

    public List<Relationship> getRelationships() {
        return relationships;
    }

    public List<Attribute> getAttributes() {
        return attributes;
    }
}
