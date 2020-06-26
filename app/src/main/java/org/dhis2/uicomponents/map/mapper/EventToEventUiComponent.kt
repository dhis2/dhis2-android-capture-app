package org.dhis2.uicomponents.map.mapper

import org.dhis2.uicomponents.map.model.EventUiComponentModel
import org.dhis2.uicomponents.map.model.StageStyle
import org.dhis2.usescases.searchTrackEntity.adapters.SearchTeiModel
import org.dhis2.usescases.teiDashboard.dashboardfragments.teidata.teievents.EventViewModel

class EventToEventUiComponent {

    fun mapList(events: List<EventViewModel>, teis: List<SearchTeiModel>): List<EventUiComponentModel> {
        return events
            .filter { it.event?.geometry() != null }
            .map { event ->
                map(
                    event,
                    teis.first {
                        it.enrollments.any { enrollment ->
                            enrollment.uid() == event.event?.enrollment()
                        }
                    }
                )
            }
    }

    private fun map(eventViewModel: EventViewModel, tei: SearchTeiModel): EventUiComponentModel {
        val eventUid = eventViewModel.event!!.uid()
        val lastUpdated = eventViewModel.lastUpdate

        return EventUiComponentModel(
            eventUid,
            eventViewModel.event,
            tei.enrollments.first { it.uid() == eventViewModel.event.enrollment() },
            eventViewModel.stage,
            lastUpdated,
            tei.attributeValues,
            tei.profilePicturePath,
            tei.defaultTypeIcon
        )
    }
}
