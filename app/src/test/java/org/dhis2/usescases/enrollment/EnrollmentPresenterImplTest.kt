package org.dhis2.usescases.enrollment

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyZeroInteractions
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Flowable
import io.reactivex.processors.PublishProcessor
import org.dhis2.data.forms.dataentry.DataEntryRepository
import org.dhis2.data.forms.dataentry.StoreResult
import org.dhis2.data.forms.dataentry.ValueStore
import org.dhis2.data.forms.dataentry.ValueStoreImpl
import org.dhis2.data.schedulers.SchedulerProvider
import org.dhis2.data.schedulers.TrampolineSchedulerProvider
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.arch.repositories.`object`.ReadOnlyOneObjectRepositoryFinalImpl
import org.hisp.dhis.android.core.arch.repositories.scope.RepositoryScope
import org.hisp.dhis.android.core.category.CategoryCombo
import org.hisp.dhis.android.core.common.Access
import org.hisp.dhis.android.core.common.DataAccess
import org.hisp.dhis.android.core.common.FeatureType
import org.hisp.dhis.android.core.common.ObjectWithUid
import org.hisp.dhis.android.core.common.ValueType
import org.hisp.dhis.android.core.enrollment.Enrollment
import org.hisp.dhis.android.core.enrollment.EnrollmentObjectRepository
import org.hisp.dhis.android.core.enrollment.EnrollmentStatus
import org.hisp.dhis.android.core.event.Event
import org.hisp.dhis.android.core.event.EventStatus
import org.hisp.dhis.android.core.program.Program
import org.hisp.dhis.android.core.program.ProgramStage
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttribute
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeValue
import org.hisp.dhis.android.core.trackedentity.TrackedEntityDataValue
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstanceObjectRepository
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito

class EnrollmentPresenterImplTest {

