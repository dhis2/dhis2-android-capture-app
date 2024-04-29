package org.dhis2.commons.filters.data

import org.dhis2.commons.filters.FilterResources
import org.dhis2.commons.filters.Filters
import org.hisp.dhis.android.core.common.AssignedUserMode
import org.hisp.dhis.android.core.event.search.EventQueryRepositoryScope
import org.hisp.dhis.android.core.trackedentity.search.TrackedEntityInstanceQueryRepositoryScope

sealed class WorkingListScope {
    abstract fun isAssignedActive(): Boolean
    abstract fun isAssignedToMeActive(): Boolean
    abstract fun isEnrollmentStatusActive(): Boolean
    abstract fun isPeriodActive(filterType: Filters): Boolean
    abstract fun isEventStatusActive(): Boolean

    abstract fun eventStatusCount(): Int
    abstract fun eventDateCount(): Int
    abstract fun enrollmentDateCount(): Int
    abstract fun enrollmentStatusCount(): Int
    abstract fun assignCount(): Int

    abstract fun value(filterType: Filters): String
}

data class EmptyWorkingList(val defaultMessage: String? = null) : WorkingListScope() {
    override fun isAssignedActive(): Boolean = false
    override fun isAssignedToMeActive(): Boolean = false
    override fun isEnrollmentStatusActive(): Boolean = false
    override fun isPeriodActive(filterType: Filters): Boolean = false
    override fun isEventStatusActive(): Boolean = false
    override fun eventStatusCount(): Int = 0
    override fun eventDateCount(): Int = 0
    override fun enrollmentDateCount(): Int = 0
    override fun enrollmentStatusCount(): Int = 0
    override fun assignCount(): Int = 0
    override fun value(filterType: Filters) = ""
}

data class TeiWorkingListScope(
    val enrollmentStatusList: List<String>?,
    val enrollmentDate: String?,
    val eventStatusList: List<String>?,
    val eventDateList: List<String>?,
    val assignedToMe: List<AssignedUserMode>?,
) : WorkingListScope() {
    override fun isAssignedActive(): Boolean = assignedToMe?.isNotEmpty() == true
    override fun isAssignedToMeActive(): Boolean =
        assignedToMe?.isNotEmpty() == true && assignedToMe.any { it == AssignedUserMode.CURRENT }

    override fun isEnrollmentStatusActive(): Boolean = enrollmentStatusList?.isNotEmpty() == true
    override fun isPeriodActive(filterType: Filters): Boolean = when (filterType) {
        Filters.PERIOD -> eventDateList?.isNotEmpty() == true
        Filters.ENROLLMENT_DATE -> enrollmentDate != null
        else -> false
    }

    override fun isEventStatusActive(): Boolean = eventStatusList?.isNotEmpty() == true

    override fun eventStatusCount(): Int = eventStatusList?.size ?: 0
    override fun eventDateCount(): Int = eventDateList?.size ?: 0
    override fun enrollmentDateCount(): Int = if (enrollmentDate != null) 1 else 0
    override fun enrollmentStatusCount(): Int = enrollmentStatusList?.size ?: 0
    override fun assignCount(): Int = assignedToMe?.size ?: 0

    override fun value(filterType: Filters) = when (filterType) {
        Filters.PERIOD -> eventDateList?.joinToString() ?: ""
        Filters.EVENT_STATUS -> eventStatusList?.joinToString() ?: ""
        Filters.ENROLLMENT_DATE -> enrollmentDate ?: ""
        Filters.ENROLLMENT_STATUS -> enrollmentStatusList?.joinToString() ?: ""
        else -> ""
    }
}

data class EventWorkingListScope(
    val stageUid: String?,
    val eventDate: String?,
    val eventStatusList: List<String>?,
    val assignedToMe: AssignedUserMode?,
) : WorkingListScope() {
    override fun isAssignedActive(): Boolean = assignedToMe != null
    override fun isAssignedToMeActive(): Boolean = assignedToMe == AssignedUserMode.CURRENT
    override fun isEnrollmentStatusActive(): Boolean = false
    override fun isPeriodActive(filterType: Filters): Boolean = eventDate != null
    override fun isEventStatusActive(): Boolean = eventStatusList?.isNotEmpty() == true

    override fun eventStatusCount(): Int = eventStatusList?.size ?: 0
    override fun eventDateCount(): Int = if (eventDate != null) 1 else 0
    override fun enrollmentDateCount(): Int = 0
    override fun enrollmentStatusCount(): Int = 0
    override fun assignCount(): Int = if (assignedToMe != null) 1 else 0

    override fun value(filterType: Filters) = when (filterType) {
        Filters.PERIOD -> eventDate ?: ""
        Filters.EVENT_STATUS -> eventStatusList?.joinToString() ?: ""
        else -> ""
    }
}

fun TrackedEntityInstanceQueryRepositoryScope.mapToWorkingListScope(
    resources: FilterResources,
): TeiWorkingListScope {
    return TeiWorkingListScope(
        enrollmentStatus()?.let { resources.enrollmentStatusToText(it) },
        programDate()?.let { resources.dateFilterPeriodToText(it) },
        resources.eventStatusToText(
            eventFilters().mapNotNull { it.eventStatus() }
                .flatten().distinct(),
        ),
        eventFilters().mapNotNull { it.eventDate() }
            .mapNotNull { resources.dateFilterPeriodToText(it) },
        eventFilters().mapNotNull { it.assignedUserMode() }.distinct(),
    )
}

fun EventQueryRepositoryScope.mapToEventWorkingListScope(
    resources: FilterResources,
): EventWorkingListScope {
    return EventWorkingListScope(
        programStage(),
        eventDate()?.let { resources.dateFilterPeriodToText(it) },
        eventStatus()?.let { resources.eventStatusToText(it) },
        assignedUserMode(),
    )
}
