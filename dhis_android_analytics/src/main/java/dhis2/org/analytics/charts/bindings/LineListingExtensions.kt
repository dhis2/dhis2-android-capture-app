package dhis2.org.analytics.charts.bindings

import org.hisp.dhis.android.core.analytics.trackerlinelist.DataFilter
import org.hisp.dhis.android.core.analytics.trackerlinelist.DateFilter
import org.hisp.dhis.android.core.analytics.trackerlinelist.OrganisationUnitFilter
import org.hisp.dhis.android.core.analytics.trackerlinelist.TrackerLineListItem
import org.hisp.dhis.android.core.enrollment.EnrollmentStatus
import org.hisp.dhis.android.core.event.EventStatus

fun TrackerLineListItem.withFilters(value: String): TrackerLineListItem {
    return when (this) {
        TrackerLineListItem.CreatedBy -> this
        TrackerLineListItem.LastUpdatedBy -> this
        is TrackerLineListItem.EnrollmentDate -> this.copy(
            filters = listOf(
                DateFilter.Range(value, value),
            ),
        )

        is TrackerLineListItem.EventDate -> this.copy(
            filters = listOf(
                DateFilter.Range(value, value),
            ),
        )

        is TrackerLineListItem.ScheduledDate -> this.copy(
            filters = listOf(
                DateFilter.Range(value, value),
            ),
        )

        is TrackerLineListItem.EventStatusItem -> this.copy(
            filters = listOf(
                EventStatus.valueOf(value),
            ),
        )

        is TrackerLineListItem.IncidentDate -> this.copy(
            filters = listOf(
                DateFilter.Range(value, value),
            ),
        )

        is TrackerLineListItem.LastUpdated -> this.copy(
            filters = listOf(
                DateFilter.Range(value, value),
            ),
        )

        is TrackerLineListItem.OrganisationUnitItem -> this.copy(
            filters = listOf(
                OrganisationUnitFilter.Absolute(value),
            ),
        )

        is TrackerLineListItem.ProgramAttribute -> this.copy(
            filters = listOf(
                DataFilter.EqualTo(value),
            ),
        )

        is TrackerLineListItem.ProgramDataElement -> this.copy(
            filters = listOf(
                DataFilter.EqualTo(value),
            ),
        )

        is TrackerLineListItem.ProgramIndicator -> this.copy(
            filters = listOf(
                DataFilter.EqualTo(value),
            ),
        )

        is TrackerLineListItem.ProgramStatusItem -> this.copy(
            filters = listOf(
                EnrollmentStatus.valueOf(value),
            ),
        )
    }
}
