package dhis2.org.analytics.charts.bindings

import dhis2.org.analytics.charts.ui.OrgUnitFilterType
import org.hisp.dhis.android.core.analytics.trackerlinelist.DataFilter
import org.hisp.dhis.android.core.analytics.trackerlinelist.DateFilter
import org.hisp.dhis.android.core.analytics.trackerlinelist.EnumFilter
import org.hisp.dhis.android.core.analytics.trackerlinelist.OrganisationUnitFilter
import org.hisp.dhis.android.core.analytics.trackerlinelist.TrackerLineListItem
import org.hisp.dhis.android.core.common.RelativeOrganisationUnit
import org.hisp.dhis.android.core.common.RelativePeriod

fun TrackerLineListItem.withFilters(
    value: String,
    categories: List<String> = emptyList(),
): TrackerLineListItem =
    when (this) {
        TrackerLineListItem.CreatedBy -> this
        TrackerLineListItem.LastUpdatedBy -> this
        is TrackerLineListItem.EnrollmentDate ->
            this.copy(
                filters =
                    this.filters +
                        listOf(
                            DateFilter.Like(value),
                        ),
            )

        is TrackerLineListItem.EventDate ->
            this.copy(
                filters =
                    this.filters +
                        listOf(
                            DateFilter.Like(value),
                        ),
            )

        is TrackerLineListItem.ScheduledDate ->
            this.copy(
                filters =
                    this.filters +
                        listOf(
                            DateFilter.Like(value),
                        ),
            )

        is TrackerLineListItem.EventStatusItem ->
            this.copy(
                filters =
                    this.filters +
                        listOf(
                            EnumFilter.Like(value),
                        ),
            )

        is TrackerLineListItem.IncidentDate ->
            this.copy(
                filters =
                    this.filters +
                        listOf(
                            DateFilter.Like(value),
                        ),
            )

        is TrackerLineListItem.LastUpdated ->
            this.copy(
                filters =
                    this.filters +
                        listOf(
                            DateFilter.Like(value),
                        ),
            )

        is TrackerLineListItem.OrganisationUnitItem ->
            this.copy(
                filters =
                    this.filters +
                        listOf(
                            OrganisationUnitFilter.Like(value),
                        ),
            )

        is TrackerLineListItem.ProgramAttribute ->
            this.copy(
                filters =
                    this.filters +
                        listOf(
                            DataFilter.Like(value),
                        ),
            )

        is TrackerLineListItem.ProgramDataElement ->
            this.copy(
                filters =
                    this.filters +
                        listOf(
                            DataFilter.Like(value),
                        ),
            )

        is TrackerLineListItem.ProgramIndicator ->
            this.copy(
                filters =
                    this.filters +
                        listOf(
                            DataFilter.Like(value),
                        ),
            )

        is TrackerLineListItem.ProgramStatusItem ->
            this.copy(
                filters =
                    this.filters +
                        listOf(
                            EnumFilter.Like(value),
                        ),
            )
        is TrackerLineListItem.Category ->
            this.copy(
                filters =
                    this.filters +
                        listOf(
                            DataFilter.In(categories),
                        ),
            )
    }

fun TrackerLineListItem.withDateFilters(periods: List<RelativePeriod>): TrackerLineListItem =
    when (this) {
        is TrackerLineListItem.EnrollmentDate ->
            this.copy(filters = this.filters + periods.map { DateFilter.Relative(it) })

        is TrackerLineListItem.EventDate ->
            this.copy(filters = this.filters + periods.map { DateFilter.Relative(it) })

        is TrackerLineListItem.IncidentDate ->
            this.copy(filters = this.filters + periods.map { DateFilter.Relative(it) })

        is TrackerLineListItem.LastUpdated ->
            this.copy(filters = this.filters + periods.map { DateFilter.Relative(it) })

        is TrackerLineListItem.ScheduledDate ->
            this.copy(filters = this.filters + periods.map { DateFilter.Relative(it) })

        else -> this
    }

fun TrackerLineListItem.withOUFilters(
    orgUnitFilterType: OrgUnitFilterType,
    orgUnitUids: List<String>,
): TrackerLineListItem =
    when (this) {
        is TrackerLineListItem.OrganisationUnitItem ->
            when (orgUnitFilterType) {
                OrgUnitFilterType.NONE -> this
                OrgUnitFilterType.ALL ->
                    this.copy(
                        filters =
                            this.filters +
                                listOf(
                                    OrganisationUnitFilter.Relative(
                                        RelativeOrganisationUnit.USER_ORGUNIT,
                                    ),
                                ),
                    )

                OrgUnitFilterType.SELECTION ->
                    this.copy(
                        filters =
                            this.filters +
                                orgUnitUids.map {
                                    OrganisationUnitFilter.Absolute(it)
                                },
                    )
            }

        else -> this
    }

object Label {
    const val ORGANISATION_UNIT = "ou"
    const val LAST_UPDATED = "lastUpdated"
    const val INCIDENT_DATE = "incidentDate"
    const val ENROLLMENT_DATE = "enrollmentDate"
    const val SCHEDULED_DATE = "scheduledDate"
}
