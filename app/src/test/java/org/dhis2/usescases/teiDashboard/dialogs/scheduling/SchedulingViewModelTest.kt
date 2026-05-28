package org.dhis2.usescases.teiDashboard.dialogs.scheduling

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.dhis2.commons.data.EventCreationType
import org.dhis2.commons.viewmodel.DispatcherProvider
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.arch.helpers.DateUtils
import org.hisp.dhis.android.core.enrollment.Enrollment
import org.hisp.dhis.android.core.event.EventCollectionRepository
import org.hisp.dhis.android.core.program.ProgramCollectionRepository
import org.hisp.dhis.android.core.program.ProgramStage
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class SchedulingViewModelTest {
    private lateinit var schedulingViewModel: SchedulingViewModel

    private val testingDispatcher = StandardTestDispatcher()

    private val enrollment =
        Enrollment
            .builder()
            .uid(ENROLLMENT_UID)
            .attributeOptionCombo("attributeOptionComboUid")
            .build()

    private val d2: D2 = Mockito.mock(D2::class.java, Mockito.RETURNS_DEEP_STUBS)

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Before
    fun setUp() {
        Dispatchers.setMain(testingDispatcher)
        whenever(d2.enrollmentModule().enrollments().uid(ENROLLMENT_UID).blockingGet()) doReturn enrollment
        whenever(d2.programModule().programStages().uid(STAGE).blockingGet()) doReturn
            ProgramStage.builder().uid(STAGE).build()
        whenever(d2.eventModule().events()) doReturn
            Mockito.mock(EventCollectionRepository::class.java, Mockito.RETURNS_DEEP_STUBS)
        whenever(d2.programModule().programs()) doReturn
            Mockito.mock(ProgramCollectionRepository::class.java, Mockito.RETURNS_DEEP_STUBS)
        schedulingViewModel =
            SchedulingViewModel(
                d2 = d2,
                resourceManager = mock(),
                eventResourcesProvider = mock(),
                periodUtils = mock(),
                dateUtils = mock(),
                dispatchersProvider =
                    object : DispatcherProvider {
                        override fun io(): CoroutineDispatcher = testingDispatcher

                        override fun computation(): CoroutineDispatcher = testingDispatcher

                        override fun ui(): CoroutineDispatcher = testingDispatcher
                    },
                launchMode =
                    SchedulingDialog.LaunchMode.NewSchedule(
                        enrollmentUid = ENROLLMENT_UID,
                        programStagesUids = listOf(STAGE),
                        showYesNoOptions = false,
                        eventCreationType = EventCreationType.SCHEDULE,
                        ownerOrgUnitUid = OWNER_ORG_UNIT_UID,
                    ),
                getEventPeriods = mock(),
            )
    }

    @Test
    fun shouldSetReportDate() {
        val date = DateUtils.DATE_FORMAT.parse(REPORT_DATE)
        val spy = Mockito.spy(schedulingViewModel)
        spy.onDateSet(2024, 4, 14)
        verify(spy).setUpEventReportDate(date)
    }

    @Test
    fun shouldSetReportDateForIncreasedNumberOfMonth() {
        val date = DateUtils.DATE_FORMAT.parse(REPORT_DATE_INCREASED_MONTH)
        val spy = Mockito.spy(schedulingViewModel)
        spy.onDateSet(2024, 13, 31)
        verify(spy).setUpEventReportDate(date)
    }

    companion object {
        const val REPORT_DATE = "2024-04-14T00:00:00.000"
        const val REPORT_DATE_INCREASED_MONTH = "2025-01-31T00:00:00.000"
        const val STAGE = "program-stage"
        const val OWNER_ORG_UNIT_UID = "ownerOrgUnitUid"
        const val ENROLLMENT_UID = "enrollment-uid"
    }
}
