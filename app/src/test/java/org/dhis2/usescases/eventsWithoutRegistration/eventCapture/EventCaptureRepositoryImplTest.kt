package org.dhis2.usescases.eventsWithoutRegistration.eventCapture

import io.reactivex.Completable
import io.reactivex.Single
import org.dhis2.data.dhislogic.AUTH_ALL
import org.dhis2.data.dhislogic.AUTH_UNCOMPLETE_EVENT
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.category.CategoryOptionCombo
import org.hisp.dhis.android.core.dataelement.DataElement
import org.hisp.dhis.android.core.enrollment.Enrollment
import org.hisp.dhis.android.core.enrollment.EnrollmentStatus
import org.hisp.dhis.android.core.event.Event
import org.hisp.dhis.android.core.event.EventEditableStatus
import org.hisp.dhis.android.core.event.EventNonEditableReason
import org.hisp.dhis.android.core.event.EventStatus
import org.hisp.dhis.android.core.maintenance.D2Error
import org.hisp.dhis.android.core.maintenance.D2ErrorCode
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit
import org.hisp.dhis.android.core.program.ProgramStage
import org.hisp.dhis.android.core.program.ProgramStageSection
import org.hisp.dhis.android.core.settings.ProgramConfigurationSetting
import org.junit.Assert.assertTrue
import org.junit.Ignore
import org.junit.Test
import org.mockito.Mockito
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.util.Date
import java.util.GregorianCalendar

class EventCaptureRepositoryImplTest {

    private val eventUid = "eventUid"
    private val d2: D2 = Mockito.mock(D2::class.java, Mockito.RETURNS_DEEP_STUBS)

    private val trackerEventEnrollmentUid = "enrollmentUid"
    private val testEventStageUid = "stageUid"
    private val testEventOrgUnitUid = "orgUnitUid"
    private val testEventProgramUid = "programUid"

    private val sectionNameA = "sectionNameA"
    private val sectionUidA = "sectionUidA"
    private val sectionOrderA = 0
    private val sectionNameB = "sectionNameB"
    private val sectionUidB = "sectionUidB"
    private val sectionOrderB = 1
    private val sectionNameC = "sectionNameC"
    private val sectionUidC = "sectionUidC"
    private val sectionOrderC = 2
    private val sectionADataElementA = "sectionADataElementA"
    private val sectionBDataElementA = "sectionBDataElementA"
    private val sectionBDataElementB = "sectionBDataElementB"
    private val sectionCDataElementA = "sectionCDataElementA"

    @Test
    fun `isEnrollmentOpen returns true if current event has no enrollment`() {
        mockEvent()
        mockEmptySections()

        whenever(
            d2.enrollmentModule().enrollmentService().blockingIsOpen(eventUid),
        ) doReturn true

        val repository = EventCaptureRepositoryImpl(
            eventUid,
            d2,
        )

        assertTrue(repository.isEnrollmentOpen)
    }

    @Test
    fun `isEnrollmentOpen checks if enrollment is open`() {
        mockEvent(trackerEventEnrollmentUid)
        mockEmptySections()

        whenever(
            d2.enrollmentModule().enrollmentService().blockingIsOpen(eventUid),
        ) doReturn true

        val repository = EventCaptureRepositoryImpl(
            eventUid,
            d2,
        )

        repository.isEnrollmentOpen

        verify(d2.enrollmentModule().enrollmentService(), times(1))
            .blockingIsOpen(trackerEventEnrollmentUid)
    }

    @Test
    fun `isEnrollmentCancelled returns false if enrollment is null`() {
        mockEvent()
        mockEmptySections()

        whenever(
            d2.enrollmentModule().enrollments().uid(null).blockingGet(),
        ) doReturn null

        val repository = EventCaptureRepositoryImpl(
            eventUid,
            d2,
        )

        assertTrue(!repository.isEnrollmentCancelled)
    }

