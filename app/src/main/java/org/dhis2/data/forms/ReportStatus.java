package org.dhis2.data.forms;

import org.hisp.dhis.android.core.enrollment.EnrollmentStatus;
import org.hisp.dhis.android.core.event.EventStatus;

enum ReportStatus {
    ACTIVE, COMPLETED;

    static ReportStatus fromEnrollmentStatus(EnrollmentStatus enrollmentStatus) {
        if (enrollmentStatus == EnrollmentStatus.ACTIVE) {
            return ACTIVE;
        }
        return COMPLETED;
    }

    static ReportStatus fromEventStatus(EventStatus eventStatus) {
        if (eventStatus == EventStatus.COMPLETED) {
            return COMPLETED;
        }
        return ACTIVE;
    }

    static EventStatus toEventStatus(ReportStatus reportStatus) {
        if (reportStatus == ACTIVE) {
            return EventStatus.ACTIVE;
        }
        return EventStatus.COMPLETED;
    }

    static EnrollmentStatus toEnrollmentStatus(ReportStatus reportStatus) {
        if (reportStatus == ACTIVE) {
            return EnrollmentStatus.ACTIVE;
        }
        return EnrollmentStatus.COMPLETED;
    }
}