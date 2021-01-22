package org.dhis2.usescases.teiDashboard.dashboardfragments.teidata.teievents

import java.util.Date
import org.hisp.dhis.android.core.event.Event
import org.hisp.dhis.android.core.program.ProgramStage

data class EventViewModel(
    val type: EventViewModelType,
    val stage: ProgramStage?,
    val event: Event?,
    val eventCount: Int,
    val lastUpdate: Date?,
    val isSelected: Boolean,
    val canAddNewEvent: Boolean,
    val orgUnitName: String,
    val catComboName: String?,
    val dataElementValues: List<Pair<String, String?>>?,
    val groupedByStage: Boolean? = false,
    var valueListIsOpen: Boolean = false,
    val showTopShadow: Boolean = false,
    val showBottomShadow: Boolean = false
) {
    fun toggleValueList() {
        this.valueListIsOpen = !valueListIsOpen
    }
}