    @Test
    fun `isEnrollmentCancelled returns true if status is CANCELLED`() {
        mockEvent(trackerEventEnrollmentUid)
        mockEmptySections()

        whenever(
            d2.enrollmentModule().enrollments().uid(trackerEventEnrollmentUid).blockingGet(),
        ) doReturn Enrollment.builder()
            .uid(trackerEventEnrollmentUid)
            .status(EnrollmentStatus.CANCELLED)
            .build()

        val repository = EventCaptureRepositoryImpl(
            eventUid,
            d2,
        )

        assertTrue(repository.isEnrollmentCancelled)
    }

    @Test
    fun `isEventEditable checks event service access`() {
        mockEvent(trackerEventEnrollmentUid)
        mockEmptySections()

        val repository = EventCaptureRepositoryImpl(
            eventUid,
            d2,
        )

        repository.isEventEditable(eventUid)

        verify(d2.eventModule().eventService(), times(1)).blockingIsEditable(eventUid)
    }

    @Test
    fun `Should return stage name`() {
        mockEvent(trackerEventEnrollmentUid)
        mockEmptySections()

        val stageName = "stageName"

        whenever(
            d2.programModule().programStages().uid(any()).blockingGet()
        ) doReturn  ProgramStage.builder()
            .uid(testEventStageUid)
            .displayName(stageName)
            .build()

        whenever(
            d2.programModule().programStages().uid(testEventStageUid).get(),
        ) doReturn Single.just(
            ProgramStage.builder()
                .uid(testEventStageUid)
                .displayName(stageName)
                .build(),
        )

        val repository = EventCaptureRepositoryImpl(
            eventUid,
            d2,
        )

        val testObserver = repository.programStageName().test()
        testObserver.assertNoErrors()
            .assertValue { it == stageName }
    }

    @Test
    fun `Should return event date`() {
        mockEvent(trackerEventEnrollmentUid)
        mockEmptySections()

        val repository = EventCaptureRepositoryImpl(

            eventUid,
            d2,
        )

        repository.eventDate().test()
            .assertNoErrors()
            .assertValue { it == "1/1/2021" }
    }

    @Test
    fun `Should return event org unit`() {
        mockEvent(trackerEventEnrollmentUid)
        mockEmptySections()

        val repository = EventCaptureRepositoryImpl(

            eventUid,
            d2,
        )

        whenever(
            d2.organisationUnitModule().organisationUnits().uid(testEventOrgUnitUid).blockingGet(),
        ) doReturn OrganisationUnit.builder()
            .uid(testEventOrgUnitUid)
            .build()

        repository.orgUnit().test()
            .assertNoErrors()
            .assertValue { it.uid() == testEventOrgUnitUid }
    }

    @Test
    fun `Should return attribute option combo name`() {
        val attrOptionComboUid = "optionComboUid"
        val attrOptionComboName = "optionComboName"
        mockEvent(trackerEventEnrollmentUid, attrOptionComboUid)
        mockEmptySections()

        whenever(
            d2.categoryModule().categoryOptionCombos().uid(attrOptionComboUid).blockingGet(),
        ) doReturn CategoryOptionCombo.builder()
            .uid(attrOptionComboUid)
            .displayName(attrOptionComboName)
            .build()

        val repository = EventCaptureRepositoryImpl(

            eventUid,
            d2,
        )

        repository.catOption().test()
            .assertNoErrors()
            .assertValue { it == attrOptionComboName }
    }

    @Test
    fun `Should return empty string if attribute option como is default`() {
        val attrOptionComboUid = "defaultOptionComboUid"
        val attrOptionComboName = "default"
        mockEvent(trackerEventEnrollmentUid, attrOptionComboUid)
        mockEmptySections()

        whenever(
            d2.categoryModule().categoryOptionCombos().uid(attrOptionComboUid).blockingGet(),
        ) doReturn CategoryOptionCombo.builder()
            .uid(attrOptionComboUid)
            .displayName(attrOptionComboName)
            .build()

        val repository = EventCaptureRepositoryImpl(

            eventUid,
            d2,
        )

        repository.catOption().test()
            .assertNoErrors()
            .assertValue { it.isEmpty() }
    }

