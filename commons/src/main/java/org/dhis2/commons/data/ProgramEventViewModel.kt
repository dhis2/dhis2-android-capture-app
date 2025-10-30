package org.dhis2.commons.data

import org.hisp.dhis.android.core.common.Geometry
import org.hisp.dhis.android.core.common.State
import org.hisp.dhis.android.core.event.EventStatus
import java.util.Date

data class ProgramEventViewModel(
    val uid: String,
    val orgUnitUid: String,
    val orgUnitName: String,
    val date: Date,
    val eventState: State,
    val eventDisplayData: List<Pair<String?, String?>?>,
    val eventStatus: EventStatus,
    val isExpired: Boolean,
    val attributeOptionComboName: String,
    val geometry: Geometry?,
    val canBeEdited: Boolean,
) : CarouselItemModel {
    override fun uid() = uid
}
