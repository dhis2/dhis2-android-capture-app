package org.dhis2.usescases.programEventDetail.eventList

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.map
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import org.dhis2.commons.filters.FilterManager
import org.dhis2.commons.ui.model.ListCardUiModel
import org.dhis2.commons.viewmodel.DispatcherProvider
import org.dhis2.usescases.programEventDetail.ProgramEventDetailRepository
import org.dhis2.usescases.programEventDetail.ProgramEventMapper
import org.dhis2.usescases.programEventDetail.eventList.ui.mapper.EventCardMapper

class EventListViewModel(
    val filterManager: FilterManager,
    val eventRepository: ProgramEventDetailRepository,
    val dispatchers: DispatcherProvider,
    val mapper: ProgramEventMapper,
    val cardMapper: EventCardMapper,
) : ViewModel() {

    var onSyncClickedListener: (eventUid: String?) -> Unit = { _ -> }

    var onCardClickedListener: (eventUid: String, orgUnitUid: String) -> Unit = { _, _ -> }

    private val _displayOrgUnitName = MutableLiveData(true)
    val displayOrgUnitName = _displayOrgUnitName

    @OptIn(ExperimentalCoroutinesApi::class)
    private var _eventList: Flow<PagingData<ListCardUiModel>> =
        filterManager.asFlow(viewModelScope)
            .flatMapLatest {
                EventListIdlingResourceSingleton.increment()
                eventRepository.filteredProgramEvents()
                    .map { pagingData ->
                        pagingData.map { event ->
                            withContext(dispatchers.io()) {
                                _displayOrgUnitName.postValue(
                                    event.program()?.let { program ->
                                        eventRepository.displayOrganisationUnit(program)
                                    } ?: true,
                                )

                                val eventModel = mapper.eventToEventViewModel(event)
                                cardMapper.map(
                                    event = eventModel,
                                    editable = eventRepository.isEventEditable(event.uid()),
                                    displayOrgUnit = displayOrgUnitName.value ?: true,
                                    onSyncIconClick = {
                                        onSyncClickedListener(
                                            eventModel.event?.uid(),
                                        )
                                    },
                                    onCardClick = {
                                        eventModel.event?.let { event ->
                                            onCardClickedListener(
                                                event.uid(),
                                                event.organisationUnit() ?: "",
                                            )
                                        }
                                    },
                                )
                            }
                        }
                    }.flowOn(dispatchers.io())
            }.flowOn(dispatchers.io())

    val eventList = _eventList

    fun refreshData() {
        filterManager.publishData()
    }
}
