package org.dhis2.usescases.programEventDetail.eventMap

import org.dhis2.commons.data.ProgramEventViewModel

interface EventMapFragmentView {
    fun updateEventCarouselItem(programEventViewModel: ProgramEventViewModel)
}
