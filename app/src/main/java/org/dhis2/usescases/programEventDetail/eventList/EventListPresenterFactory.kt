package org.dhis2.usescases.programEventDetail.eventList

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import org.dhis2.commons.filters.FilterManager
import org.dhis2.commons.viewmodel.DispatcherProvider
import org.dhis2.usescases.programEventDetail.ProgramEventDetailRepository
import org.dhis2.usescases.programEventDetail.ProgramEventMapper
import org.dhis2.usescases.programEventDetail.eventList.ui.mapper.EventCardMapper

class EventListPresenterFactory(
    private val filterManager: FilterManager,
    private val programEventDetailRepository: ProgramEventDetailRepository,
    private val dispatchers: DispatcherProvider,
    private val mapper: ProgramEventMapper,
    private val cardMapper: EventCardMapper,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        EventListViewModel(
            filterManager,
            programEventDetailRepository,
            dispatchers,
            mapper,
            cardMapper,
        ) as T
}
