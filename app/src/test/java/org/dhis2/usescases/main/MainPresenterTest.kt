package org.dhis2.usescases.main

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.processors.BehaviorProcessor
import io.reactivex.processors.FlowableProcessor
import org.dhis2.data.prefs.Preference.Companion.DEFAULT_CAT_COMBO
import org.dhis2.data.prefs.Preference.Companion.PREF_DEFAULT_CAT_OPTION_COMBO
import org.dhis2.data.prefs.PreferenceProvider
import org.dhis2.data.schedulers.SchedulerProvider
import org.dhis2.data.schedulers.TrampolineSchedulerProvider
import org.dhis2.data.service.workManager.WorkManagerController
import org.dhis2.usescases.login.LoginActivity
import org.dhis2.utils.filters.FilterManager
import org.dhis2.utils.filters.Filters
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.category.CategoryCombo
import org.hisp.dhis.android.core.category.CategoryOptionCombo
import org.hisp.dhis.android.core.user.User
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Created by frodriguez on 10/22/2019.
 *
 */
class MainPresenterTest {

    private lateinit var presenter: MainPresenter
    private val schedulers: SchedulerProvider = TrampolineSchedulerProvider()
    private val view: MainView = mock()
    private val d2: D2 = mock()
    private val preferences: PreferenceProvider = mock()
    private val workManagerController: WorkManagerController = mock()
    private val filterManager: FilterManager = mock()

    @Before
    fun setUp() {
        presenter =
            MainPresenter(view, d2, schedulers, preferences, workManagerController, filterManager)
    }

    @Test
    fun `Should save default settings and render user name when the activity is resumed`() {
        presenterMocks()

        presenter.init()

        verify(view).renderUsername(any())
        verify(preferences).setValue(DEFAULT_CAT_COMBO, "uid")
        verify(preferences).setValue(PREF_DEFAULT_CAT_OPTION_COMBO, "uid")
    }

    @Test
    fun `Should setup filters when activity is resumed`() {
        val periodRequest: FlowableProcessor<Pair<FilterManager.PeriodRequest, Filters?>> =
            BehaviorProcessor.create()
        whenever(filterManager.asFlowable()) doReturn Flowable.just(filterManager)
        whenever(filterManager.periodRequest) doReturn periodRequest
        periodRequest.onNext(Pair(FilterManager.PeriodRequest.FROM_TO,null))

        presenter.initFilters()

        verify(view).updateFilters(any())
        verify(view).showPeriodRequest(periodRequest.blockingFirst().first)
    }

    @Test
    fun `Should log out`() {
        whenever(d2.userModule()) doReturn mock()
        whenever(d2.userModule().logOut()) doReturn Completable.complete()

        presenter.logOut()

        verify(workManagerController).cancelAllWork()
        verify(view).startActivity(LoginActivity::class.java, null, true, true, null)
    }

    @Test
    fun `Should block session`() {
        presenter.blockSession()

        verify(workManagerController).cancelAllWork()
        verify(view).back()
    }

    @Test
    fun `Should show filter screen when filter icon is clicked`() {
        presenter.showFilter()

        verify(view).showHideFilter()
    }

    @Test
    fun `Should clear disposable when activity is paused`() {
        presenter.onDetach()

        val disposableSize = presenter.disposable.size()

        assertTrue(disposableSize == 0)
    }

    @Test
    fun `Should open drawer when menu is clicked`() {
        presenter.onMenuClick()

        verify(view).openDrawer(any())
    }

    private fun presenterMocks() {
        // UserModule
        whenever(d2.userModule()) doReturn mock()
        whenever(d2.userModule().user()) doReturn mock()
        whenever(d2.userModule().user().get()) doReturn Single.just(createUser())

        // categoryModule
        whenever(d2.categoryModule()) doReturn mock()
        whenever(d2.categoryModule().categoryCombos()) doReturn mock()
        whenever(d2.categoryModule().categoryCombos().byIsDefault()) doReturn mock()
        whenever(d2.categoryModule().categoryCombos().byIsDefault().eq(true)) doReturn mock()
        whenever(d2.categoryModule().categoryCombos().byIsDefault().eq(true).one()) doReturn mock()
        whenever(
            d2.categoryModule().categoryCombos().byIsDefault().eq(true).one().get()
        ) doReturn Single.just(createCategoryCombo())

        whenever(d2.categoryModule().categoryOptionCombos()) doReturn mock()
        whenever(d2.categoryModule().categoryOptionCombos().byCode()) doReturn mock()
        whenever(d2.categoryModule().categoryOptionCombos().byCode().eq("default")) doReturn mock()
        whenever(
            d2.categoryModule().categoryOptionCombos().byCode().eq("default").one()
        ) doReturn mock()
        whenever(
            d2.categoryModule().categoryOptionCombos().byCode().eq("default").one().get()
        ) doReturn Single.just(
            createCategoryOptionCombo()
        )
    }

    private fun createUser(): User {
        return User.builder()
            .uid("userUid")
            .firstName("test_name")
            .surname("test_surName")
            .build()
    }

    private fun createCategoryCombo(): CategoryCombo {
        return CategoryCombo.builder()
            .uid("uid")
            .build()
    }

    private fun createCategoryOptionCombo(): CategoryOptionCombo {
        return CategoryOptionCombo.builder()
            .uid("uid")
            .build()
    }
}
