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
import org.hisp.dhis.android.core.arch.helpers.DateUtils
import org.hisp.dhis.android.core.arch.repositories.`object`.ReadOnlyOneObjectRepositoryFinalImpl
import org.hisp.dhis.android.core.enrollment.Enrollment
import org.hisp.dhis.android.core.enrollment.EnrollmentCollectionRepository
import org.hisp.dhis.android.core.enrollment.EnrollmentModule
import org.hisp.dhis.android.core.enrollment.EnrollmentObjectRepository
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

    private val enrollment = Enrollment.builder().uid("enrollment-uid").build()
    private val programStage = ProgramStage.builder().uid("program-stage").build()

    private val enrollmentObjectRepository: EnrollmentObjectRepository = mock {
        on { blockingGet() } doReturn enrollment
    }
    private val enrollmentCollectionRepository: EnrollmentCollectionRepository = mock {
        on { uid("enrollment-uid") } doReturn enrollmentObjectRepository
    }
    private val enrollmentModule: EnrollmentModule = mock {
        on { enrollments() } doReturn enrollmentCollectionRepository
    }

    private val readOnlyOneObjectRepositoryFinalImpl: ReadOnlyOneObjectRepositoryFinalImpl<ProgramStage> = mock {
        on { blockingGet() } doReturn programStage
    }
    private val programStageCollectionRepository: ProgramStageCollectionRepository = mock {
        on { uid("program-stage") } doReturn readOnlyOneObjectRepositoryFinalImpl
    }
    private val programModule: ProgramModule = mock {
        on { programStages() } doReturn programStageCollectionRepository
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Before
    fun setUp() {
        Dispatchers.setMain(testingDispatcher)
        schedulingViewModel = SchedulingViewModel(
            d2 = mock {
                on { enrollmentModule() } doReturn enrollmentModule
                on { programModule() } doReturn programModule
            },
            resourceManager = mock(),
            eventResourcesProvider = mock(),
            periodUtils = mock(),
            dateUtils = mock(),
            dispatchersProvider = object : DispatcherProvider {
                override fun io(): CoroutineDispatcher {
                    return testingDispatcher
                }

                override fun computation(): CoroutineDispatcher {
                    return testingDispatcher
                }

                override fun ui(): CoroutineDispatcher {
                    return testingDispatcher
                }
            },
            launchMode = SchedulingDialog.LaunchMode.NewSchedule(
                enrollmentUid = "enrollment-uid",
                programStagesUids = listOf("program-stage"),
                showYesNoOptions = false,
                eventCreationType = EventCreationType.SCHEDULE,
            ),
        )
    }

    @Test
    fun shouldSetReportDate() {
        val date = DateUtils.DATE_FORMAT.parse("2024-04-14T00:00:00.000")
        val spy = spy(schedulingViewModel)
        spy.onDateSet(2024, 4, 14)
        verify(spy).setUpEventReportDate(date)
    }

    @Test
    fun shouldSetReportDateForIncreasedNumberOfMonth() {
        val date = DateUtils.DATE_FORMAT.parse("2025-01-31T00:00:00.000")
        val spy = spy(schedulingViewModel)
        spy.onDateSet(2024, 13, 31)
        verify(spy).setUpEventReportDate(date)
    }
}
