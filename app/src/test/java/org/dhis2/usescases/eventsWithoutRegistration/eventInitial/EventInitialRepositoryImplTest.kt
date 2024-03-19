package org.dhis2.usescases.eventsWithoutRegistration.eventInitial

import io.reactivex.Single
import org.dhis2.commons.resources.MetadataIconProvider
import org.dhis2.form.ui.FieldViewModelFactory
import org.dhis2.mobileProgramRules.RuleEngineHelper
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.common.Access
import org.hisp.dhis.android.core.common.DataAccess
import org.hisp.dhis.android.core.common.FeatureType
import org.hisp.dhis.android.core.common.Geometry
import org.hisp.dhis.android.core.enrollment.Enrollment
import org.hisp.dhis.android.core.enrollment.EnrollmentStatus
import org.hisp.dhis.android.core.event.Event
import org.hisp.dhis.android.core.event.EventStatus
import org.hisp.dhis.android.core.program.Program
import org.hisp.dhis.android.core.program.ProgramStage
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.util.ArrayList

class EventInitialRepositoryImplTest {
    private lateinit var repository: EventInitialRepositoryImpl
    private val eventUid = "eventUid"
    private val stageUid = "stageUid"
    private val d2: D2 = Mockito.mock(D2::class.java, Mockito.RETURNS_DEEP_STUBS)
    private val fieldFactory: FieldViewModelFactory = mock()
    private val ruleEngineHelper: RuleEngineHelper = mock()
    private val metadataIconProvider: MetadataIconProvider = mock()

    @Before
    fun setUp() {
        repository = EventInitialRepositoryImpl(
            eventUid,
            stageUid,
            d2,
            fieldFactory,
            ruleEngineHelper,
            metadataIconProvider,
        )
    }

    @Test
    fun `Should return editable geometry model`() {
        whenever(
            d2.eventModule().eventService().isEditable(eventUid),
        ) doReturn Single.just(true)
        val event = Event.builder()
            .uid(eventUid)
            .geometry(Geometry.builder().type(FeatureType.POINT).coordinates("[0,0]").build())
            .status(EventStatus.ACTIVE)
            .build()
        whenever(
            d2.eventModule().events().uid(eventUid).blockingGet(),
        ) doReturn event
        whenever(
            d2.eventModule().events().uid(eventUid).get(),
        ) doReturn Single.just(event)
        mockStage(true)
        mockProgramAccess(true)
        mockEnrollment(null)

        val accessDataWrite = repository
            .accessDataWrite("programUid")
            .blockingFirst() && repository.isEnrollmentOpen
        val nonEditableStatus = ArrayList<EventStatus>()
        nonEditableStatus.add(EventStatus.COMPLETED)
        nonEditableStatus.add(EventStatus.SKIPPED)
        val shouldBlockEdition = !d2.eventModule().eventService().blockingIsEditable(eventUid) &&
            nonEditableStatus.contains(
                d2.eventModule().events().uid(eventUid).blockingGet()?.status(),
            )

        val editableField = accessDataWrite && !shouldBlockEdition
        assertTrue(editableField)
    }

    @Test
    fun `Should return not editable geometry model if stage has no access`() {
        whenever(
            d2.eventModule().eventService().blockingIsEditable(eventUid),
        ) doReturn true
        val event = Event.builder()
            .uid(eventUid)
            .geometry(Geometry.builder().type(FeatureType.POINT).coordinates("[0,0]").build())
            .status(EventStatus.ACTIVE)
            .build()
        whenever(
            d2.eventModule().events().uid(eventUid).blockingGet(),
        ) doReturn event
        whenever(
            d2.eventModule().events().uid(eventUid).get(),
        ) doReturn Single.just(event)
        mockStage(false)
        mockProgramAccess(true)
        mockEnrollment(null)

        val accessDataWrite =
            repository
                .accessDataWrite("programUid")
                .blockingFirst() && repository.isEnrollmentOpen
        val nonEditableStatus = ArrayList<EventStatus>()
        nonEditableStatus.add(EventStatus.COMPLETED)
        nonEditableStatus.add(EventStatus.SKIPPED)
        val shouldBlockEdition = !d2.eventModule().eventService().blockingIsEditable(eventUid) &&
            nonEditableStatus.contains(
                d2.eventModule().events().uid(eventUid).blockingGet()?.status(),
            )

        val editableField = accessDataWrite && !shouldBlockEdition
        assertFalse(editableField)
    }

    @Test
    fun `Should return not editable geometry model if program has no access`() {
        whenever(
            d2.eventModule().eventService().blockingIsEditable(eventUid),
        ) doReturn true
        val event = Event.builder()
            .uid(eventUid)
            .geometry(Geometry.builder().type(FeatureType.POINT).coordinates("[0,0]").build())
            .status(EventStatus.ACTIVE)
            .build()
        whenever(
            d2.eventModule().events().uid(eventUid).blockingGet(),
        ) doReturn event
        whenever(
            d2.eventModule().events().uid(eventUid).get(),
        ) doReturn Single.just(event)
        mockStage(true)
        mockProgramAccess(false)
        mockEnrollment(null)

        val accessDataWrite =
            repository
                .accessDataWrite("programUid")
                .blockingFirst() && repository.isEnrollmentOpen
        val nonEditableStatus = ArrayList<EventStatus>()
        nonEditableStatus.add(EventStatus.COMPLETED)
        nonEditableStatus.add(EventStatus.SKIPPED)
        val shouldBlockEdition = !d2.eventModule().eventService().blockingIsEditable(eventUid) &&
            nonEditableStatus.contains(
                d2.eventModule().events().uid(eventUid).blockingGet()?.status(),
            )

        val editableField = accessDataWrite && !shouldBlockEdition
        assertFalse(editableField)
    }

    private fun mockProgramAccess(hasAccess: Boolean) {
        whenever(
            d2.programModule().programs().uid("programUid").blockingGet(),
        ) doReturn Program.builder()
            .uid("programUid")
            .access(Access.create(true, true, DataAccess.create(true, hasAccess)))
            .build()
    }

    private fun mockStage(hasAccess: Boolean) {
        val stage = ProgramStage.builder()
            .uid(stageUid)
            .featureType(FeatureType.POINT)
            .access(Access.create(true, true, DataAccess.create(true, hasAccess)))
            .build()
        whenever(
            d2.programModule().programStages().uid(stageUid).get(),
        ) doReturn Single.just(
            stage,

        )
        whenever(
            d2.programModule().programStages().uid(stageUid).blockingGet(),
        ) doReturn stage
    }

    private fun mockEnrollment(enrollmentUid: String?) {
        whenever(
            d2.enrollmentModule().enrollments().uid(enrollmentUid).blockingGet(),
        ) doReturn if (enrollmentUid != null) {
            Enrollment.builder()
                .uid(enrollmentUid)
                .status(EnrollmentStatus.ACTIVE)
                .build()
        } else {
            null
        }
    }
}
