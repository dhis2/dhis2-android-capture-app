package org.dhis2.usescases.programEventDetail.eventList

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.paging.PagingData
import io.reactivex.Flowable
import io.reactivex.processors.FlowableProcessor
import io.reactivex.processors.PublishProcessor
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.dhis2.commons.filters.FilterManager
import org.dhis2.commons.viewmodel.DispatcherProvider
import org.dhis2.usescases.programEventDetail.ProgramEventDetailRepository
import org.dhis2.usescases.programEventDetail.ProgramEventMapper
import org.dhis2.usescases.programEventDetail.eventList.ui.mapper.EventCardMapper
import org.hisp.dhis.android.core.event.Event
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.util.Date

class EventListViewModelTest {
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var viewModel: EventListViewModel
    private val repository: ProgramEventDetailRepository = mock()

    private val mapper: ProgramEventMapper = mock()
    private val cardMapper: EventCardMapper = mock()
    private val filterManager: FilterManager = mock()

    @OptIn(ExperimentalCoroutinesApi::class)
    private val testingDispatcher = UnconfinedTestDispatcher()
    private val dispatcherProvider: DispatcherProvider =
        mock {
            on { io() } doReturn testingDispatcher
            on { computation() } doReturn testingDispatcher
            on { ui() } doReturn testingDispatcher
        }

    private val filterProcessor: FlowableProcessor<FilterManager> = PublishProcessor.create()
    private val filterManagerFlowable =
        flow<Int> { Flowable.just(filterManager).startWith(filterProcessor) }

    @Test
    fun `Display Org unit should be false if configured to not show`() =
        runTest {
            whenever(repository.filteredProgramEvents()) doReturn mockedList()
            whenever(filterManager.asFlow(any())) doReturn filterManagerFlowable
            whenever(repository.displayOrganisationUnit("programuid")) doReturn false
            whenever(mapper.eventToEventViewModel(any())) doReturn mock()
            viewModel =
                EventListViewModel(
                    filterManager,
                    repository,
                    dispatcherProvider,
                    mapper,
                    cardMapper,
                )
            launch {
                viewModel.eventList.collect {
                    assert(viewModel.displayOrgUnitName.value == false)
                }
            }
        }

    private fun mockedList(): Flow<PagingData<Event>> =
        flow {
            Event
                .builder()
                .uid("uid")
                .program("programuid")
                .eventDate(Date())
                .build()
        }
}