    private val formRepository: EnrollmentFormRepository = mock()
    private val programRepository: ReadOnlyOneObjectRepositoryFinalImpl<Program> = mock()
    private val teiRepository: TrackedEntityInstanceObjectRepository = mock()
    private val dataEntryRepository: DataEntryRepository = mock()
    lateinit var presenter: EnrollmentPresenterImpl
    private val enrollmentView: EnrollmentView = mock()
    private val d2: D2 = Mockito.mock(D2::class.java, Mockito.RETURNS_DEEP_STUBS)
    private val enrollmentRepository: EnrollmentObjectRepository = mock()
    private val schedulers: SchedulerProvider = TrampolineSchedulerProvider()
    private val valueStore: ValueStore = mock()

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
            formRepository,
            valueStore
        )
    }

    @Test
    fun `Missing and errors fields should show mandatory fields dialog`() {
        val checkWthErrors = presenter.dataIntegrityCheck(mandatoryOk = false, hasError = true)

        Assert.assertFalse(checkWthErrors)

        verify(enrollmentView, times(1)).showMissingMandatoryFieldsMessage()
    }

    @Test
    fun `Missing fields should show mandatory fields dialog`() {
        val checkWthErrors = presenter.dataIntegrityCheck(mandatoryOk = false, hasError = false)

        Assert.assertFalse(checkWthErrors)

        verify(enrollmentView, times(1)).showMissingMandatoryFieldsMessage()
    }

    @Test
    fun `Error fields should show mandatory fields dialog`() {
        val checkWthErrors = presenter.dataIntegrityCheck(mandatoryOk = true, hasError = true)

        Assert.assertFalse(checkWthErrors)

        verify(enrollmentView, times(1)).showErrorFieldsMessage()
    }

    @Test
    fun `Open initial when needsCatCombo is false and needsCoordinates is false`() {
        checkCatCombo(true, FeatureType.NONE)
        assert(!presenter.openInitial(""))
    }

    @Test
    fun `Open initial when needsCatCombo is true and needsCoordinates is false`() {
        checkCatCombo(false, FeatureType.NONE)
        assert(presenter.openInitial(""))
    }

    @Test
    fun `Open initial when needsCatCombo is false and needsCoordinates is true`() {
        checkCatCombo(true, FeatureType.POINT)
        assert(presenter.openInitial(""))
    }

    @Test
    fun `Open initial when needsCatCombo is true and needsCoordinates is true`() {
        checkCatCombo(false, FeatureType.POINT)
        assert(presenter.openInitial(""))
    }

    private fun checkCatCombo(catCombo: Boolean, featureType: FeatureType) {
        whenever(programRepository.blockingGet()) doReturn Program.builder().uid("")
            .categoryCombo(ObjectWithUid.create("")).build()

        whenever(d2.eventModule()) doReturn mock()
        whenever(d2.eventModule().events()) doReturn mock()
        whenever(d2.eventModule().events().uid("")) doReturn mock()
        whenever(d2.eventModule().events().uid("").blockingGet()) doReturn Event.builder()
            .uid("").programStage("").build()

        whenever(d2.programModule()) doReturn mock()
        whenever(d2.programModule().programStages()) doReturn mock()
        whenever(d2.programModule().programStages().uid("")) doReturn mock()
        whenever(
            d2.programModule().programStages().uid("").blockingGet()
        ) doReturn ProgramStage.builder().uid("").featureType(featureType).build()

        whenever(d2.categoryModule()) doReturn mock()
        whenever(d2.categoryModule().categoryCombos()) doReturn mock()
        whenever(d2.categoryModule().categoryCombos().uid("")) doReturn mock()
        whenever(
            d2.categoryModule().categoryCombos().uid("").blockingGet()
        ) doReturn CategoryCombo.builder()
            .isDefault(catCombo)
            .uid("")
            .build()
    }

    @Test
    fun `Check updateEnrollmentStatus where write access is granted`() {
        whenever(programRepository.blockingGet()) doReturn Program.builder().uid("")
            .access(
                Access.builder()
                    .data(
                        DataAccess.builder().write(true)
                            .build()
                    ).build()
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
                            .build()
                    ).build()
            ).build()
        presenter.updateEnrollmentStatus(EnrollmentStatus.ACTIVE)

        verify(enrollmentView).displayMessage(null)
    }

    @Test
    fun `Save file should use valueStore`() {
        whenever(valueStore.save("uid", "fileValue")) doReturn Flowable.just(
            StoreResult(
                "uid",
                ValueStoreImpl.ValueStoreResult.VALUE_CHANGED
            )
        )
        presenter.saveFile("uid", "fileValue")
        verify(valueStore, times(1)).save("uid", "fileValue")
    }

    private fun mockValuesDataElement(
        uid: String,
        value: String?,
        existValue: Boolean,
        eventValueIsNull: Boolean,
        isSameValue: Boolean,
        valueTypeIsImage: Boolean
    ) {
        val sameValue = if (isSameValue) {
            value
        } else {
            "other_value"
        }
        whenever(d2.trackedEntityModule().trackedEntityAttributes().uid("")) doReturn mock()
        whenever(
            d2.trackedEntityModule().trackedEntityAttributes().uid("").blockingExists()
        ) doReturn false

        whenever(
            enrollmentRepository.blockingGet()
        ) doReturn Enrollment.builder().uid("").build()

        whenever(d2.eventModule().events().byEnrollmentUid().eq("")) doReturn mock()
        whenever(
            d2.eventModule().events().byEnrollmentUid().eq("").byStatus()
        ) doReturn mock()
        whenever(
            d2.eventModule().events()
                .byEnrollmentUid().eq("")
                .byStatus().eq(EventStatus.ACTIVE)
        ) doReturn mock()
        whenever(
            d2.eventModule().events()
                .byEnrollmentUid().eq("")
                .byStatus().eq(EventStatus.ACTIVE).orderByEventDate(
                RepositoryScope.OrderByDirection.DESC
            )
        ) doReturn mock()
        whenever(
            d2.eventModule().events().byEnrollmentUid().eq("").byStatus()
                .eq(EventStatus.ACTIVE).orderByEventDate(RepositoryScope.OrderByDirection.DESC)
                .blockingGet()
        ) doReturn listOf(
            Event.builder().uid("id").build()
        )

        whenever(
            d2.trackedEntityModule().trackedEntityDataValues().byDataElement().eq(uid)
        ) doReturn mock()
        whenever(
            d2.trackedEntityModule().trackedEntityDataValues().byDataElement().eq(uid).byEvent()
        ) doReturn mock()

        val retList = listOf("id")

        whenever(
            d2.trackedEntityModule().trackedEntityDataValues()
                .byDataElement().eq(uid)
                .byEvent().`in`(retList)
        ) doReturn mock()
        if (eventValueIsNull) {
            whenever(
                d2.trackedEntityModule().trackedEntityDataValues()
                    .byDataElement().eq(uid)
                    .byEvent().`in`(retList).blockingGet()
            ) doReturn listOf()
        } else {
            whenever(
                d2.trackedEntityModule().trackedEntityDataValues()
                    .byDataElement().eq(uid)
                    .byEvent().`in`(retList).blockingGet()
            ) doReturn listOf(
                TrackedEntityDataValue.builder()
                    .event("eventName")
                    .build()
            )
        }

        whenever(
            d2.trackedEntityModule().trackedEntityDataValues().value(
                "eventName",
                uid
            )
        ) doReturn mock()
        whenever(
            d2.trackedEntityModule().trackedEntityDataValues().value(
                "eventName",
                uid
            ).blockingExists()
        ) doReturn existValue
        whenever(
            d2.trackedEntityModule().trackedEntityDataValues().value(
                "eventName",
                uid
            ).blockingGet()
        ) doReturn TrackedEntityDataValue.builder()
            .value(sameValue)
            .build()

        whenever(d2.dataElementModule().dataElements().uid(uid)) doReturn mock()
        whenever(d2.dataElementModule().dataElements().uid(uid).blockingGet()) doReturn mock()
        if (valueTypeIsImage) {
            whenever(
                d2.dataElementModule().dataElements().uid(uid).blockingGet().valueType()
            ) doReturn ValueType.IMAGE
        } else {
            whenever(
                d2.dataElementModule().dataElements().uid(uid).blockingGet().valueType()
            ) doReturn ValueType.BOOLEAN
        }

        whenever(d2.fileResourceModule().fileResources().blockingAdd(any())) doReturn sameValue
    }

    private fun mockValuesAttribute(newValue: String) {
        whenever(d2.trackedEntityModule().trackedEntityAttributes().uid("")) doReturn mock()
        whenever(
            d2.trackedEntityModule().trackedEntityAttributes().uid("").blockingExists()
        ) doReturn true

        whenever(teiRepository.blockingGet()) doReturn mock()
        whenever(teiRepository.blockingGet().uid()) doReturn ""

        whenever(
            d2.trackedEntityModule().trackedEntityAttributeValues().value(
                "",
                ""
            )
        ) doReturn mock()

        whenever(
            d2.trackedEntityModule().trackedEntityAttributes().uid("").blockingGet()
        ) doReturn TrackedEntityAttribute.builder()
            .valueType(ValueType.IMAGE)
            .uid("")
            .build()

        whenever(d2.fileResourceModule().fileResources().blockingAdd(any())) doReturn newValue

        whenever(
            d2.trackedEntityModule().trackedEntityAttributeValues().value(
                "",
                ""
            ).blockingExists()
        ) doReturn true
        whenever(
            d2.trackedEntityModule().trackedEntityAttributeValues().value(
                "",
                ""
            ).blockingGet()
        ) doReturn TrackedEntityAttributeValue.builder()
            .value("value").build()
    }

    @Test
    fun `Check data integrity when mandatory is false and has error is false`() {
        val result = presenter.dataIntegrityCheck(false, false)
        verify(enrollmentView, times(1)).showMissingMandatoryFieldsMessage()
        verify(enrollmentView, times(0)).showErrorFieldsMessage()
        Assert.assertFalse(result)
    }

    @Test
    fun `Check data integrity when mandatory is true and has error is false`() {
        val result = presenter.dataIntegrityCheck(true, false)
        verifyZeroInteractions(enrollmentView)
        Assert.assertTrue(result)
    }

    @Test
    fun `Check data integrity when mandatory is false and has error is true`() {
        val result = presenter.dataIntegrityCheck(false, true)
        verify(enrollmentView, times(1)).showMissingMandatoryFieldsMessage()
        verify(enrollmentView, times(0)).showErrorFieldsMessage()
        Assert.assertFalse(result)
    }

    @Test
    fun `Check data integrity when mandatory is true and has error is true`() {
        val result = presenter.dataIntegrityCheck(true, true)
        verify(enrollmentView, times(0)).showMissingMandatoryFieldsMessage()
        verify(enrollmentView, times(1)).showErrorFieldsMessage()
        Assert.assertFalse(result)
    }

    @Test
    fun `Should update the fields flowable`() {
        val processor = PublishProcessor.create<Boolean>()
        val testSubscriber = processor.test()

        presenter.updateFields()
        processor.onNext(true)

        testSubscriber.assertValueAt(0, true)

    }
}
