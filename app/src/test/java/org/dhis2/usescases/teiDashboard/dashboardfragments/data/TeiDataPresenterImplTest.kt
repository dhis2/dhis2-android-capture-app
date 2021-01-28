package org.dhis2.usescases.teiDashboard.dashboardfragments.data

import android.view.View
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.schedulers.TestScheduler
import org.dhis2.data.forms.dataentry.RuleEngineRepository
import org.dhis2.data.prefs.PreferenceProvider
import org.dhis2.data.schedulers.TestSchedulerProvider
import org.dhis2.usescases.teiDashboard.DashboardRepository
import org.dhis2.usescases.teiDashboard.dashboardfragments.teidata.TEIDataContracts
import org.dhis2.usescases.teiDashboard.dashboardfragments.teidata.TEIDataPresenterImpl
import org.dhis2.usescases.teiDashboard.dashboardfragments.teidata.TeiDataRepository
import org.dhis2.utils.analytics.AnalyticsHelper
import org.dhis2.utils.filters.FilterManager
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit
import org.hisp.dhis.android.core.program.ProgramStage
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito

class TeiDataPresenterImplTest {

    private val view: TEIDataContracts.View = mock()
    private val d2: D2 = Mockito.mock(D2::class.java, Mockito.RETURNS_DEEP_STUBS)
    private val dashboardRepository: DashboardRepository = mock()
    private val teiDataRepository: TeiDataRepository = mock()
    private val ruleEngineRepository: RuleEngineRepository = mock()
    private val programUid = "UID"
    private val teiUid = "123"
    private val enrollmentUid = "456"
    private val schedulers: TestSchedulerProvider = TestSchedulerProvider(TestScheduler())
    private val preferences: PreferenceProvider = mock()
    private val analytics: AnalyticsHelper = mock()
    private val filterManager: FilterManager = mock()

    private lateinit var teiDataPresenterImpl: TEIDataContracts.Presenter

    @Before
    fun setUp() {
        teiDataPresenterImpl = TEIDataPresenterImpl(
            view,
            d2,
            dashboardRepository,
            teiDataRepository,
            ruleEngineRepository,
            programUid,
            teiUid,
            enrollmentUid,
            schedulers,
            preferences,
            analytics,
            filterManager
        )
    }

    @Test
    fun `Should hide schedule event when hideDueDate is true`() {
        val programStage: ProgramStage = mock {
            on { hideDueDate() } doReturn true
        }

        val anyView: View = any()
        teiDataPresenterImpl.onAddNewEvent(anyView, eq(programStage))

        verify(view).showNewEventOptions(anyView, programStage)
        verify(view).hideDueDate()
    }

    @Test
    fun `Should return false if orgUnit does not belong to the capture scope`() {
        whenever(
            d2.organisationUnitModule().organisationUnits()
        ) doReturn mock()
        whenever(
            d2.organisationUnitModule().organisationUnits()
                .byOrganisationUnitScope(OrganisationUnit.Scope.SCOPE_DATA_CAPTURE)
        ) doReturn mock()
        whenever(
            d2.organisationUnitModule().organisationUnits()
                .byOrganisationUnitScope(OrganisationUnit.Scope.SCOPE_DATA_CAPTURE)
                .byUid()
        ) doReturn mock()
        whenever(
            d2.organisationUnitModule().organisationUnits()
                .byOrganisationUnitScope(OrganisationUnit.Scope.SCOPE_DATA_CAPTURE)
                .byUid().eq("orgUnitUid")
        ) doReturn mock()
        whenever(
            d2.organisationUnitModule().organisationUnits()
                .byOrganisationUnitScope(OrganisationUnit.Scope.SCOPE_DATA_CAPTURE)
                .byUid().eq("orgUnitUid")
                .blockingIsEmpty()
        ) doReturn false
        assertTrue(
            teiDataPresenterImpl.enrollmentOrgUnitInCaptureScope("orgUnitUid")
        )
    }
}
