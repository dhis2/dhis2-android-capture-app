package org.dhis2.metadata.usecases

import org.hisp.dhis.android.core.D2

class TrackedEntityTypeConfiguration(private val d2: D2) {

    fun getTrackedEntityType(teTypeUid: String) =
        d2.trackedEntityModule().trackedEntityTypes().uid(teTypeUid).blockingGet()

    fun getTrackedEntityTypeStyle(teTypeUid: String) = getTrackedEntityType(teTypeUid)?.style()

    fun getTrackedEntityTypeColor(teTypeUid: String) = getTrackedEntityTypeStyle(teTypeUid)?.color()

    fun getTrackedEntityTypeIcon(teTypeUid: String) = getTrackedEntityTypeStyle(teTypeUid)?.icon()
}