    @Test
    fun `Should return empty string if cat combo is null`() {
        val attrOptionComboUid = "optionComboUid"
        mockEvent(trackerEventEnrollmentUid, attrOptionComboUid)
        mockEmptySections()

        whenever(
            d2.categoryModule().categoryOptionCombos().uid(attrOptionComboUid).blockingGet(),
        ) doReturn null

        val repository = EventCaptureRepositoryImpl(

            eventUid,
            d2,
        )

        repository.catOption().test()
            .assertNoErrors()
            .assertValue { it.isEmpty() }
    }

    @Ignore("Use EventCaptureFieldProvider in the list method of the repository")
    @Test
    fun `Should return list of fields`() {
    }

    @Test
    fun `Should complete event`() {
        mockEvent()
        mockSections()

        val repository = EventCaptureRepositoryImpl(

            eventUid,
            d2,
        )

        whenever(
            d2.eventModule().events().uid(eventUid),
        ) doReturn mock()

        repository.completeEvent().test()
            .assertNoErrors()
            .assertValue { it }
    }

    @Test
    fun `Should throw error when completing event`() {
        mockEvent()
        mockSections()

        val repository = EventCaptureRepositoryImpl(

            eventUid,
            d2,
        )

        whenever(
            d2.eventModule().events().uid(eventUid),
        ) doReturn mock()
        whenever(
            d2.eventModule().events().uid(eventUid).setStatus(any()),
        ) doThrow D2Error.builder()
            .errorCode(D2ErrorCode.UNEXPECTED)
            .errorDescription("error test")
            .build()

        repository.completeEvent().test()
            .assertNoErrors()
            .assertValue { !it }
    }

    @Test
    fun `Should delete event`() {
        mockEvent()
        mockSections()

        val repository = EventCaptureRepositoryImpl(

            eventUid,
            d2,
        )

        whenever(
            d2.eventModule().events().uid(eventUid).delete(),
        ) doReturn Completable.complete()

        repository.deleteEvent().test()
            .assertNoErrors()
            .assertValue { it }
    }

    @Test
    fun `Should update status`() {
        mockEvent()
        mockSections()

        val repository = EventCaptureRepositoryImpl(

            eventUid,
            d2,
        )
        val testStatus = EventStatus.SKIPPED
        whenever(
            d2.eventModule().events().uid(eventUid),
        ) doReturn mock()

        repository.updateEventStatus(testStatus).test()
            .assertNoErrors()
            .assertValue { it }
    }

    @Test
    fun `Should reschedule event`() {
        mockEvent()
        mockSections()

        val repository = EventCaptureRepositoryImpl(
            eventUid,
            d2,
        )
        val testNewDate = GregorianCalendar(3021, 11, 1).time
        whenever(
            d2.eventModule().events().uid(eventUid),
        ) doReturn mock()

        repository.rescheduleEvent(testNewDate).test()
            .assertNoErrors()
            .assertValue { it }

        verify(d2.eventModule().events().uid(eventUid)).setDueDate(testNewDate)
        verify(d2.eventModule().events().uid(eventUid)).setStatus(EventStatus.SCHEDULE)
    }

    @Test
    fun `Should return stage`() {
        mockEvent()
        mockSections()

        val repository = EventCaptureRepositoryImpl(

            eventUid,
            d2,
        )

        repository.programStage().test()
            .assertNoErrors()
            .assertValue { it == testEventStageUid }
    }

    @Test
    fun `Should get event access`() {
        mockEvent()
        mockSections()

        val repository = EventCaptureRepositoryImpl(

            eventUid,
            d2,
        )

        whenever(
            d2.eventModule().eventService(),
        ) doReturn mock()

        repository.accessDataWrite

        verify(d2.eventModule().eventService()).blockingHasDataWriteAccess(eventUid)
    }

    @Test
    fun `Should return event status`() {
        mockEvent()
        mockSections()

        val repository = EventCaptureRepositoryImpl(

            eventUid,
            d2,
        )

        repository.eventStatus().test()
            .assertNoErrors()
            .assertValue { it == EventStatus.ACTIVE }
    }

