package com.dhis2.usescases.programDetail;

import org.hisp.dhis.android.core.enrollment.EnrollmentModel;
import org.hisp.dhis.android.core.event.EventModel;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeValueModel;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstanceModel;

import java.util.List;

public class MyTrackedEntityInstance {
    private TrackedEntityInstanceModel trackedEntityInstance;
    private List<TrackedEntityAttributeValueModel> trackedEntityAttributeValues;
    private List<EnrollmentModel> enrollments;
    private List<EventModel> eventModels;

    MyTrackedEntityInstance(TrackedEntityInstanceModel trackedEntityInstance) {
        this.trackedEntityInstance = trackedEntityInstance;
    }

    public void setTrackedEntityAttributeValues(List<TrackedEntityAttributeValueModel> trackedEntityAttributeValues) {
        this.trackedEntityAttributeValues = trackedEntityAttributeValues;
    }

    public void setEnrollments(List<EnrollmentModel> enrollments) {
        this.enrollments = enrollments;
    }

    public List<EventModel> getEventModels() {
        return eventModels;
    }

    public void setEventModels(List<EventModel> eventModels) {
        this.eventModels = eventModels;
    }

    public List<EnrollmentModel> getEnrollments() {
        return enrollments;
    }

    public TrackedEntityInstanceModel getTrackedEntityInstance() {
        return trackedEntityInstance;
    }

    public List<TrackedEntityAttributeValueModel> getTrackedEntityAttributeValues() {
        return trackedEntityAttributeValues;
    }
}
