package org.dhis2.usescases.programEventDetail.eventList

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.paging.PagingData
import androidx.paging.testing.asSnapshot
import io.reactivex.Single
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.dhis2.commons.filters.FilterManager
import org.dhis2.commons.ui.model.ListCardUiModel
import org.dhis2.commons.viewmodel.DispatcherProvider
import org.dhis2.mobile.commons.providers.CustomLabelProvider
import org.dhis2.usescases.programEventDetail.ProgramEventDetailRepository
import org.dhis2.usescases.programEventDetail.ProgramEventMapper
import org.dhis2.usescases.programEventDetail.eventList.ui.mapper.EventCardMapper
import org.dhis2.utils.MainCoroutineScopeRule
import org.hisp.dhis.android.core.event.Event
import org.hisp.dhis.android.core.program.Program
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.util.Date

@OptIn(ExperimentalCoroutinesApi::class)
class EventListViewModelTest {
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testingDispatcher = UnconfinedTestDispatcher()

    @get:Rule
    val coroutineScopeRule = MainCoroutineScopeRule(testingDispatcher)

    private lateinit var viewModel: EventListViewModel
    private val repository: ProgramEventDetailRepository = mock()
    private val mapper: ProgramEventMapper = mock()
    private val cardMapper: EventCardMapper = mock()
    private val filterManager: FilterManager = mock()

    private val dispatcherProvider: DispatcherProvider =
        mock {
            on { io() } doReturn testingDispatcher
            on { computation() } doReturn testingDispatcher
            on { ui() } doReturn testingDispatcher
        }

    private val event =
        Event
            .builder()
            .uid("uid")
            .program("programuid")
            .eventDate(Date())
            .build()

    private val program: Program = mock { on { uid } doReturn "programuid" }

    private val mappedCard: ListCardUiModel = mock()

    private val customLabelProvider: CustomLabelProvider =
        mock {
            onBlocking { getCustomEventLabel("programuid", 2) } doReturn "Events"
        }

    @Test
    fun `eventListState should expose the mapped paging data and the custom event label`() =
        runTest {
            whenever(filterManager.asFlow(any())) doReturn flowOf(0)
            whenever(repository.filteredProgramEvents()) doReturn
                flowOf(PagingData.from(listOf(event)))
            whenever(repository.program()) doReturn Single.just(program)
            whenever(repository.displayOrganisationUnit("programuid")) doReturn false
            whenever(repository.isEventEditable("uid")) doReturn true
            whenever(mapper.eventToEventViewModel(event)) doReturn mock()
            whenever(cardMapper.map(any(), any(), any(), any(), any())) doReturn mappedCard

            viewModel =
                EventListViewModel(
                    filterManager,
                    repository,
                    dispatcherProvider,
                    mapper,
                    cardMapper,
                    customLabelProvider,
                )

            val state = viewModel.eventListState.filterNotNull().first()

            assertEquals("Events", state.customEventLabel)
            assertEquals(listOf(mappedCard), state.eventList.asSnapshot())
        }
}
