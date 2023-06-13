package org.dhis2.usescases.teiDashboard.dashboardfragments.data

import android.view.View
import io.reactivex.Single
import org.dhis2.commons.filters.FilterManager
import org.dhis2.commons.filters.data.FilterRepository
import org.dhis2.commons.prefs.PreferenceProvider
import org.dhis2.commons.resources.ResourceManager
import org.dhis2.data.forms.dataentry.RuleEngineRepository
import org.dhis2.data.schedulers.TrampolineSchedulerProvider
import org.dhis2.form.data.FormValueStore
import org.dhis2.form.data.OptionsRepository
import org.dhis2.usescases.teiDashboard.DashboardRepository
import org.dhis2.usescases.teiDashboard.dashboardfragments.teidata.TEIDataContracts
import org.dhis2.usescases.teiDashboard.dashboardfragments.teidata.TEIDataPresenter
import org.dhis2.usescases.teiDashboard.dashboardfragments.teidata.TeiDataRepository
import org.dhis2.utils.analytics.AnalyticsHelper
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit
import org.hisp.dhis.android.core.program.ProgramStage
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class TeiDataPresenterTest {

    private val view: TEIDataContracts.View = mock()
    private val d2: D2 = Mockito.mock(D2::class.java, Mockito.RETURNS_DEEP_STUBS)
    private val dashboardRepository: DashboardRepository = mock()
    private val teiDataRepository: TeiDataRepository = mock()
    private val ruleEngineRepository: RuleEngineRepository = mock()
    private val programUid = "UID"
    private val teiUid = "123"
    private val enrollmentUid = "456"
    private val schedulers = TrampolineSchedulerProvider()
    private val preferences: PreferenceProvider = mock()
    private val analytics: AnalyticsHelper = mock()
    private val filterManager: FilterManager = mock()
    private val filterRepository: FilterRepository = mock()
    private lateinit var teiDataPresenter: TEIDataPresenter
    private val valueStore: FormValueStore = mock()
    private val resources: ResourceManager = mock()
    private val optionsRepository: OptionsRepository = mock()

    @Before
    fun setUp() {
        teiDataPresenter = TEIDataPresenter(
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
            filterManager,
            filterRepository,
            valueStore,
            resources,
            optionsRepository
        )
    }

    @Test
    fun `Should hide schedule event when hideDueDate is true`() {
        val programStage: ProgramStage = mock {
            on { hideDueDate() } doReturn true
        }

        val anyView: View = any()
        teiDataPresenter.onAddNewEvent(anyView, eq(programStage))

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
            teiDataPresenter.enrollmentOrgUnitInCaptureScope("orgUnitUid")
        )
    }

    @Test
    fun `Should show category combo dialog`() {
        whenever(
            teiDataRepository.eventsWithoutCatCombo()
        ) doReturn Single.just(mock())
        teiDataPresenter.getEventsWithoutCatCombo()
        verify(view).displayCatComboOptionSelectorForEvents(any())
    }

    @Test
    fun `Should return orgUnit display name`() {
        val uid = "orgUnitUid"
        whenever(teiDataRepository.getOrgUnitName(uid)) doReturn "OrgUnitDisplayName"
        assertTrue(teiDataPresenter.getOrgUnitName(uid) == "OrgUnitDisplayName")
    }
}
