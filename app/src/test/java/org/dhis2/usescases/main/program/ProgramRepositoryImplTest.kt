package org.dhis2.usescases.main.program

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.android.plugins.RxAndroidPlugins
import io.reactivex.schedulers.Schedulers
import org.dhis2.commons.filters.FilterManager
import org.dhis2.commons.filters.data.FilterPresenter
import org.dhis2.commons.resources.MetadataIconProvider
import org.dhis2.commons.resources.ResourceManager
import org.dhis2.data.dhislogic.DhisProgramUtils
import org.dhis2.data.dhislogic.DhisTrackedEntityInstanceUtils
import org.dhis2.data.schedulers.TrampolineSchedulerProvider
import org.dhis2.data.service.SyncStatusData
import org.dhis2.ui.MetadataIconData
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.common.Access
import org.hisp.dhis.android.core.common.DataAccess
import org.hisp.dhis.android.core.common.ObjectStyle
import org.hisp.dhis.android.core.common.State
import org.hisp.dhis.android.core.dataset.DataSet
import org.hisp.dhis.android.core.dataset.DataSetInstanceSummary
import org.hisp.dhis.android.core.event.Event
import org.hisp.dhis.android.core.program.Program
import org.hisp.dhis.android.core.program.ProgramType
import org.hisp.dhis.android.core.trackedentity.TrackedEntityType
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.doReturnConsecutively
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class ProgramRepositoryImplTest {
    @Rule
    @JvmField
    var instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var programRepository: ProgramRepository
    private val d2: D2 = Mockito.mock(D2::class.java, Mockito.RETURNS_DEEP_STUBS)
    private val filterPresenter: FilterPresenter =
        Mockito.mock(FilterPresenter::class.java, Mockito.RETURNS_DEEP_STUBS)
    private val dhisProgramUtils: DhisProgramUtils = mock()
    private val dhisTeiUtils: DhisTrackedEntityInstanceUtils = mock()
    private val scheduler = TrampolineSchedulerProvider()
    private val resourceManager: ResourceManager = mock()
    private val metadataIconProvider: MetadataIconProvider = mock {
        on { invoke(any(), any<String>(), any()) } doReturn MetadataIconData.defaultIcon()
    }

    @Before
    fun setUp() {
        RxAndroidPlugins.setInitMainThreadSchedulerHandler { Schedulers.trampoline() }

        programRepository = ProgramRepositoryImpl(
            d2,
            filterPresenter,
            dhisProgramUtils,
            dhisTeiUtils,
            resourceManager,
            metadataIconProvider,
            scheduler,
        )
        whenever(
            resourceManager.defaultDataSetLabel(),
        ) doReturn "dataset"
        whenever(
            resourceManager.defaultEventLabel(),
        ) doReturn "event"
        whenever(
            resourceManager.defaultTeiLabel(),
        ) doReturn "tei"
        whenever(
            d2.dataSetModule().dataSets().uid(anyString()).blockingGet(),
        ) doReturn DataSet.builder()
            .uid("dataSetUid")
            .description("description")
            .style(
                ObjectStyle.builder()
                    .color("color")
                    .icon("icon")
                    .build(),
            )
            .access(
                Access.create(
                    true,
                    true,
                    DataAccess.create(
                        true,
                        true,
                    ),
                ),
            )
            .build()
    }

    @After
    fun clear() {
        FilterManager.getInstance().clearAllFilters()
        RxAndroidPlugins.reset()
    }

    @Test
    fun `Should return list of data set ProgramViewModel`() {
        val syncStatusData = SyncStatusData(true)
        whenever(
            filterPresenter.filteredDataSetInstances(),
        ) doReturn mock()
        whenever(
            filterPresenter.filteredDataSetInstances().get(),
        ) doReturn Single.just(mockedDataSetInstanceSummaries())
        whenever(
            filterPresenter.isAssignedToMeApplied(),
        ) doReturn false

        val testObserver = programRepository.aggregatesModels(syncStatusData).test()

        testObserver
            .assertNoErrors()
            .assertValue {
                it.size == 2
            }
    }

    @Test
    fun `Should set data set count to 0 if assign to me is active`() {
        val syncStatusData = SyncStatusData(true)
        whenever(
            filterPresenter.filteredDataSetInstances(),
        ) doReturn mock()
        whenever(
            filterPresenter.filteredDataSetInstances().get(),
        ) doReturn Single.just(mockedDataSetInstanceSummaries())
        whenever(
            filterPresenter.isAssignedToMeApplied(),
        ) doReturn true
        whenever(
            filterPresenter.areFiltersActive(),
        ) doReturn true

        val testObserver = programRepository.aggregatesModels(syncStatusData).test()

        testObserver
            .assertNoErrors()
            .assertValue {
                it.size == 2 &&
                    it[0].count == 0 &&
                    it[0].translucent() &&
                    it[1].count == 0 &&
                    it[1].translucent()
            }
    }

    @Test
    fun `Should return list of program ProgramViewModels`() {
        val syncStatusData = SyncStatusData(true)
        initWheneverForPrograms()
        whenever(
            filterPresenter.areFiltersActive(),
        ) doReturn false
        val testOvserver = programRepository.programModels(syncStatusData).test()

        testOvserver
            .assertNoErrors()
            .assertValue {
                it.size == mockedPrograms().size &&
                    it[0].count == 10 &&
                    it[0].typeName == "event" &&
                    it[1].count == 2 &&
                    it[1].hasOverdueEvent &&
                    it[1].typeName == "tei"
            }
    }

    private fun initWheneverForPrograms() {
        whenever(
            dhisProgramUtils.getProgramsInCaptureOrgUnits(),
        ) doReturn Flowable.just(
            mockedPrograms(),
        )
        whenever(
            dhisProgramUtils.getProgramRecordLabel(any(), any(), any()),
        ) doReturnConsecutively arrayListOf("event", "tei")
        whenever(
            dhisProgramUtils.getProgramState(any<Program>()),
        ) doReturnConsecutively arrayListOf(State.SYNCED, State.TO_POST)
        whenever(
            d2.programModule().programs().uid(any()).blockingGet(),
        )doReturnConsecutively mockedPrograms()
        whenever(
            filterPresenter.filteredEventProgram(any()),
        ) doReturn mock()
        whenever(
            filterPresenter.filteredEventProgram(any()).blockingGet(),
        ) doReturn listOf(
            Event.builder().uid("0").syncState(State.SYNCED).build(),
            Event.builder().uid("1").syncState(State.SYNCED).build(),
            Event.builder().uid("2").syncState(State.SYNCED).build(),
            Event.builder().uid("3").syncState(State.SYNCED).build(),
            Event.builder().uid("4").syncState(State.SYNCED).build(),
            Event.builder().uid("5").syncState(State.SYNCED).build(),
            Event.builder().uid("6").syncState(State.SYNCED).build(),
            Event.builder().uid("7").syncState(State.SYNCED).build(),
            Event.builder().uid("8").syncState(State.SYNCED).build(),
            Event.builder().uid("9").syncState(State.SYNCED).build(),
            Event.builder().uid("10").syncState(State.RELATIONSHIP).build(),
        )
        whenever(
            filterPresenter.filteredTrackerProgram(any()),
        ) doReturn mock()
        whenever(
            filterPresenter.filteredTrackerProgram(any()).offlineFirst(),
        ) doReturn mock()
        whenever(
            filterPresenter.filteredTrackerProgram(any<Program>()).offlineFirst().blockingGetUids(),
        ) doReturn arrayListOf("teiUid1", "teiUid2")

        whenever(
            dhisTeiUtils.hasOverdueInProgram(any(), any()),
        ) doReturn true
    }

    private fun mockedDataSetInstanceSummaries(): List<DataSetInstanceSummary> {
        return listOf(
            DataSetInstanceSummary.builder()
                .dataSetUid("dataSetUid_1")
                .dataSetDisplayName("dataSetUid_1")
                .valueCount(5)
                .dataSetInstanceCount(2)
                .state(State.SYNCED)
                .build(),
            DataSetInstanceSummary.builder()
                .dataSetUid("dataSetUid_1")
                .dataSetDisplayName("dataSetUid_1")
                .dataSetInstanceCount(1)
                .valueCount(5)
                .state(State.TO_UPDATE)
                .build(),
        )
    }

    private fun mockedPrograms(): List<Program> {
        return arrayListOf(
            Program.builder()
                .uid("program1")
                .displayName("program1")
                .programType(ProgramType.WITHOUT_REGISTRATION)
                .style(ObjectStyle.builder().build())
                .build(),
            Program.builder()
                .uid("program2")
                .displayName("program2")
                .programType(ProgramType.WITH_REGISTRATION)
                .style(ObjectStyle.builder().build())
                .trackedEntityType(
                    TrackedEntityType.builder()
                        .uid("teType")
                        .displayName("Person")
                        .build(),
                )
                .build(),
        )
    }
}