    @Test
    fun `Should check if event can be opened`() {
        mockEvent()
        mockSections()

        val repository = EventCaptureRepositoryImpl(

            eventUid,
            d2,
        )

        whenever(
            d2.userModule().authorities()
                .byName(),
        ) doReturn mock()

        whenever(
            d2.userModule().authorities()
                .byName().`in`(AUTH_UNCOMPLETE_EVENT, AUTH_ALL),
        ) doReturn mock()

        whenever(
            d2.userModule().authorities()
                .byName().`in`(AUTH_UNCOMPLETE_EVENT, AUTH_ALL)
                .one(),
        ) doReturn mock()

        whenever(
            d2.userModule().authorities()
                .byName().`in`(AUTH_UNCOMPLETE_EVENT, AUTH_ALL)
                .one()
                .blockingExists(),
        ) doReturn true

        repository.canReOpenEvent().test()
            .assertNoErrors()
            .assertValue { it }
    }

    @Test
    fun `Should return true if completed event is expired`() {
        mockEvent(status = EventStatus.COMPLETED)
        mockSections()

        val repository = EventCaptureRepositoryImpl(

            eventUid,
            d2,
        )

        whenever(
            d2.eventModule().eventService().getEditableStatus(eventUid),
        ) doReturn Single.just(
            EventEditableStatus.NonEditable(EventNonEditableReason.EXPIRED) as EventEditableStatus,
        )

        repository.isCompletedEventExpired(eventUid).test()
            .assertNoErrors()
            .assertValue { it }
    }

    @Test
    fun `Should return false if completed event is expired`() {
        mockEvent(status = EventStatus.COMPLETED)
        mockSections()

        val repository = EventCaptureRepositoryImpl(

            eventUid,
            d2,
        )

        whenever(
            d2.eventModule().eventService().getEditableStatus(eventUid),
        ) doReturn Single.just(
            EventEditableStatus.Editable() as EventEditableStatus,
        )

        repository.isCompletedEventExpired(eventUid).test()
            .assertNoErrors()
            .assertValue { !it }
    }

    @Test
    fun `Integrity check should return true`() {
        mockEvent()
        mockSections()

        val repository = EventCaptureRepositoryImpl(

            eventUid,
            d2,
        )

        repository.eventIntegrityCheck().test()
            .assertNoErrors()
            .assertValue { it }
    }

    @Test
    fun `Integrity check should return false for event in the future`() {
        mockEvent(eventDate = GregorianCalendar(3021, 0, 1).time)
        mockSections()

        val repository = EventCaptureRepositoryImpl(

            eventUid,
            d2,
        )

        repository.eventIntegrityCheck().test()
            .assertNoErrors()
            .assertValue { !it }
    }

    @Test
    fun `Should return event count`() {
        mockEvent()
        mockSections()

        val repository = EventCaptureRepositoryImpl(

            eventUid,
            d2,
        )
        val numberOfNotes = 12
        whenever(
            d2.noteModule().notes().byEventUid(),
        ) doReturn mock()
        whenever(
            d2.noteModule().notes().byEventUid().eq(eventUid),
        ) doReturn mock()
        whenever(
            d2.noteModule().notes().byEventUid().eq(eventUid).count(),
        ) doReturn Single.just(numberOfNotes)

        repository.noteCount.test()
            .assertNoErrors()
            .assertValue { it == 12 }
    }

    @Test
    fun `Should return true if no settings is available`() {
        mockEvent()
        mockSections()

        val repository = EventCaptureRepositoryImpl(

            eventUid,
            d2,
        )

        whenever(
            d2.settingModule().appearanceSettings().blockingExists(),
        ) doReturn false

        assertTrue(repository.showCompletionPercentage())
    }

    @Test
    fun `Should return true if percentage settings is visible`() {
        mockEvent()
        mockSections()

        val repository = EventCaptureRepositoryImpl(

            eventUid,
            d2,
        )

        whenever(
            d2.settingModule().appearanceSettings().blockingExists(),
        ) doReturn true

        whenever(
            d2.settingModule()
                .appearanceSettings()
                .getProgramConfigurationByUid(testEventProgramUid),
        ) doReturn ProgramConfigurationSetting.builder()
            .completionSpinner(true)
            .build()

        assertTrue(repository.showCompletionPercentage())
    }

