package org.dhis2.data.sorting

import org.dhis2.commons.data.SearchTeiModel
import org.dhis2.commons.filters.Filters
import org.dhis2.commons.filters.sorting.SortingItem
import org.dhis2.commons.filters.sorting.SortingStatus
import org.dhis2.data.enrollment.EnrollmentUiDataHelper
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.arch.repositories.scope.RepositoryScope
import org.hisp.dhis.android.core.event.EventStatus
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SearchSortingValueSetter(
    private val d2: D2,
    val unknownLabel: String,
    private val eventDateLabel: String,
    private val enrollmentStatusLabel: String,
    private val enrollmentDateDefaultLabel: String,
    private val uiDateFormat: String,
    private val enrollmentUiDataHelper: EnrollmentUiDataHelper,
) {

    fun setSortingItem(teiModel: SearchTeiModel, sortingItem: SortingItem?): Pair<String, String>? {
        val sortingKeyValue: Pair<String, String>?
        return if (
            sortingItem != null && sortingItem.filterSelectedForSorting != Filters.ORG_UNIT
        ) {
            sortingKeyValue = when (sortingItem.filterSelectedForSorting) {
                Filters.PERIOD -> getTeiSortedEvent(teiModel, sortingItem.sortingStatus)
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
        sortingStatus: SortingStatus,
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
                },
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

    private fun getTeiSortedStatus(teiModel: SearchTeiModel): Pair<String, String>? {
        var enrollmentStatusValue = unknownLabel
        if (teiModel.selectedEnrollment != null) {
            enrollmentStatusValue =
                enrollmentUiDataHelper.getEnrollmentStatusClientName(
                    teiModel.selectedEnrollment.status()!!,
                )
        }
        return Pair(enrollmentStatusLabel, enrollmentStatusValue)
    }

    private fun getTeiSortedEnrollmentDate(teiModel: SearchTeiModel): Pair<String, String>? {
        return teiModel.selectedEnrollment?.let {
            val enrollmentDateLabel = d2.programModule().programs()
                .uid(it.program())
                .blockingGet()?.enrollmentDateLabel() ?: enrollmentDateDefaultLabel
            val enrollmentDateValue = SimpleDateFormat(uiDateFormat, Locale.getDefault())
                .format(teiModel.selectedEnrollment.enrollmentDate())
            Pair(enrollmentDateLabel, enrollmentDateValue)
        } ?: Pair(enrollmentDateDefaultLabel, unknownLabel)
    }
}
