package org.dhis2.usescases.searchTrackEntity.adapters;

import org.dhis2.data.tuples.Trio;
import org.hisp.dhis.android.core.enrollment.Enrollment;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeValueModel;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstance;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SearchTeiModel {

    private List<TrackedEntityAttributeValueModel> attributeValueModels;

    private List<Trio<String, String, String>> enrollmentsInfo;
    private boolean hasOverdue;
    private boolean isOnline;

    private TrackedEntityInstance tei;
    private String profilePictureUid;
    private String defaultTypeIcon;

    private Enrollment selectedEnrollment;
    private List<Enrollment> enrollments;

    public SearchTeiModel() {
        this.tei = null;
        this.selectedEnrollment = null;
        this.attributeValueModels = new ArrayList<>();
        this.enrollmentsInfo = new ArrayList<>();
        this.isOnline = true;
        this.enrollments = new ArrayList<>();
    }


    public void addEnrollmentInfo(Trio<String, String, String> enrollmentInfo) {
        enrollmentsInfo.add(enrollmentInfo);
    }

    public boolean isHasOverdue() {
        return hasOverdue;
    }

    public void setHasOverdue(boolean hasOverdue) {
        this.hasOverdue = hasOverdue;
    }

    public boolean isOnline() {
        return isOnline;
    }

    public void setOnline(boolean online) {
        isOnline = online;
        this.attributeValueModels.clear();
    }

    public List<TrackedEntityAttributeValueModel> getAttributeValueModels() {
        return attributeValueModels;
    }

    public void addAttributeValuesModels(TrackedEntityAttributeValueModel attributeValues) {
        this.attributeValueModels.add(attributeValues);
    }

    public void resetEnrollments() {
        this.enrollments.clear();
        this.enrollmentsInfo.clear();
    }

    public List<Trio<String, String, String>> getEnrollmentInfo() {
        Collections.sort(enrollmentsInfo, (enrollment1, enrollment2) -> enrollment1.val0().compareToIgnoreCase(enrollment2.val0()));
        return enrollmentsInfo;
    }

    public void setAttributeValueModels(List<TrackedEntityAttributeValueModel> attributeValueModels) {
        this.attributeValueModels = attributeValueModels;
    }


    public void setTei(TrackedEntityInstance tei) {
        this.tei = tei;
    }

    public TrackedEntityInstance getTei() {
        return tei;
    }

    public void setProfilePicture(String profilePictureUid) {
        this.profilePictureUid = profilePictureUid;
    }

    public String getProfilePictureUid() {
        return profilePictureUid;
    }

    public void setDefaultTypeIcon(String defaultTypeIcon) {
        this.defaultTypeIcon = defaultTypeIcon;
    }

    public String getDefaultTypeIcon() {
        return defaultTypeIcon;
    }

    public void setCurrentEnrollment(Enrollment enrollment) {
        this.selectedEnrollment = enrollment;
    }

    public Enrollment getSelectedEnrollment(){
        return this.selectedEnrollment;
    }

    public void addEnrollment(Enrollment enrollment) {
        this.enrollments.add(enrollment);
    }

    public List<Enrollment> getEnrollments() {
        return enrollments;
    }
}
