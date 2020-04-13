package org.dhis2.usescases.searchTrackEntity

import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.schedulers.TestScheduler
import org.dhis2.data.schedulers.TestSchedulerProvider
import org.dhis2.utils.analytics.AnalyticsHelper
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.program.Program
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito


class SearchTEPresenterTest {

    lateinit var presenter: SearchTEPresenter

    private val view: SearchTEContractsModule.View = mock()
    private val d2: D2 = Mockito.mock(D2::class.java, Mockito.RETURNS_DEEP_STUBS)
    private val repository: SearchRepository = mock()
    private val schedulers: TestSchedulerProvider = TestSchedulerProvider(TestScheduler())
    private val analyticsHelper: AnalyticsHelper = mock()
    private val initialProgram = "programUid"

    @Before
    fun setUp() {
        whenever(d2.programModule().programs().uid(initialProgram).blockingGet()) doReturn Program.builder().uid(
            initialProgram
        )
            .displayFrontPageList(true)
            .minAttributesRequiredToSearch(0).build()
        presenter =
            SearchTEPresenter(view, d2, repository, schedulers, analyticsHelper, initialProgram)
    }

    @Test
    fun `Should set fabIcon to search when displayFrontPageList is true and minAttributes isgreater than querydata`() {

        presenter.setProgramForTesting(
            Program.builder()
                .uid("uid")
                .displayFrontPageList(true)
                .minAttributesRequiredToSearch(1)
                .build()
        )

        presenter.onFabClick(true)

        verify(view).clearData()
        verify(view).updateFiltersSearch(0)
        verify(view).setFabIcon(true)

    }

    @Test
    fun `Should set fabIcon to add when displayFrontPageList is true and does not hava a minAttributes required`() {
        presenter.setProgramForTesting(
            Program.builder()
                .uid("uid")
                .displayFrontPageList(true)
                .minAttributesRequiredToSearch(0)
                .build()
        )

        presenter.onFabClick(true)

        verify(view).clearData()
        verify(view).updateFiltersSearch(0)
        verify(view).setFabIcon(false)
    }

    @Test
    fun `Should set fabIcon to add if there is no program selected`() {
        presenter.setProgramForTesting(null)
        presenter.onFabClick(true)

        verify(view).clearData()
        verify(view).updateFiltersSearch(0)
        verify(view).setFabIcon(false)
    }
}