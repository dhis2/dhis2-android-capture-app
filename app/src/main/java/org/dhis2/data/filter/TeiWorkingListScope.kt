package org.dhis2.data.filter

import org.dhis2.utils.resources.ResourceManager
import org.hisp.dhis.android.core.common.AssignedUserMode
import org.hisp.dhis.android.core.trackedentity.search.TrackedEntityInstanceQueryRepositoryScope

data class TeiWorkingListScope(
    val enrollmentStatusList: List<String>?,
    val enrollmentDate: String?,
    val eventStatusList: List<String>?,
    val eventDateList: List<String>?,
    val assignedToMe: List<AssignedUserMode>?
){
    fun isAssignedToMeActive():Boolean = assignedToMe?.isNotEmpty() == true
}

fun TrackedEntityInstanceQueryRepositoryScope.mapToWorkingListScope(resources: ResourceManager): TeiWorkingListScope {
    return TeiWorkingListScope(
        enrollmentStatus()?.let { resources.filterResources.enrollmentStatusToText(it) },
        programDate()?.let { resources.filterResources.dateFilterPeriodToText(it) },
        resources.filterResources.eventStatusToText(eventFilters().mapNotNull { it.eventStatus() }
            .flatten().distinct()),
        eventFilters().mapNotNull { it.eventDate() }
            .mapNotNull { resources.filterResources.dateFilterPeriodToText(it) },
        eventFilters().mapNotNull { it.assignedUserMode() }.distinct()
    )
}