    @Test
    fun `Should return false if percentage settings is not visible`() {
        mockEvent()
        mockSections()

        val repository = EventCaptureRepositoryImpl(

            eventUid,
            d2,
        )

        whenever(
            d2.settingModule().appearanceSettings().blockingExists(),
        ) doReturn true

        whenever(
            d2.settingModule()
                .appearanceSettings()
                .getProgramConfigurationByUid(testEventProgramUid),
        ) doReturn ProgramConfigurationSetting.builder()
            .completionSpinner(false)
            .build()

        assertTrue(!repository.showCompletionPercentage())
    }

    @Test
    fun `Should have analytics if there are indicators`() {
        mockEvent()
        mockSections()

        val repository = EventCaptureRepositoryImpl(
            eventUid,
            d2,
        )
        whenever(
            d2.programModule().programIndicators().byProgramUid().eq(testEventProgramUid),
        ) doReturn mock()
        whenever(
            d2.programModule().programIndicators().byProgramUid().eq(any()).blockingIsEmpty(),
        ) doReturn false
        whenever(
            d2.programModule().programRules().withProgramRuleActions().byProgramUid()
                .eq(testEventProgramUid),
        ) doReturn mock()
        whenever(
            d2.programModule().programRules().withProgramRuleActions().byProgramUid()
                .eq(testEventProgramUid).blockingGet(),
        ) doReturn emptyList()

        assertTrue(repository.hasAnalytics())
    }

    private fun mockEvent(
        enrollmentUid: String? = null,
        attrOptionComboUid: String? = null,
        deleted: Boolean = false,
        status: EventStatus = EventStatus.ACTIVE,
        eventDate: Date = GregorianCalendar(2021, 0, 1).time,
    ) {
        whenever(
            d2.eventModule().events().uid(eventUid).blockingGet(),
        ) doReturn Event.builder()
            .uid(eventUid)
            .apply {
                enrollmentUid?.let { enrollment(it) }
                attrOptionComboUid?.let { attributeOptionCombo(it) }
            }
            .programStage(testEventStageUid)
            .eventDate(eventDate)
            .organisationUnit(testEventOrgUnitUid)
            .deleted(deleted)
            .status(status)
            .program(testEventProgramUid)
            .build()
    }

    private fun mockEmptySections() {
        whenever(
            d2.programModule().programStageSections().byProgramStageUid().eq(testEventStageUid),
        ) doReturn mock()
        whenever(
            d2.programModule().programStageSections().byProgramStageUid().eq(testEventStageUid)
                .withDataElements(),
        ) doReturn mock()
        whenever(
            d2.programModule().programStageSections().byProgramStageUid().eq(testEventStageUid)
                .withDataElements().blockingGet(),
        ) doReturn listOf()
    }

    private fun mockSections() {
        whenever(
            d2.programModule().programStageSections().byProgramStageUid().eq(testEventStageUid),
        ) doReturn mock()
        whenever(
            d2.programModule().programStageSections().byProgramStageUid().eq(testEventStageUid)
                .withDataElements(),
        ) doReturn mock()
        whenever(
            d2.programModule().programStageSections().byProgramStageUid().eq(testEventStageUid)
                .withDataElements().blockingGet(),
        ) doReturn listOf(
            ProgramStageSection.builder()
                .uid(sectionUidC)
                .displayName(sectionNameC)
                .sortOrder(sectionOrderC)
                .dataElements(
                    mutableListOf(
                        DataElement.builder().uid(sectionCDataElementA).build(),
                    ),
                )
                .build(),
            ProgramStageSection.builder()
                .uid(sectionUidB)
                .displayName(sectionNameB)
                .sortOrder(sectionOrderB)
                .dataElements(
                    mutableListOf(
                        DataElement.builder().uid(sectionBDataElementA).build(),
                        DataElement.builder().uid(sectionBDataElementB).build(),
                    ),
                )
                .build(),
            ProgramStageSection.builder()
                .uid(sectionUidA)
                .displayName(sectionNameA)
                .sortOrder(sectionOrderA)
                .dataElements(
                    mutableListOf(
                        DataElement.builder().uid(sectionADataElementA).build(),
                    ),
                )
                .build(),
        )
    }
}
