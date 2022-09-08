package org.dhis2.usescases.searchTrackEntity

import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Observable
import io.reactivex.schedulers.TestScheduler
import org.dhis2.commons.filters.DisableHomeFiltersFromSettingsApp
import org.dhis2.commons.filters.FilterItem
import org.dhis2.commons.filters.data.FilterRepository
import org.dhis2.commons.filters.workingLists.TeiFilterToWorkingListItemMapper
import org.dhis2.commons.matomo.MatomoAnalyticsController
import org.dhis2.commons.prefs.PreferenceProvider
import org.dhis2.data.schedulers.TestSchedulerProvider
import org.dhis2.data.service.SyncStatusController
import org.dhis2.utils.analytics.AnalyticsHelper
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.program.Program
import org.hisp.dhis.android.core.trackedentity.TrackedEntityType
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import org.mockito.Mockito.validateMockitoUsage

class SearchTEPresenterTest {

    lateinit var presenter: SearchTEContractsModule.Presenter

    private val view: SearchTEContractsModule.View = mock()
    private val d2: D2 = Mockito.mock(D2::class.java, Mockito.RETURNS_DEEP_STUBS)
    private val repository: SearchRepository = mock()
    private val schedulers: TestSchedulerProvider = TestSchedulerProvider(TestScheduler())
    private val analyticsHelper: AnalyticsHelper = mock()
    private val initialProgram = "programUid"
    private val teType = "teTypeUid"
    private val preferenceProvider: PreferenceProvider = mock()
    private val workingListMapper: TeiFilterToWorkingListItemMapper = mock()
    private val filterRepository: FilterRepository = mock()
    private val disableHomeFiltersFromSettingsApp: DisableHomeFiltersFromSettingsApp = mock()
    private val matomoAnalyticsController: MatomoAnalyticsController = mock()
    private val syncStatusController: SyncStatusController = mock()

    @Before
    fun setUp() {
        whenever(
            d2.programModule().programs().uid(initialProgram).blockingGet()
        ) doReturn
            Program.builder().uid(initialProgram)
                .displayFrontPageList(true)
                .minAttributesRequiredToSearch(0).build()

        whenever(
            repository.getTrackedEntityType(teType)
        )doReturn Observable.just(
            TrackedEntityType.builder()
                .uid(teType)
                .displayName("teTypeName")
                .build()
        )

        presenter = SearchTEPresenter(
            view,
            d2,
            repository,
            schedulers,
            analyticsHelper,
            initialProgram,
            teType,
            preferenceProvider,
            workingListMapper,
            filterRepository,
            disableHomeFiltersFromSettingsApp,
            matomoAnalyticsController,
            syncStatusController
        )
    }

    @Test
    fun `Should ignore initial program spinner selection`() {
        val program = Program.builder()
            .uid("uid")
            .displayFrontPageList(true)
            .minAttributesRequiredToSearch(1)
            .build()

        presenter.setProgramForTesting(program)

        presenter.program = program

        verify(view, never()).clearList(program.uid())
    }

    @Test
    fun `Should clear data, fab and list when another program is selected`() {
        val program = Program.builder()
            .uid("uid")
            .displayFrontPageList(true)
            .minAttributesRequiredToSearch(1)
            .build()

        val newSelectedProgram = Program.builder()
            .uid("uid2")
            .displayFrontPageList(true)
            .minAttributesRequiredToSearch(1)
            .build()

        whenever(
            d2.programModule().programStages()
                .byProgramUid().eq(newSelectedProgram.uid())
        ) doReturn mock()

        whenever(
            d2.programModule().programStages()
                .byProgramUid().eq(newSelectedProgram.uid())
                .byEnableUserAssignment()
        ) doReturn mock()

        whenever(
            d2.programModule().programStages()
                .byProgramUid().eq(newSelectedProgram.uid())
                .byEnableUserAssignment().isTrue
        ) doReturn mock()

        whenever(
            d2.programModule().programStages()
                .byProgramUid().eq(newSelectedProgram.uid())
                .byEnableUserAssignment().isTrue
                .blockingIsEmpty()
        ) doReturn false

        presenter.setProgramForTesting(program)
        presenter.program = newSelectedProgram

        verify(view).clearList(newSelectedProgram.uid())
    }

    @Test
    fun `Should clear other filters if webapp is config`() {
        val list = listOf<FilterItem>()
        whenever(filterRepository.homeFilters()) doReturn listOf()

        presenter.clearOtherFiltersIfWebAppIsConfig()

        verify(disableHomeFiltersFromSettingsApp).execute(list)
    }

    @After
    fun validate() {
        validateMockitoUsage()
    }
}
