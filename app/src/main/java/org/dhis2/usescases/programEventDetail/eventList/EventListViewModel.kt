package org.dhis2.usescases.programEventDetail.eventList

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.map
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
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

    val disposable = CompositeDisposable()
    private var _onSyncClick: MutableStateFlow<String?> = MutableStateFlow(null)
    val onSyncClick: Flow<String?> = _onSyncClick
    private var _onEventCardClick: MutableStateFlow<Pair<String, String>?> = MutableStateFlow(null)
    val onEventCardClick: Flow<Pair<String, String>?> = _onEventCardClick

    private var _eventList: Flow<PagingData<ListCardUiModel>> =
        filterManager.asFlow(viewModelScope)
            .flatMapLatest {
                eventRepository.filteredProgramEvents()
                    .map { pagingData ->
                        pagingData.map { event ->
                            withContext(dispatchers.io()) {
                                val eventModel = mapper.eventToEventViewModel(event)
                                cardMapper.map(
                                    event = eventModel,
                                    editable = eventRepository.isEventEditable(event.uid()),
                                    displayOrgUnit = event.program()?.let { program ->
                                        eventRepository.displayOrganisationUnit(program)
                                    } ?: true,
                                    onSyncIconClick = {
                                        onSyncIconClick(
                                            eventModel.event?.uid(),
                                        )
                                    },
                                    onCardClick = {
                                        eventModel.event?.let { event ->
                                            onEventCardClick(
                                                Pair(
                                                    event.uid(),
                                                    event.organisationUnit() ?: "",
                                                ),
                                            )
                                        }
                                    },
                                )
                            }
                        }
                    }.flowOn(dispatchers.io())
            }.flowOn(dispatchers.io())

    val eventList = _eventList

    private fun onSyncIconClick(eventUid: String?) {
        viewModelScope.launch {
            _onSyncClick.emit(eventUid)
        }
    }

    private fun onEventCardClick(eventUidAndOrgUnit: Pair<String, String>) {
        viewModelScope.launch {
            _onEventCardClick.emit(eventUidAndOrgUnit)
        }
    }

    fun refreshData() {
        filterManager.publishData()
    }
}
