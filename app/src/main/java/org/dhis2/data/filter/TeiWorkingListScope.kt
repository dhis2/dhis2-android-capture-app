package org.dhis2.data.filter

import org.dhis2.utils.resources.ResourceManager
import org.hisp.dhis.android.core.common.AssignedUserMode
import org.hisp.dhis.android.core.event.search.EventQueryRepositoryScope
import org.hisp.dhis.android.core.trackedentity.search.TrackedEntityInstanceQueryRepositoryScope

sealed class WorkingListScope() {
    abstract fun isAssignedToMeActive(): Boolean
}

data class TeiWorkingListScope(
    val enrollmentStatusList: List<String>?,
    val enrollmentDate: String?,
    val eventStatusList: List<String>?,
    val eventDateList: List<String>?,
    val assignedToMe: List<AssignedUserMode>?
) : WorkingListScope() {
    override fun isAssignedToMeActive(): Boolean = assignedToMe?.isNotEmpty() == true
}

data class EventWorkingListScope(
    val stageUid: String?,
    val eventDate: String?,
    val eventStatus: String?,
    val assignedToMe: AssignedUserMode?
) : WorkingListScope() {
    override fun isAssignedToMeActive(): Boolean = assignedToMe != null
}

fun TrackedEntityInstanceQueryRepositoryScope.mapToWorkingListScope(
    resources: ResourceManager
): TeiWorkingListScope {
    return TeiWorkingListScope(
        enrollmentStatus()?.let { resources.filterResources.enrollmentStatusToText(it) },
        programDate()?.let { resources.filterResources.dateFilterPeriodToText(it) },
        resources.filterResources.eventStatusToText(
            eventFilters().mapNotNull { it.eventStatus() }
                .flatten().distinct()
        ),
        eventFilters().mapNotNull { it.eventDate() }
            .mapNotNull { resources.filterResources.dateFilterPeriodToText(it) },
        eventFilters().mapNotNull { it.assignedUserMode() }.distinct()
    )
}

fun EventQueryRepositoryScope.mapToEventWorkingListScope(
    resources: ResourceManager
): EventWorkingListScope {
    return EventWorkingListScope(
        programStage(),
        eventDate()?.let { resources.filterResources.dateFilterPeriodToText(it) },
        eventStatus()?.let { resources.filterResources.eventStatusToText(it) },
        assignedUserMode()
    )
}
