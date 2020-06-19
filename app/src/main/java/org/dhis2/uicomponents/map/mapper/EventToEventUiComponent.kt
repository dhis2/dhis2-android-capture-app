package org.dhis2.uicomponents.map.mapper

import org.dhis2.uicomponents.map.model.EventUiComponentModel
import org.dhis2.usescases.teiDashboard.dashboardfragments.teidata.teievents.EventViewModel

class EventToEventUiComponent {

    fun mapList(events: List<EventViewModel>): List<EventUiComponentModel> {
        return events
            .filter { it.event?.geometry() != null }
            .map { map(it) }
    }

    private fun map(eventViewModel: EventViewModel): EventUiComponentModel {
        val stageUid = eventViewModel.stage!!.uid()
        val stageDisplayName = eventViewModel.stage.displayName()!!
        val eventUid = eventViewModel.event!!.uid()
        val lastUpdated = eventViewModel.lastUpdate

        return EventUiComponentModel(
            stageUid,
            stageDisplayName,
            eventUid,
            eventViewModel.event,
            lastUpdated
        )
    }
}
