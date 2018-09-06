package org.dhis2.usescases.searchTrackEntity.adapters;

import org.hisp.dhis.android.core.enrollment.EnrollmentModel;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeValueModel;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstanceModel;

import java.util.ArrayList;
import java.util.List;

public class SearchTeiModel {


    private TrackedEntityInstanceModel tei;
    private List<EnrollmentModel> enrollments;
    private boolean hasOverdue;
    private boolean isOnline;
    private List<TrackedEntityAttributeValueModel> attributeValues;

    public SearchTeiModel(TrackedEntityInstanceModel tei, List<TrackedEntityAttributeValueModel> attributeValues) {
        this.tei = tei;
        this.enrollments = new ArrayList<>();
        this.attributeValues = new ArrayList<>();
        this.attributeValues.addAll(attributeValues);
        this.isOnline = true;
    }


    public TrackedEntityInstanceModel getTei() {
        return tei;
    }

    public List<EnrollmentModel> getEnrollments() {
        return enrollments;
    }

    public void setEnrollments(List<EnrollmentModel> enrollments) {
        this.enrollments = enrollments;
    }

    public void addEnrollment(EnrollmentModel enrollmentModel) {
        this.enrollments.add(enrollmentModel);
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
        this.attributeValues.clear();
    }

    public List<TrackedEntityAttributeValueModel> getAttributeValues() {
        return attributeValues;
    }

    public void addAttributeValues(TrackedEntityAttributeValueModel attributeValues) {
        this.attributeValues.add(attributeValues);
    }

    public void resetEnrollments() {
        this.enrollments.clear();
    }
}
