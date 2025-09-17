package org.dhis2.usescases.teiDashboard.dialogs.scheduling

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.setMain
import org.dhis2.commons.bindings.enrollment
import org.dhis2.commons.bindings.programStage
import org.dhis2.commons.data.EventCreationType
import org.dhis2.commons.viewmodel.DispatcherProvider
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.arch.helpers.DateUtils
import org.hisp.dhis.android.core.arch.repositories.`object`.ReadOnlyOneObjectRepositoryFinalImpl
import org.hisp.dhis.android.core.enrollment.Enrollment
import org.hisp.dhis.android.core.enrollment.EnrollmentCollectionRepository
import org.hisp.dhis.android.core.enrollment.EnrollmentModule
import org.hisp.dhis.android.core.enrollment.EnrollmentObjectRepository
import org.hisp.dhis.android.core.period.PeriodModule
import org.hisp.dhis.android.core.program.ProgramModule
import org.hisp.dhis.android.core.program.ProgramStage
import org.hisp.dhis.android.core.program.ProgramStageCollectionRepository
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.spy
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

@OptIn(ExperimentalCoroutinesApi::class)
class SchedulingViewModelTest {
    private lateinit var schedulingViewModel: SchedulingViewModel

    private val testingDispatcher = UnconfinedTestDispatcher()

    private val enrollment = Enrollment.builder().uid(ENROLLMENT_UID).build()
    private val programStage = ProgramStage.builder().uid(STAGE).build()

    private val enrollmentObjectRepository: EnrollmentObjectRepository =
        mock {
            on { blockingGet() } doReturn enrollment
        }
    private val enrollmentCollectionRepository: EnrollmentCollectionRepository =
        mock {
            on { uid(ENROLLMENT_UID) } doReturn enrollmentObjectRepository
        }
    private val enrollmentModule: EnrollmentModule =
        mock {
            on { enrollments() } doReturn enrollmentCollectionRepository
        }

    private val readOnlyOneObjectRepositoryFinalImpl: ReadOnlyOneObjectRepositoryFinalImpl<ProgramStage> =
        mock {
            on { blockingGet() } doReturn programStage
        }
    private val programStageCollectionRepository: ProgramStageCollectionRepository =
        mock {
            on { uid(STAGE) } doReturn readOnlyOneObjectRepositoryFinalImpl
        }
    private val programModule: ProgramModule =
        mock {
            on { programStages() } doReturn programStageCollectionRepository
        }

    private val periodModule: PeriodModule =
        mock {
            on { periodHelper() } doReturn mock()
        }

    private val d2: D2 =
        mock {
            on { enrollmentModule() } doReturn enrollmentModule
            on { programModule() } doReturn programModule
            on { periodModule() } doReturn periodModule
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Before
    fun setUp() {
        Dispatchers.setMain(testingDispatcher)
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
        val spy = spy(schedulingViewModel)
        spy.onDateSet(2024, 4, 14)
        verify(spy).setUpEventReportDate(date)
    }

    @Test
    fun shouldSetReportDateForIncreasedNumberOfMonth() {
        val date = DateUtils.DATE_FORMAT.parse(REPORT_DATE_INCREASED_MONTH)
        val spy = spy(schedulingViewModel)
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
