package dhis2.org.analytics.charts.bindings

import dhis2.org.analytics.charts.ui.OrgUnitFilterType
import org.hisp.dhis.android.core.analytics.trackerlinelist.DataFilter
import org.hisp.dhis.android.core.analytics.trackerlinelist.DateFilter
import org.hisp.dhis.android.core.analytics.trackerlinelist.EnumFilter
import org.hisp.dhis.android.core.analytics.trackerlinelist.OrganisationUnitFilter
import org.hisp.dhis.android.core.analytics.trackerlinelist.TrackerLineListItem
import org.hisp.dhis.android.core.common.RelativeOrganisationUnit
import org.hisp.dhis.android.core.common.RelativePeriod

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
                EnumFilter.Like(value),
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
                EnumFilter.Like(value),
            ),
        )
    }
}

fun TrackerLineListItem.withDateFilters(periods: List<RelativePeriod>): TrackerLineListItem {
    return when (this) {
        is TrackerLineListItem.EnrollmentDate ->
            this.copy(filters = periods.map { DateFilter.Relative(it) })

        is TrackerLineListItem.EventDate ->
            this.copy(filters = periods.map { DateFilter.Relative(it) })

        is TrackerLineListItem.IncidentDate ->
            this.copy(filters = periods.map { DateFilter.Relative(it) })

        is TrackerLineListItem.LastUpdated ->
            this.copy(filters = periods.map { DateFilter.Relative(it) })

        is TrackerLineListItem.ScheduledDate ->
            this.copy(filters = periods.map { DateFilter.Relative(it) })

        else -> this
    }
}

fun TrackerLineListItem.withOUFilters(
    orgUnitFilterType: OrgUnitFilterType,
    orgUnitUids: List<String>,
): TrackerLineListItem {
    return when (this) {
        is TrackerLineListItem.OrganisationUnitItem -> when (orgUnitFilterType) {
            OrgUnitFilterType.NONE -> this
            OrgUnitFilterType.ALL ->
                this.copy(
                    filters = listOf(
                        OrganisationUnitFilter.Relative(
                            RelativeOrganisationUnit.USER_ORGUNIT,
                        ),
                    ),
                )

            OrgUnitFilterType.SELECTION ->
                this.copy(
                    filters = orgUnitUids.map {
                        OrganisationUnitFilter.Absolute(it)
                    },
                )
        }

        else -> this
    }
}

object Label {
    const val OrganisationUnit = "ou"
    const val LastUpdated = "lastUpdated"
    const val IncidentDate = "incidentDate"
    const val EnrollmentDate = "enrollmentDate"
    const val ScheduledDate = "scheduledDate"
    const val EventDate = "eventDate"
    const val CreatedBy = "createdBy"
    const val LastUpdatedBy = "lastUpdatedBy"
    const val ProgramStatus = "programStatus"
    const val EventStatus = "eventStatus"
}
