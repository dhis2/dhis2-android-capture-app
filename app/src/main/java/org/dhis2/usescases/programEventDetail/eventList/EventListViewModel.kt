package org.dhis2.usescases.programEventDetail.eventList

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.map
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.withContext
import org.dhis2.commons.filters.FilterManager
import org.dhis2.commons.viewmodel.DispatcherProvider
import org.dhis2.mobile.commons.providers.CustomLabelContext
import org.dhis2.mobile.commons.providers.CustomLabelProvider
import org.dhis2.usescases.programEventDetail.ProgramEventDetailRepository
import org.dhis2.usescases.programEventDetail.ProgramEventMapper
import org.dhis2.usescases.programEventDetail.eventList.ui.mapper.EventCardMapper

class EventListViewModel(
    val filterManager: FilterManager,
    val eventRepository: ProgramEventDetailRepository,
    val dispatchers: DispatcherProvider,
    val mapper: ProgramEventMapper,
    val cardMapper: EventCardMapper,
    private val customLabelProvider: CustomLabelProvider,
) : ViewModel() {
    var onSyncClickedListener: (eventUid: String?) -> Unit = { _ -> }

    var onCardClickedListener: (eventUid: String, orgUnitUid: String) -> Unit = { _, _ -> }

    @OptIn(ExperimentalCoroutinesApi::class)
    private var _eventListState = MutableStateFlow<EventListState?>(null)
    val eventListState =
        _eventListState
            .onStart {
                _eventListState.value =
                    EventListState(
                        eventList = getPagingDataFlow(),
                        customEventLabel =
                            customLabelProvider.getCustomEventLabel(
                                customLabelContext =
                                    CustomLabelContext.ProgramStage(
                                        programStageUid =
                                            eventRepository
                                                .programStage()
                                                .blockingGet()
                                                .uid,
                                        programUid = eventRepository.program().blockingGet().uid,
                                    ),
                                quantity = 2,
                            ),
                    )
            }.stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                null,
            )

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun getPagingDataFlow() =
        filterManager
            .asFlow(viewModelScope)
            .flatMapLatest {
                EventListIdlingResourceSingleton.increment()
                eventRepository
                    .filteredProgramEvents()
                    .map { pagingData ->
                        pagingData.map { event ->
                            withContext(dispatchers.io()) {
                                val displayOrgUnitName =
                                    event.program()?.let { program ->
                                        eventRepository.displayOrganisationUnit(program)
                                    } ?: true

                                val eventModel = mapper.eventToEventViewModel(event)
                                cardMapper.map(
                                    event = eventModel,
                                    editable = eventRepository.isEventEditable(event.uid()),
                                    displayOrgUnit = displayOrgUnitName,
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
            .onEach {
                EventListIdlingResourceSingleton.decrement()
            }.catch {
                EventListIdlingResourceSingleton.decrement()
            }.onStart {
                filterManager.publishData()
            }

    fun refreshData() {
        filterManager.publishData()
    }
}
