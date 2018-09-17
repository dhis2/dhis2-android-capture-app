package org.dhis2.domain.responses;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

/**
 * Created by ppajuelo on 14/12/2017.
 */

public class Enrollment implements Parcelable {
    private String trackedEntity;
    private String created;
    private String orgUnit;
    private String createdAtClient;
    private String program;
    private String trackedEntityInstance;
    private String enrollment;
    private String lastUpdated;
    private String orgUnitName;
    private String enrollmentDate;
    private boolean followup;
    private String incidentDate;
    private String status;
    private List<Attribute> attributes;

    protected Enrollment(Parcel in) {
        trackedEntity = in.readString();
        created = in.readString();
        orgUnit = in.readString();
        createdAtClient = in.readString();
        program = in.readString();
        trackedEntityInstance = in.readString();
        enrollment = in.readString();
        lastUpdated = in.readString();
        orgUnitName = in.readString();
        enrollmentDate = in.readString();
        followup = in.readByte() != 0;
        incidentDate = in.readString();
        status = in.readString();
        attributes = in.createTypedArrayList(Attribute.CREATOR);
    }

    public static final Creator<Enrollment> CREATOR = new Creator<Enrollment>() {
        @Override
        public Enrollment createFromParcel(Parcel in) {
            return new Enrollment(in);
        }

        @Override
        public Enrollment[] newArray(int size) {
            return new Enrollment[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(trackedEntity);
        parcel.writeString(created);
        parcel.writeString(orgUnit);
        parcel.writeString(createdAtClient);
        parcel.writeString(program);
        parcel.writeString(trackedEntityInstance);
        parcel.writeString(enrollment);
        parcel.writeString(lastUpdated);
        parcel.writeString(orgUnitName);
        parcel.writeString(enrollmentDate);
        parcel.writeByte((byte) (followup ? 1 : 0));
        parcel.writeString(incidentDate);
        parcel.writeString(status);
        parcel.writeTypedList(attributes);
    }

    public String getTrackedEntity() {
        return trackedEntity;
    }

    public String getCreated() {
        return created;
    }

    public String getOrgUnit() {
        return orgUnit;
    }

    public String getCreatedAtClient() {
        return createdAtClient;
    }

    public String getProgram() {
        return program;
    }

    public String getTrackedEntityInstance() {
        return trackedEntityInstance;
    }

    public String getEnrollment() {
        return enrollment;
    }

    public String getLastUpdated() {
        return lastUpdated;
    }

    public String getOrgUnitName() {
        return orgUnitName;
    }

    public String getEnrollmentDate() {
        return enrollmentDate;
    }

    public boolean isFollowup() {
        return followup;
    }

    public String getIncidentDate() {
        return incidentDate;
    }

    public String getStatus() {
        return status;
    }

    public List<Attribute> getAttributes() {
        return attributes;
    }

}
