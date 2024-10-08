package org.dhis2.usescases.teiDashboard

import dhis2.org.analytics.charts.Charts
import io.reactivex.Single
import org.dhis2.commons.prefs.PreferenceProvider
import org.dhis2.commons.resources.MetadataIconProvider
import org.dhis2.commons.resources.ResourceManager
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.common.Unit
import org.hisp.dhis.android.core.common.ValueType
import org.hisp.dhis.android.core.enrollment.Enrollment
import org.hisp.dhis.android.core.enrollment.EnrollmentStatus
import org.hisp.dhis.android.core.event.Event
import org.hisp.dhis.android.core.maintenance.D2Error
import org.hisp.dhis.android.core.maintenance.D2ErrorCode
import org.hisp.dhis.android.core.maintenance.D2ErrorComponent
import org.hisp.dhis.android.core.program.ProgramStage
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttribute
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeValue
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.doReturnConsecutively
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class DashboardRepositoryImplTest {

    private lateinit var repository: DashboardRepositoryImpl
    private val d2: D2 = Mockito.mock(D2::class.java, Mockito.RETURNS_DEEP_STUBS)
    private val resources: ResourceManager = mock()
    private val charts: Charts = mock()
    private val teiAttributesProvider: TeiAttributesProvider = mock()
    private val preferences: PreferenceProvider = mock()
    private val metadataIconProvider: MetadataIconProvider = mock()

    @Before
    fun setUp() {
        repository = DashboardRepositoryImpl(
            d2,
            charts,
            "teiUid",
            "programUid",
            "enrollmentUid",
            teiAttributesProvider,
            preferences,
            metadataIconProvider,
        )
    }

    @Test
    fun `Should return program stage to show display generate event`() {
        whenever(d2.eventModule()) doReturn mock()
        whenever(d2.eventModule().events()) doReturn mock()
        whenever(d2.eventModule().events().uid("event_uid")) doReturn mock()
        whenever(d2.eventModule().events().uid("event_uid").get()) doReturn
            Single.just(getMockSingleEvent())

        whenever(d2.programModule()) doReturn mock()
        whenever(d2.programModule().programStages()) doReturn mock()
        whenever(d2.programModule().programStages().uid("program_stage")) doReturn mock()
        whenever(d2.programModule().programStages().uid("program_stage").get()) doReturn
            Single.just(getMockStage())

        val testObserver = repository.displayGenerateEvent("event_uid").test()

        testObserver.assertNoErrors()
        testObserver.assertValueCount(1)
        testObserver.assertValue(getMockStage())

        testObserver.dispose()
    }

    @Test
    fun `Should return only enrollments that are not deleted`() {
        val teiUid = "teiUid"

        val enrollment1 = getMockingEnrollment().toBuilder()
            .uid("enrollment_1").deleted(true).trackedEntityInstance(teiUid)
            .build()
        val enrollment2 = getMockingEnrollment().toBuilder()
            .deleted(false).trackedEntityInstance(teiUid)
            .build()

        val enrollments = listOf(enrollment1, enrollment2)

        whenever(d2.enrollmentModule().enrollments().byTrackedEntityInstance()) doReturn mock()
        whenever(
            d2.enrollmentModule().enrollments().byTrackedEntityInstance().eq(teiUid),
        ) doReturn mock()
        whenever(
            d2.enrollmentModule().enrollments()
                .byTrackedEntityInstance().eq(teiUid)
                .byDeleted(),
        ) doReturn mock()
        whenever(
            d2.enrollmentModule().enrollments()
                .byTrackedEntityInstance().eq(teiUid)
                .byDeleted().eq(false),
        ) doReturn mock()
        whenever(
            d2.enrollmentModule().enrollments()
                .byTrackedEntityInstance().eq(teiUid)
                .byDeleted().eq(false).get(),
        ) doReturn Single.just(enrollments)

        val testObserver = repository.getTEIEnrollments(teiUid).test()

        testObserver.assertNoErrors()
        testObserver.assertValueCount(1)
    }

    @Test
    fun `Should get enrollment status`() {
        whenever(d2.enrollmentModule().enrollments()) doReturn mock()
        whenever(d2.enrollmentModule().enrollments().uid("uid")) doReturn mock()
        whenever(d2.enrollmentModule().enrollments().uid("uid").blockingGet()) doReturn mock()
        whenever(
            d2.enrollmentModule().enrollments().uid("uid").blockingGet()?.status(),
        ) doReturn EnrollmentStatus.COMPLETED

        val status = repository.getEnrollmentStatus("uid")

        assert(status == EnrollmentStatus.COMPLETED)
    }

    @Test
    fun `Should return false if updating status of enrollment returns a D2Error`() {
        whenever(d2.programModule().programs()) doReturn mock()
        whenever(d2.programModule().programs().uid("programUid")) doReturn mock()
        whenever(d2.programModule().programs().uid("programUid").blockingGet()) doReturn mock()
        whenever(
            d2.programModule().programs().uid("programUid").blockingGet()?.access(),
        ) doReturn mock()
        whenever(
            d2.programModule().programs().uid("programUid").blockingGet()?.access()?.data(),
        ) doReturn mock()
        whenever(
            d2.programModule().programs().uid("programUid").blockingGet()?.access()?.data()?.write(),
        ) doReturn true
        whenever(d2.enrollmentModule().enrollments()) doReturn mock()
        whenever(d2.enrollmentModule().enrollments().uid("enrollmentUid")) doReturn mock()
        whenever(
            d2.enrollmentModule().enrollments()
                .uid("enrollmentUid").setStatus(EnrollmentStatus.COMPLETED),
        ) doThrow D2Error.builder().errorCode(D2ErrorCode.VALUE_CANT_BE_SET)
            .errorComponent(D2ErrorComponent.Database)
            .errorDescription("description")
            .build()

        val testObserver =
            repository.updateEnrollmentStatus("enrollmentUid", EnrollmentStatus.COMPLETED).test()

        testObserver.assertNoErrors()
        testObserver.assertValueAt(0) {
            it == StatusChangeResultCode.FAILED
        }
    }

    @Test
    fun `Should return true if enrollment status was updated correctly`() {
        whenever(d2.programModule().programs()) doReturn mock()
        whenever(d2.programModule().programs().uid("programUid")) doReturn mock()
        whenever(d2.programModule().programs().uid("programUid").blockingGet()) doReturn mock()
        whenever(
            d2.programModule().programs().uid("programUid").blockingGet()?.access(),
        ) doReturn mock()
        whenever(
            d2.programModule().programs().uid("programUid").blockingGet()?.access()?.data(),
        ) doReturn mock()
        whenever(
            d2.programModule().programs().uid("programUid").blockingGet()?.access()?.data()?.write(),
        ) doReturn true
        whenever(d2.enrollmentModule().enrollments()) doReturn mock()
        whenever(d2.enrollmentModule().enrollments().uid("enrollmentUid")) doReturn mock()
        whenever(
            d2.enrollmentModule().enrollments()
                .uid("enrollmentUid").setStatus(EnrollmentStatus.COMPLETED),
        ) doReturn Unit()

        val testObserver =
            repository.updateEnrollmentStatus("enrollmentUid", EnrollmentStatus.COMPLETED).test()

        testObserver.assertNoErrors()
        testObserver.assertValueAt(0) {
            it == StatusChangeResultCode.CHANGED
        }
    }

    @Test
    fun `Should return false if user does not hava write permission to update enrollment status`() {
        whenever(d2.programModule().programs()) doReturn mock()
        whenever(d2.programModule().programs().uid("programUid")) doReturn mock()
        whenever(d2.programModule().programs().uid("programUid").blockingGet()) doReturn mock()
        whenever(
            d2.programModule().programs().uid("programUid").blockingGet()?.access(),
        ) doReturn mock()
        whenever(
            d2.programModule().programs().uid("programUid").blockingGet()?.access()?.data(),
        ) doReturn mock()
        whenever(
            d2.programModule().programs().uid("programUid").blockingGet()?.access()?.data()?.write(),
        ) doReturn false

        val testObserver =
            repository.updateEnrollmentStatus("enrollmentUid", EnrollmentStatus.COMPLETED).test()

        testObserver.assertNoErrors()
        testObserver.assertValueAt(0) {
            it == StatusChangeResultCode.WRITE_PERMISSION_FAIL
        }
    }

    @Test
    fun `Should get program attributes if program is not null`() {
        val programUid = "programUid"
        val teiUid = "teiUid"
        val expectedResults = arrayListOf("1", "", "3")

        whenever(
            teiAttributesProvider
                .getValuesFromProgramTrackedEntityAttributesByProgram(programUid, teiUid),
        ) doReturn Single.just(
            arrayListOf(
                TrackedEntityAttributeValue.builder()
                    .trackedEntityAttribute("attr1")
                    .trackedEntityInstance(teiUid)
                    .value("1")
                    .build(),
                TrackedEntityAttributeValue.builder()
                    .trackedEntityAttribute("attr2")
                    .trackedEntityInstance(teiUid)
                    .value(null)
                    .build(),
                TrackedEntityAttributeValue.builder()
                    .trackedEntityAttribute("attr3")
                    .trackedEntityInstance(teiUid)
                    .value("3")
                    .build(),
            ),
        )

        whenever(
            d2.trackedEntityModule().trackedEntityAttributes().uid(anyString()).blockingGet(),
        ) doReturnConsecutively arrayListOf(
            TrackedEntityAttribute.builder()
                .uid("attr1")
                .valueType(ValueType.TEXT)
                .build(),
            TrackedEntityAttribute.builder()
                .uid("attr3")
                .valueType(ValueType.TEXT)
                .build(),
        )

        val testObserver = repository.getTEIAttributeValues(programUid, teiUid).test()
        testObserver
            .assertNoErrors()
            .assertValue {
                it[0].value() == expectedResults[0] &&
                    it[1].value() == expectedResults[1] &&
                    it[2].value() == expectedResults[2]
            }
    }

    @Test
    fun `Should display TrackedEntityType attributes if there is no program selected`() {
        val teType = "teType"
        val teiUid = "teiUid"
        val expectedResults = arrayListOf("attrValue1", "attrValue2", "attrValue4")

        whenever(d2.trackedEntityModule().trackedEntityInstances()) doReturn mock()
        whenever(
            d2.trackedEntityModule().trackedEntityInstances().uid(anyString()),
        ) doReturn mock()
        whenever(
            d2.trackedEntityModule().trackedEntityInstances().uid(anyString()).blockingGet(),
        ) doReturn mock()
        whenever(
            d2.trackedEntityModule().trackedEntityInstances()
                .uid(anyString()).blockingGet()?.trackedEntityType(),
        ) doReturn teType

        whenever(
            teiAttributesProvider.getValuesFromTrackedEntityTypeAttributes(teType, teiUid),
        ) doReturn arrayListOf(
            TrackedEntityAttributeValue.builder()
                .id(1)
                .value("attrValue1")
                .trackedEntityAttribute("attr1")
                .build(),
            TrackedEntityAttributeValue.builder()
                .id(2)
                .value("attrValue2")
                .trackedEntityAttribute("attr2")
                .build(),
            TrackedEntityAttributeValue.builder()
                .id(3)
                .value("attrValue4")
                .trackedEntityAttribute("attr4")
                .build(),
        )

        whenever(d2.trackedEntityModule().trackedEntityAttributes()) doReturn mock()
        whenever(
            d2.trackedEntityModule().trackedEntityAttributes().uid(anyString()),
        ) doReturn mock()
        whenever(
            d2.trackedEntityModule().trackedEntityAttributes().uid(anyString()).blockingGet(),
        ) doReturnConsecutively arrayListOf(
            TrackedEntityAttribute.builder()
                .uid("attr1")
                .valueType(ValueType.TEXT)
                .build(),
            TrackedEntityAttribute.builder()
                .uid("attr2")
                .valueType(ValueType.TEXT)
                .build(),
            TrackedEntityAttribute.builder()
                .uid("attr4")
                .valueType(ValueType.TEXT)
                .build(),
        )

        val testObserver = repository.getTEIAttributeValues(null, "teiUid").test()
        testObserver
            .assertNoErrors()
            .assertValue {
                it[0].value() == expectedResults[0] &&
                    it[1].value() == expectedResults[1] &&
                    it[2].value() == expectedResults[2]
            }
    }

    @Test
    fun `Should display program attributes when tracked entity type has no attributes`() {
        val teType = "teType"
        val teiUid = "teiUid"
        val expectedResults = arrayListOf("attrValue1", "attrValue2", "attrValue3")

        whenever(d2.trackedEntityModule().trackedEntityInstances()) doReturn mock()
        whenever(
            d2.trackedEntityModule().trackedEntityInstances().uid(anyString()),
        ) doReturn mock()
        whenever(
            d2.trackedEntityModule().trackedEntityInstances().uid(anyString()).blockingGet(),
        ) doReturn mock()
        whenever(
            d2.trackedEntityModule().trackedEntityInstances()
                .uid(anyString()).blockingGet()?.trackedEntityType(),
        ) doReturn teType
        whenever(
            teiAttributesProvider.getValuesFromTrackedEntityTypeAttributes(teType, teiUid),
        ) doReturn emptyList()
        whenever(
            teiAttributesProvider.getValuesFromProgramTrackedEntityAttributes(teType, teiUid),
        ) doReturn arrayListOf(
            TrackedEntityAttributeValue.builder()
                .id(1)
                .value("attrValue1")
                .trackedEntityAttribute("attr1")
                .build(),
            TrackedEntityAttributeValue.builder()
                .id(2)
                .value("attrValue2")
                .trackedEntityAttribute("attr2")
                .build(),
            TrackedEntityAttributeValue.builder()
                .id(3)
                .value("attrValue3")
                .trackedEntityAttribute("attr3")
                .build(),
        )

        whenever(d2.trackedEntityModule().trackedEntityAttributes()) doReturn mock()
        whenever(
            d2.trackedEntityModule().trackedEntityAttributes().uid(anyString()),
        ) doReturn mock()
        whenever(
            d2.trackedEntityModule().trackedEntityAttributes().uid(anyString()).blockingGet(),
        ) doReturnConsecutively arrayListOf(
            TrackedEntityAttribute.builder()
                .uid("attr1")
                .valueType(ValueType.TEXT)
                .build(),
            TrackedEntityAttribute.builder()
                .uid("attr2")
                .valueType(ValueType.TEXT)
                .build(),
            TrackedEntityAttribute.builder()
                .uid("attr3")
                .valueType(ValueType.TEXT)
                .build(),
        )

        val testObserver = repository.getTEIAttributeValues(null, "teiUid").test()
        testObserver
            .assertNoErrors()
            .assertValue {
                it[0].value() == expectedResults[0] &&
                    it[1].value() == expectedResults[1] &&
                    it[2].value() == expectedResults[2]
            }
    }

    private fun getMockingEnrollment(): Enrollment {
        return Enrollment.builder()
            .uid("enrollmentUid")
            .build()
    }

    private fun getMockSingleEvent(): Event {
        return Event.builder()
            .uid("event_uid")
            .programStage("program_stage")
            .program("program")
            .build()
    }

    private fun getMockStage(): ProgramStage {
        return ProgramStage.builder()
            .uid("program_stage")
            .build()
    }
}
