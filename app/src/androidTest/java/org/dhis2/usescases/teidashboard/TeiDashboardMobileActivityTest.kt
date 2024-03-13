package org.dhis2.usescases.teidashboard

import android.view.View
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.rules.activityScenarioRule
import dhis2.org.analytics.charts.Charts
import io.reactivex.Observable
import org.dhis2.R
import org.dhis2.android.rtsm.utils.NetworkUtils
import org.dhis2.commons.filters.FilterManager
import org.dhis2.commons.prefs.PreferenceProvider
import org.dhis2.commons.resources.MetadataIconProvider
import org.dhis2.commons.resources.ResourceManager
import org.dhis2.commons.viewmodel.DispatcherProvider
import org.dhis2.ui.MetadataIconData
import org.dhis2.ui.ThemeManager
import org.dhis2.usescases.teiDashboard.DashboardRepositoryImpl
import org.dhis2.usescases.teiDashboard.DashboardViewModel
import org.dhis2.usescases.teiDashboard.TeiAttributesProvider
import org.dhis2.usescases.teiDashboard.TeiDashboardContracts
import org.dhis2.usescases.teiDashboard.TeiDashboardMobileActivity
import org.dhis2.utils.analytics.AnalyticsHelper
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstance
import org.hisp.dhis.android.core.trackedentity.TrackedEntityType
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.util.Calendar

class TeiDashboardMobileActivityTest {

    @get:Rule
    val activityScenarioRule = activityScenarioRule<TeiDashboardMobileActivity>()

    @get:Rule
    val composeRule = createAndroidComposeRule<TeiDashboardMobileActivity>()


    private lateinit var viewModel: DashboardViewModel

    private val d2: D2 = Mockito.mock(D2::class.java, Mockito.RETURNS_DEEP_STUBS)
    private val resources: ResourceManager = mock()
    private val charts: Charts = mock()
    private val teiAttributesProvider: TeiAttributesProvider = mock()
    private val preferences: PreferenceProvider = mock()

    private val metadataIconProvider: MetadataIconProvider = mock {
        on { invoke(any(), any<String>(), any()) }doReturn MetadataIconData.defaultIcon()
    }

    private var repository: DashboardRepositoryImpl = mock {

    }

    private var dispatcher: DispatcherProvider = mock()
    var tei = Observable.just(
        TrackedEntityInstance.builder()
            .uid(TEI_Uid)
            .created(Calendar.getInstance().time)
            .lastUpdated(Calendar.getInstance().time)
            .organisationUnit(ORG_UNIT_UID)
            .trackedEntityType(TETYPE_NAME)
            .build()
    )

    private val teType: TrackedEntityType = mock()

    private val analyticsHelper = mock<AnalyticsHelper> {
    }

    private val themeManager: ThemeManager = mock()
    private val presenter: TeiDashboardContracts.Presenter = mock()
    private val filterManager: FilterManager = mock()
    private val networkUtils: NetworkUtils = mock()

    companion object {
        const val ENROLLMENT_UID = "enrollmentUid"
        const val TEI_Uid = "TEIUid"
        const val PROGRAM_UID = "programUid"
        const val TETYPE_NAME = "TETypeName"
        const val INITIAL_ORG_UNIT_UID = "initialOrgUnitUid"
        const val PROGRAM_STAGE_NAME = "Marvellous Program Stage"
        const val EXECUTION_DATE = "Date of Marvellous Program Stage"
        const val ORG_UNIT_UID = "orgUnitUid"
        const val ENROLLMENT_VALUE_WITH_NOTE = "EnrollmentValueWithNote"
        const val TEI_UID_VALUE_WITH_NOTE = "TeiUidValueWithNote"
        const val CHILD_PROGRAM_UID_VALUE = "childProgramUid"
    }

    private fun initViewModel() {
        viewModel = DashboardViewModel(
            repository,
            analyticsHelper,
            dispatcher,
        )

    }

    private fun setUp() {
        initRepository()
        initViewModel()
    }

    private fun initRepository() {
        repository = DashboardRepositoryImpl(
            d2,
            charts,
            TEI_Uid,
            PROGRAM_UID,
            ENROLLMENT_UID,
            teiAttributesProvider,
            preferences,
            metadataIconProvider
        )


    }


    @Test
    fun shouldSuccessfullyInitializeTeiDashBoardMobileActivity() {
        setUp()
        whenever(repository.getTETypeName()) doReturn TETYPE_NAME
        whenever(repository.getTrackedEntityInstance("")) doReturn mock()
        whenever {
            repository.getTrackedEntityInstance("").flatMap { tei: TrackedEntityInstance ->
                d2.trackedEntityModule().trackedEntityTypes()
                    .uid(tei.trackedEntityType())
                    .get()
                    .toObservable()
            }
        } doReturn mock()
        whenever {
            repository.getTrackedEntityInstance("").flatMap { tei: TrackedEntityInstance ->
                d2.trackedEntityModule().trackedEntityTypes()
                    .uid(tei.trackedEntityType())
                    .get()
                    .toObservable()
            }.blockingFirst()
        } doReturn { teType }
        whenever(
            presenter.teType
        ) doReturn TETYPE_NAME

        whenever(
            repository.getTETypeName()
        ) doReturn TETYPE_NAME
        whenever(
            d2.trackedEntityModule()
        ) doReturn mock()
        whenever(
            d2.trackedEntityModule().trackedEntityInstances()
        ) doReturn mock()
        whenever(
            d2.trackedEntityModule().trackedEntityInstances().byUid()
        ) doReturn mock()
        whenever(
            d2.trackedEntityModule().trackedEntityInstances().byUid().eq("")
        ) doReturn mock()
        whenever(
            d2.trackedEntityModule().trackedEntityInstances().byUid().eq("").one()
        ) doReturn mock()
        whenever(
            d2.trackedEntityModule().trackedEntityInstances().byUid().eq("").one()
                .blockingGet()
        ) doReturn mock()

        whenever(
            d2.trackedEntityModule().trackedEntityTypes()
        ) doReturn mock()
        whenever(
            d2.trackedEntityModule().trackedEntityTypes().uid("")
        ) doReturn mock()
        whenever(
            d2.trackedEntityModule().trackedEntityTypes().uid("").get()
        ) doReturn mock()
        whenever(
            d2.trackedEntityModule().trackedEntityTypes().uid("").get().toObservable()
        ) doReturn mock()
        whenever(
            d2.trackedEntityModule().trackedEntityTypes().uid("").get().toObservable()
        ) doReturn mock()


        ActivityScenario.launch(TeiDashboardMobileActivity::class.java).onActivity { activity ->

            val showMoreOptions = activity.findViewById<View>(R.id.moreOptions)
            showMoreOptions.performClick()
        }
    }
}