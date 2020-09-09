package org.dhis2.data.sorting

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import org.dhis2.data.enrollment.EnrollmentUiDataHelper
import org.dhis2.usescases.searchTrackEntity.adapters.SearchTeiModel
import org.dhis2.utils.filters.Filters
import org.dhis2.utils.filters.sorting.SortingItem
import org.dhis2.utils.filters.sorting.SortingStatus
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.arch.repositories.scope.RepositoryScope
import org.hisp.dhis.android.core.event.EventStatus

class SearchSortingValueSetter(
    private val d2: D2,
    val unknownLabel: String,
    private val eventDateLabel: String,
    private val orgUnitLabel: String,
    private val enrollmentStatusLabel: String,
    private val enrollmentDateDefaultLabel: String,
    private val uiDateFormat: String,
    private val enrollmentUiDataHelper: EnrollmentUiDataHelper
) {

    fun setSortingItem(
        teiModel: SearchTeiModel,
        sortingItem: SortingItem?
    ): Pair<String, String>? {
        val sortingKeyValue: Pair<String, String>?
        return if (sortingItem != null) {
            sortingKeyValue = when (sortingItem.filterSelectedForSorting) {
                Filters.PERIOD -> getTeiSortedEvent(teiModel, sortingItem.sortingStatus)
                Filters.ORG_UNIT -> getTeiSortedOrgUnit(teiModel)
                Filters.ENROLLMENT_DATE -> getTeiSortedEnrollmentDate(teiModel)
                Filters.ENROLLMENT_STATUS -> getTeiSortedStatus(teiModel)
                else -> Pair(unknownLabel, unknownLabel)
            }
            sortingKeyValue ?: Pair(unknownLabel, unknownLabel)
        } else {
            null
        }
    }

    private fun getTeiSortedEvent(
        teiModel: SearchTeiModel,
        sortingStatus: SortingStatus
    ): Pair<String, String>? {
        var eventDate = unknownLabel
        val sortedEvents = d2.eventModule().events()
            .byEnrollmentUid().eq(teiModel.selectedEnrollment?.uid() ?: "")
            .byDeleted().isFalse
            .orderByTimeline(
                if (sortingStatus === SortingStatus.ASC) {
                    RepositoryScope.OrderByDirection.ASC
                } else {
                    RepositoryScope.OrderByDirection.DESC
                }
            )
            .blockingGet()
        if (sortedEvents != null && sortedEvents.isNotEmpty()) {
            val sortedEvent = sortedEvents.first()
            val sortedDate: Date? =
                if (sortedEvent.status() == EventStatus.SCHEDULE ||
                    sortedEvent.status() == EventStatus.SKIPPED ||
                    sortedEvent.status() == EventStatus.OVERDUE
                ) {
                    sortedEvent.dueDate()
                } else {
                    sortedEvent.eventDate()
                }
            eventDate = SimpleDateFormat(uiDateFormat, Locale.getDefault())
                .format(sortedDate)
        }
        return Pair(eventDateLabel, eventDate)
    }

    private fun getTeiSortedOrgUnit(teiModel: SearchTeiModel): Pair<String, String>? {
        val orgUnitValue: String? = if (teiModel.selectedEnrollment != null) {
            d2.organisationUnitModule().organisationUnits()
                .uid(teiModel.selectedEnrollment.organisationUnit())
                .blockingGet().displayName()
        } else {
            d2.organisationUnitModule().organisationUnits()
                .uid(teiModel.tei.organisationUnit())
                .blockingGet().displayName()
        }
        return Pair(orgUnitLabel, orgUnitValue ?: unknownLabel)
    }

    private fun getTeiSortedStatus(teiModel: SearchTeiModel): Pair<String, String>? {
        var enrollmentStatusValue = unknownLabel
        if (teiModel.selectedEnrollment != null) {
            enrollmentStatusValue =
                enrollmentUiDataHelper.getEnrollmentStatusClientName(
                    teiModel.selectedEnrollment.status()!!
                )
        }
        return Pair(enrollmentStatusLabel, enrollmentStatusValue)
    }

    private fun getTeiSortedEnrollmentDate(teiModel: SearchTeiModel): Pair<String, String>? {
        return teiModel.selectedEnrollment?.let {
            val enrollmentDateLabel = d2.programModule().programs()
                .uid(it.program())
                .blockingGet().enrollmentDateLabel() ?: enrollmentDateDefaultLabel
            val enrollmentDateValue = SimpleDateFormat(uiDateFormat, Locale.getDefault())
                .format(teiModel.selectedEnrollment.enrollmentDate())
            Pair(enrollmentDateLabel, enrollmentDateValue)
        } ?: Pair(enrollmentDateDefaultLabel, unknownLabel)
    }
}
