package org.dhis2.usescases.searchTrackEntity.adapters;

import org.dhis2.data.tuples.Trio;
import org.hisp.dhis.android.core.enrollment.Enrollment;
import org.hisp.dhis.android.core.enrollment.EnrollmentModel;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeValueModel;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstance;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstanceModel;

import java.util.ArrayList;
import java.util.List;

public class SearchTeiModel {


    private TrackedEntityInstanceModel teiModel; //7

    private List<TrackedEntityAttributeValueModel> attributeValueModels; //3,4
    private List<EnrollmentModel> enrollmentModels;

    private List<Trio<String, String, String>> enrollmentsInfo;//2
    private boolean hasOverdue; //6
    private boolean isOnline;//8

    private TrackedEntityInstance tei;
    private String profilePictureUid;
    private String defaultTypeIcon;

    private Enrollment selectedEnrollment;

    public SearchTeiModel() {
        this.tei = null;
        this.selectedEnrollment = null;
        this.attributeValueModels = new ArrayList<>();
        this.enrollmentModels = new ArrayList<>();
        this.enrollmentsInfo = new ArrayList<>();
        this.isOnline = true;
    }

    public TrackedEntityInstanceModel getTeiModel() {
        return teiModel;
    }

    public List<EnrollmentModel> getEnrollmentModels() {
        return enrollmentModels;
    }

    public void setEnrollmentModels(List<EnrollmentModel> enrollmentModels) {
        this.enrollmentModels = enrollmentModels;
    }

    public void addEnrollment(EnrollmentModel enrollmentModel) {
        this.enrollmentModels.add(enrollmentModel);
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
        //this.attributeValues.clear();
    }

    public List<TrackedEntityAttributeValueModel> getAttributeValueModels() {
        return attributeValueModels;
    }

    public void addAttributeValuesModels(TrackedEntityAttributeValueModel attributeValues) {
        this.attributeValueModels.add(attributeValues);
    }

    public void resetEnrollments() {
        this.enrollmentModels.clear();
        this.enrollmentsInfo.clear();
    }

    public List<Trio<String, String, String>> getEnrollmentInfo() {
        return enrollmentsInfo;
    }

    public void toLocalTei(TrackedEntityInstanceModel localTei) {
        this.teiModel = localTei;
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
}
