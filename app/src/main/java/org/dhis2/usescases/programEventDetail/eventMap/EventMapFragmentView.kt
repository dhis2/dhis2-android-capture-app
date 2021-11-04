package org.dhis2.usescases.programEventDetail.eventMap

import org.dhis2.usescases.programEventDetail.ProgramEventMapData
import org.dhis2.commons.data.ProgramEventViewModel

interface EventMapFragmentView {
    fun setMap(mapData: ProgramEventMapData)
    fun updateEventCarouselItem(programEventViewModel: ProgramEventViewModel)
}
