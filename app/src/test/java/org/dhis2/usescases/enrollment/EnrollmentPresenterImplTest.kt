package org.dhis2.usescases.enrollment

import io.reactivex.Single
import io.reactivex.processors.PublishProcessor
import org.dhis2.commons.matomo.MatomoAnalyticsController
import org.dhis2.commons.schedulers.SchedulerProvider
import org.dhis2.data.schedulers.TrampolineSchedulerProvider
import org.dhis2.form.data.EnrollmentRepository
import org.dhis2.usescases.enrollment.EnrollmentActivity.EnrollmentMode.CHECK
import org.dhis2.usescases.enrollment.EnrollmentActivity.EnrollmentMode.NEW
import org.dhis2.usescases.teiDashboard.TeiAttributesProvider
import org.dhis2.utils.analytics.AnalyticsHelper
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.arch.repositories.`object`.ReadOnlyOneObjectRepositoryFinalImpl
import org.hisp.dhis.android.core.common.Access
import org.hisp.dhis.android.core.common.DataAccess
import org.hisp.dhis.android.core.common.FeatureType
import org.hisp.dhis.android.core.common.Geometry
import org.hisp.dhis.android.core.enrollment.EnrollmentAccess
import org.hisp.dhis.android.core.enrollment.EnrollmentObjectRepository
import org.hisp.dhis.android.core.enrollment.EnrollmentStatus
import org.hisp.dhis.android.core.event.Event
import org.hisp.dhis.android.core.event.EventCollectionRepository
import org.hisp.dhis.android.core.event.EventStatus
import org.hisp.dhis.android.core.program.Program
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstance
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstanceObjectRepository
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class EnrollmentPresenterImplTest {

    private val enrollmentFormRepository: EnrollmentFormRepository = mock()
    private val programRepository: ReadOnlyOneObjectRepositoryFinalImpl<Program> = mock()
    private val teiRepository: TrackedEntityInstanceObjectRepository = mock()
    private val dataEntryRepository: EnrollmentRepository = mock()
    lateinit var presenter: EnrollmentPresenterImpl
    private val enrollmentView: EnrollmentView = mock()
    private val d2: D2 = Mockito.mock(D2::class.java, Mockito.RETURNS_DEEP_STUBS)
    private val enrollmentRepository: EnrollmentObjectRepository = mock()
    private val schedulers: SchedulerProvider = TrampolineSchedulerProvider()
    private val analyticsHelper: AnalyticsHelper = mock()
    private val matomoAnalyticsController: MatomoAnalyticsController = mock()
    private val eventCollectionRepository: EventCollectionRepository = mock()
    private val teiAttributesProvider: TeiAttributesProvider = mock()

    @Before
    fun setUp() {
        presenter = EnrollmentPresenterImpl(
            enrollmentView,
            d2,
            enrollmentRepository,
            dataEntryRepository,
            teiRepository,
            programRepository,
            schedulers,
            enrollmentFormRepository,
            analyticsHelper,
            matomoAnalyticsController,
            eventCollectionRepository,
            teiAttributesProvider,
        )
    }

    @Test
    fun `Check updateEnrollmentStatus where write access is granted`() {
        whenever(programRepository.blockingGet()) doReturn Program.builder().uid("")
            .access(
                Access.builder()
                    .data(
                        DataAccess.builder().write(true)
                            .build(),
                    ).build(),
            ).build()
        presenter.updateEnrollmentStatus(EnrollmentStatus.ACTIVE)
        verify(enrollmentRepository).setStatus(EnrollmentStatus.ACTIVE)
        verify(enrollmentView).renderStatus(EnrollmentStatus.ACTIVE)
    }

    @Test
    fun `Check updateEnrollmentStatus where write access is denied`() {
        whenever(programRepository.blockingGet()) doReturn Program.builder().uid("")
            .access(
                Access.builder()
                    .data(
                        DataAccess.builder().write(false)
                            .build(),
                    ).build(),
            ).build()
        presenter.updateEnrollmentStatus(EnrollmentStatus.ACTIVE)

        verify(enrollmentView).displayMessage(null)
    }

    @Test
    fun `Should update the fields flowable`() {
        val processor = PublishProcessor.create<Boolean>()
        val testSubscriber = processor.test()

        presenter.updateFields()
        processor.onNext(true)

        testSubscriber.assertValueAt(0, true)
    }

    @Test
    fun `Should execute the backButton processor`() {
        val processor = PublishProcessor.create<Boolean>()
        val testSubscriber = processor.test()

        presenter.backIsClicked()
        processor.onNext(true)

        testSubscriber.assertValueAt(0, true)
    }

    @Test
    fun `Should show a profile picture image`() {
        val path = "route/image"
        whenever(enrollmentFormRepository.getProfilePicture()) doReturn path
        presenter.onTeiImageHeaderClick()
        verify(enrollmentView).displayTeiPicture(path)
    }

    @Test
    fun `Should not show a profile picture image`() {
        val path = ""
        whenever(enrollmentFormRepository.getProfilePicture()) doReturn path
        presenter.onTeiImageHeaderClick()
        verify(enrollmentView, never()).displayTeiPicture(path)
    }

    @Test
    fun `Should show save button when the enrollment is editable`() {
        val geometry = Geometry.builder()
            .coordinates("[-30.00, 11.00]")
            .type(FeatureType.POINT)
            .build()
        val tei = TrackedEntityInstance.builder().geometry(geometry).uid("random").build()
        val program = Program.builder().uid("tUID").build()

        whenever(teiRepository.blockingGet()) doReturn tei
        whenever(programRepository.blockingGet()) doReturn program
        whenever(d2.enrollmentModule()) doReturn mock()
        whenever(d2.enrollmentModule().enrollmentService()) doReturn mock()
        whenever(
            d2.enrollmentModule().enrollmentService()
                .blockingGetEnrollmentAccess(tei.uid(), program.uid()),
        ) doReturn EnrollmentAccess.WRITE_ACCESS

        presenter.showOrHideSaveButton()

        verify(enrollmentView).setSaveButtonVisible(true)
    }

    @Test
    fun `Should hide save button when the enrollment is not editable`() {
        val geometry = Geometry.builder()
            .coordinates("[-30.00, 11.00]")
            .type(FeatureType.POINT)
            .build()
        val tei = TrackedEntityInstance.builder().geometry(geometry).uid("random").build()
        val program = Program.builder().uid("tUID").build()

        whenever(teiRepository.blockingGet()) doReturn tei
        whenever(programRepository.blockingGet()) doReturn program
        whenever(d2.enrollmentModule()) doReturn mock()
        whenever(d2.enrollmentModule().enrollmentService()) doReturn mock()
        whenever(
            d2.enrollmentModule().enrollmentService()
                .blockingGetEnrollmentAccess(tei.uid(), program.uid()),
        ) doReturn EnrollmentAccess.NO_ACCESS

        presenter.showOrHideSaveButton()

        verify(enrollmentView).setSaveButtonVisible(false)
    }

    @Test
    fun `Should return true if event status is SCHEDULE`() {
        val event = Event.builder().uid("uid").status(EventStatus.SCHEDULE).build()

        whenever(eventCollectionRepository.uid("uid")) doReturn mock()
        whenever(eventCollectionRepository.uid("uid").blockingGet()) doReturn event
        assert(presenter.isEventScheduleOrSkipped("uid"))
    }

    @Test
    fun `Should return true if event status is SKIPPED`() {
        val event = Event.builder().uid("uid").status(EventStatus.SKIPPED).build()

        whenever(eventCollectionRepository.uid("uid")) doReturn mock()
        whenever(eventCollectionRepository.uid("uid").blockingGet()) doReturn event
        assert(presenter.isEventScheduleOrSkipped("uid"))
    }

    @Test
    fun `Should return false if event status is ACTIVE`() {
        val event = Event.builder().uid("uid").status(EventStatus.ACTIVE).build()

        whenever(eventCollectionRepository.uid("uid")) doReturn mock()
        whenever(eventCollectionRepository.uid("uid").blockingGet()) doReturn event
        assert(!presenter.isEventScheduleOrSkipped("uid"))
    }

    @Test
    fun `should create an event right after enrollment creation`() {
        whenever(enrollmentFormRepository.generateEvents()) doReturn Single.just(
            Pair(
                "enrollmentUid",
                "eventUid",
            ),
        )

        presenter.finish(NEW)

        verify(enrollmentView).openEvent("eventUid")
    }

    @Test
    fun `should navigate to enrollment dashboard after enrollment creation`() {
        whenever(enrollmentFormRepository.generateEvents()) doReturn Single.just(
            Pair(
                "enrollmentUid",
                null,
            ),
        )

        presenter.finish(NEW)

        verify(enrollmentView).openDashboard("enrollmentUid")
    }

    @Test
    fun `should close enrollment screen if it already exists`() {
        presenter.finish(CHECK)

        verify(enrollmentView).setResultAndFinish()
    }
}
