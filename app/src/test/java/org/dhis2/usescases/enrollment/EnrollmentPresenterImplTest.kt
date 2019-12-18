package org.dhis2.usescases.enrollment

import com.nhaarman.mockitokotlin2.*
import org.dhis2.data.forms.dataentry.DataEntryRepository
import org.dhis2.data.schedulers.SchedulerProvider
import org.dhis2.data.schedulers.TrampolineSchedulerProvider
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.arch.repositories.`object`.ReadOnlyOneObjectRepositoryFinalImpl
import org.hisp.dhis.android.core.arch.repositories.scope.RepositoryScope
import org.hisp.dhis.android.core.category.CategoryCombo
import org.hisp.dhis.android.core.common.*
import org.hisp.dhis.android.core.datavalue.DataValue
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
import org.hisp.dhis.smscompression.SMSConsts
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import java.io.File
import java.util.*

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
            formRepository
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
        whenever(d2.programModule().programStages().uid("").blockingGet()) doReturn ProgramStage
                .builder().uid("").featureType(featureType).build()

        whenever(d2.categoryModule()) doReturn mock()
        whenever(d2.categoryModule().categoryCombos()) doReturn mock()
        whenever(d2.categoryModule().categoryCombos().uid("")) doReturn mock()
        whenever(d2.categoryModule().categoryCombos().uid("").blockingGet()) doReturn CategoryCombo.builder()
                .isDefault(catCombo)
                .uid("")
                .build()
    }

    @Test
    fun `Check updateEnrollmentStatus where write access is granted`() {
        whenever(programRepository.blockingGet()) doReturn Program.builder().uid("")
                .access(Access.builder()
                        .data(DataAccess.builder().write(true)
                                .build()).build()).build()
        presenter.updateEnrollmentStatus(EnrollmentStatus.ACTIVE)
        verify(enrollmentRepository).setStatus(EnrollmentStatus.ACTIVE)
        verify(enrollmentView).renderStatus(EnrollmentStatus.ACTIVE)
    }

    @Test
    fun `Check updateEnrollmentStatus where write access is denied`() {
        whenever(programRepository.blockingGet()) doReturn Program.builder().uid("")
                .access(Access.builder()
                        .data(DataAccess.builder().write(false)
                                .build()).build()).build()
        presenter.updateEnrollmentStatus(EnrollmentStatus.ACTIVE)

        verify(enrollmentView).displayMessage(null)
    }

    @Test
    fun `Save value when value is an attribute`() {
        val newValue = "newValue"
        val stringValue = "notEmpty"
        mockValuesAttribute(newValue)
        val value = presenter.saveValue("", stringValue)
        verify(d2.trackedEntityModule().trackedEntityAttributeValues().value("", ""), times(1)).blockingSet(newValue)
        verify(d2.trackedEntityModule().trackedEntityAttributeValues().value("", ""), times(0)).blockingDelete()
        assert(value)
    }

    @Test
    fun `Save value when value is empty and is an attribute`() {
        val newValue = "newValue"
        val stringValue = String()
        mockValuesAttribute(newValue)
        val value = presenter.saveValue("", stringValue)
        verify(d2.trackedEntityModule().trackedEntityAttributeValues().value("", ""), times(0)).blockingSet(newValue)
        verify(d2.trackedEntityModule().trackedEntityAttributeValues().value("", ""), times(1)).blockingDelete()
        assert(value)
    }

    @Test
    fun `Save value when value is same as new value and is an attribute`() {
        val newValue = "newValue"
        val stringValue = "newValue"
        mockValuesAttribute(newValue)
        val value = presenter.saveValue("", stringValue)
        verify(d2.trackedEntityModule().trackedEntityAttributeValues().value("", ""), times(0)).blockingSet(newValue)
        verify(d2.trackedEntityModule().trackedEntityAttributeValues().value("", ""), times(0)).blockingDelete()
        assert(!value)
    }

    @Test
    fun `Save value when value is data value, dont exist the value, the event value is null, have the same value, and is image type`() {
        // assert(mockValuesDataElement(existValue = false, eventValueIsNull = true, isSameValue = true, valueTypeIsImage = true) == true)
        val uid = ""
        val value: String? = "otherValue"
        mockValuesDataElement(
                uid = "",
                value = "",
                existValue = false,
                eventValueIsNull = true,
                isSameValue = true,
                valueTypeIsImage = true)
        val retValue = presenter.saveValue(uid, value)
        verify(d2.trackedEntityModule().trackedEntityDataValues().value("eventName", uid), times(0)).blockingSet(value)
        verify(d2.trackedEntityModule().trackedEntityDataValues().value("eventName", uid), times(0)).blockingDelete()
        assert(!retValue)
    }

    @Test
    fun `Save value when value is data value, exist the value, the event value is null, have the same value, and is image type`() {
        // assert(mockValuesDataElement(existValue = true, eventValueIsNull = true, isSameValue = true, valueTypeIsImage = true) == true)
        val uid = ""
        val value: String? = "otherValue"
        mockValuesDataElement(
                uid = "",
                value = "",
                existValue = true,
                eventValueIsNull = true,
                isSameValue = true,
                valueTypeIsImage = true)
        val retValue = presenter.saveValue(uid, value)
        verify(d2.trackedEntityModule().trackedEntityDataValues().value("eventName", uid), times(0)).blockingSet(value)
        verify(d2.trackedEntityModule().trackedEntityDataValues().value("eventName", uid), times(0)).blockingDelete()
        assert(!retValue)
    }

    @Test
    fun `Save value when value is data value,dont exist the value, the event value is not null, have the same value, and is image type`() {
        // assert(mockValuesDataElement(existValue = false, eventValueIsNull = false, isSameValue = true, valueTypeIsImage = true) == true)
        val uid = ""
        val value: String? = "otherValue"
        mockValuesDataElement(
                uid = "",
                value = "",
                existValue = false,
                eventValueIsNull = false,
                isSameValue = true,
                valueTypeIsImage = true)
        val retValue = presenter.saveValue(uid, value)
        verify(d2.trackedEntityModule().trackedEntityDataValues().value("eventName", uid), times(0)).blockingSet(value)
        verify(d2.trackedEntityModule().trackedEntityDataValues().value("eventName", uid), times(0)).blockingDelete()
        assert(retValue)
    }

    @Test
    fun `Save value when value is data value, exist the value, the event value is not null, have the same value, and is image type`() {
        // assert(mockValuesDataElement(existValue = true, eventValueIsNull = false, isSameValue = true, valueTypeIsImage = true) == true)
        val uid = ""
        val value: String? = "otherValue"
        mockValuesDataElement(
                uid = "",
                value = "",
                existValue = true,
                eventValueIsNull = false,
                isSameValue = true,
                valueTypeIsImage = true)
        val retValue = presenter.saveValue(uid, value)
        verify(d2.trackedEntityModule().trackedEntityDataValues().value("eventName", uid), times(0)).blockingSet(value)
        verify(d2.trackedEntityModule().trackedEntityDataValues().value("eventName", uid), times(0)).blockingDelete()
        assert(!retValue)
    }

    @Test
    fun `Save value when value is data value, dont exist the value, the event value is null, havent the same value, and is image type`() {
        // assert(mockValuesDataElement(existValue = false, eventValueIsNull = true, isSameValue = false, valueTypeIsImage = true) == true)
        val uid = ""
        val value: String? = "otherValue"
        mockValuesDataElement(
                uid = "",
                value = "",
                existValue = false,
                eventValueIsNull = true,
                isSameValue = false,
                valueTypeIsImage = true)
        val retValue = presenter.saveValue(uid, value)
        verify(d2.trackedEntityModule().trackedEntityDataValues().value("eventName", uid), times(0)).blockingSet(value)
        verify(d2.trackedEntityModule().trackedEntityDataValues().value("eventName", uid), times(0)).blockingDelete()
        assert(!retValue)
    }

    @Test
    fun `Save value when value is data value, exist the value, the event value is null, havent the same value, and is image type`() {
        // assert(mockValuesDataElement(existValue = true, eventValueIsNull = true, isSameValue = false, valueTypeIsImage = true) == true)
        val uid = ""
        val value: String? = "otherValue"
        mockValuesDataElement(
                uid = "",
                value = "",
                existValue = true,
                eventValueIsNull = true,
                isSameValue = false,
                valueTypeIsImage = true)
        val retValue = presenter.saveValue(uid, value)
        verify(d2.trackedEntityModule().trackedEntityDataValues().value("eventName", uid), times(0)).blockingSet(value)
        verify(d2.trackedEntityModule().trackedEntityDataValues().value("eventName", uid), times(0)).blockingDelete()
        assert(!retValue)
    }

    @Test
    fun `Save value when value is data value,dont exist the value, the event value is not null, havent the same value, and is image type`() {
        // assert(mockValuesDataElement(existValue = false, eventValueIsNull = false, isSameValue = false, valueTypeIsImage = true) == true)
        val uid = ""
        val value: String? = "otherValue"
        mockValuesDataElement(
                uid = "",
                value = "",
                existValue = false,
                eventValueIsNull = false,
                isSameValue = false,
                valueTypeIsImage = true)
        val retValue = presenter.saveValue(uid, value)
        verify(d2.trackedEntityModule().trackedEntityDataValues().value("eventName", uid), times(0)).blockingSet(value)
        verify(d2.trackedEntityModule().trackedEntityDataValues().value("eventName", uid), times(0)).blockingDelete()
        assert(retValue)
    }

    @Test
    fun `Save value when value is data value, exist the value, the event value is not null, havent the same value, and is image type`() {
        // assert(mockValuesDataElement(existValue = true, eventValueIsNull = false, isSameValue = false, valueTypeIsImage = true) == true)
        val uid = ""
        val value: String? = "otherValue"
        mockValuesDataElement(
                uid = "",
                value = "",
                existValue = true,
                eventValueIsNull = false,
                isSameValue = false,
                valueTypeIsImage = true)
        val retValue = presenter.saveValue(uid, value)
        verify(d2.trackedEntityModule().trackedEntityDataValues().value("eventName", uid), times(0)).blockingSet(value)
        verify(d2.trackedEntityModule().trackedEntityDataValues().value("eventName", uid), times(0)).blockingDelete()
        assert(!retValue)
    }

    @Test
    fun `Save value when value is data value, dont exist the value, the event value is null, have the same value, and is not image type`() {
        // assert(mockValuesDataElement(existValue = false, eventValueIsNull = true, isSameValue = true, valueTypeIsImage = false) == true)
        val uid = ""
        val value: String? = "otherValue"
        mockValuesDataElement(
                uid = "",
                value = "",
                existValue = false,
                eventValueIsNull = true,
                isSameValue = true,
                valueTypeIsImage = false)
        val retValue = presenter.saveValue(uid, value)
        verify(d2.trackedEntityModule().trackedEntityDataValues().value("eventName", uid), times(0)).blockingSet(value)
        verify(d2.trackedEntityModule().trackedEntityDataValues().value("eventName", uid), times(0)).blockingDelete()
        assert(!retValue)
    }

    @Test
    fun `Save value when value is data value, exist the value, the event value is null, have the same value, and is not image type`() {
        // assert(mockValuesDataElement(existValue = true, eventValueIsNull = true, isSameValue = true, valueTypeIsImage = false) == true)
        val uid = ""
        val value: String? = "otherValue"
        mockValuesDataElement(
                uid = "",
                value = "",
                existValue = true,
                eventValueIsNull = true,
                isSameValue = true,
                valueTypeIsImage = false)
        val retValue = presenter.saveValue(uid, value)
        verify(d2.trackedEntityModule().trackedEntityDataValues().value("eventName", uid), times(0)).blockingSet(value)
        verify(d2.trackedEntityModule().trackedEntityDataValues().value("eventName", uid), times(0)).blockingDelete()
        assert(!retValue)
    }

    @Test
    fun `Save value when value is data value,dont exist the value, the event value is not null, have the same value, and is not image type`() {
        // assert(mockValuesDataElement(existValue = false, eventValueIsNull = false, isSameValue = true, valueTypeIsImage = false) == true)
        val uid = ""
        val value: String? = "otherValue"
        mockValuesDataElement(
                uid = "",
                value = "",
                existValue = false,
                eventValueIsNull = false,
                isSameValue = true,
                valueTypeIsImage = false)
        val retValue = presenter.saveValue(uid, value)
        verify(d2.trackedEntityModule().trackedEntityDataValues().value("eventName", uid), times(1)).blockingSet(value)
        verify(d2.trackedEntityModule().trackedEntityDataValues().value("eventName", uid), times(0)).blockingDelete()
        assert(retValue)
    }

    @Test
    fun `Save value when value is data value, exist the value, the event value is not null, have the same value, and is not image type`() {
        // assert(mockValuesDataElement(existValue = true, eventValueIsNull = false, isSameValue = true, valueTypeIsImage = false) == true)
        val uid = ""
        val value: String? = "otherValue"
        mockValuesDataElement(
                uid = "",
                value = "",
                existValue = true,
                eventValueIsNull = false,
                isSameValue = true,
                valueTypeIsImage = false)
        val retValue = presenter.saveValue(uid, value)
        verify(d2.trackedEntityModule().trackedEntityDataValues().value("eventName", uid), times(1)).blockingSet(value)
        verify(d2.trackedEntityModule().trackedEntityDataValues().value("eventName", uid), times(0)).blockingDelete()
        assert(retValue)
    }

    @Test
    fun `Save value when value is data value, dont exist the value, the event value is null, havent the same value, and is not image type`() {
        // assert(mockValuesDataElement(existValue = false, eventValueIsNull = true, isSameValue = false, valueTypeIsImage = false) == true)
        val uid = ""
        val value: String? = "otherValue"
        mockValuesDataElement(
                uid = "",
                value = "",
                existValue = false,
                eventValueIsNull = true,
                isSameValue = false,
                valueTypeIsImage = false)
        val retValue = presenter.saveValue(uid, value)
        verify(d2.trackedEntityModule().trackedEntityDataValues().value("eventName", uid), times(0)).blockingSet(value)
        verify(d2.trackedEntityModule().trackedEntityDataValues().value("eventName", uid), times(0)).blockingDelete()
        assert(!retValue)
    }

    @Test
    fun `Save value when value is data value, exist the value, the event value is null, havent the same value, and is not image type`() {
        // assert(mockValuesDataElement(existValue = true, eventValueIsNull = true, isSameValue = false, valueTypeIsImage = false) == true)
        val uid = ""
        val value: String? = "otherValue"
        mockValuesDataElement(
                uid = "",
                value = "",
                existValue = true,
                eventValueIsNull = true,
                isSameValue = false,
                valueTypeIsImage = false)
        val retValue = presenter.saveValue(uid, value)
        verify(d2.trackedEntityModule().trackedEntityDataValues().value("eventName", uid), times(0)).blockingSet(value)
        verify(d2.trackedEntityModule().trackedEntityDataValues().value("eventName", uid), times(0)).blockingDelete()
        assert(!retValue)
    }

    @Test
    fun `Save value when value is data value,dont exist the value, the event value is not null, havent the same value, and is not image type`() {
        // assert(mockValuesDataElement(existValue = false, eventValueIsNull = false, isSameValue = false, valueTypeIsImage = false) == true)
        val uid = ""
        val value: String? = "otherValue"
        mockValuesDataElement(
                uid = "",
                value = "",
                existValue = false,
                eventValueIsNull = false,
                isSameValue = false,
                valueTypeIsImage = false)
        val retValue = presenter.saveValue(uid, value)
        verify(d2.trackedEntityModule().trackedEntityDataValues().value("eventName", uid)).blockingSet(value)
        verify(d2.trackedEntityModule().trackedEntityDataValues().value("eventName", uid), times(0)).blockingDelete()
        assert(retValue)
    }

    @Test
    fun `Save value when value is data value, exist the value, the event value is not null, havent the same value, and is not image type`() {
        val uid = ""
        val value: String? = "otherValue"
        mockValuesDataElement(
                uid = "",
                value = "",
                existValue = true,
                eventValueIsNull = false,
                isSameValue = false,
                valueTypeIsImage = false)
        val retValue = presenter.saveValue(uid, value)
        verify(d2.trackedEntityModule().trackedEntityDataValues().value("eventName", uid)).blockingSet(value)
        verify(d2.trackedEntityModule().trackedEntityDataValues().value("eventName", uid), times(0)).blockingDelete()
        assert(retValue)
    }



    private fun mockValuesDataElement(
            uid: String,
            value: String,
            existValue: Boolean,
            eventValueIsNull: Boolean,
            isSameValue: Boolean,
            valueTypeIsImage: Boolean) {

        val sameValue = if (isSameValue) {
            value
        } else {
            "other_value"
        }
        whenever(d2.trackedEntityModule().trackedEntityAttributes().uid("")) doReturn mock()
        whenever(d2.trackedEntityModule().trackedEntityAttributes().uid("").blockingExists()) doReturn false

        whenever(enrollmentRepository.blockingGet()) doReturn  Enrollment.builder().uid("").build()

        whenever(d2.eventModule().events().byEnrollmentUid().eq("")) doReturn mock()
        whenever(d2.eventModule().events().byEnrollmentUid().eq("").byStatus()) doReturn mock()
        whenever(d2.eventModule().events().byEnrollmentUid().eq("").byStatus().eq(EventStatus.ACTIVE)) doReturn mock()
        whenever(d2.eventModule().events().byEnrollmentUid().eq("").byStatus().eq(EventStatus.ACTIVE).orderByEventDate(RepositoryScope.OrderByDirection.DESC)) doReturn mock()
        whenever(d2.eventModule().events().byEnrollmentUid().eq("").byStatus()
                .eq(EventStatus.ACTIVE).orderByEventDate(RepositoryScope.OrderByDirection.DESC)
                .blockingGet()) doReturn listOf(
                Event.builder().uid("id").build()
        )


        whenever(d2.trackedEntityModule().trackedEntityDataValues().byDataElement().eq(uid)) doReturn mock()
        whenever(d2.trackedEntityModule().trackedEntityDataValues().byDataElement().eq(uid).byEvent()) doReturn  mock()
        val retList = listOf("id")
        whenever(d2.trackedEntityModule().trackedEntityDataValues().byDataElement().eq(uid).byEvent().`in`(retList)) doReturn  mock()
        if (eventValueIsNull) {
            whenever(d2.trackedEntityModule().trackedEntityDataValues().byDataElement().eq(uid).byEvent().`in`(retList).blockingGet()) doReturn listOf()
        } else {
            whenever(d2.trackedEntityModule().trackedEntityDataValues().byDataElement().eq(uid).byEvent().`in`(retList).blockingGet()) doReturn listOf(
                    TrackedEntityDataValue.builder()
                            .event("eventName")
                            .build()
            )
        }

        whenever(d2.trackedEntityModule().trackedEntityDataValues().value("eventName", uid)) doReturn mock()
        whenever(d2.trackedEntityModule().trackedEntityDataValues().value("eventName", uid).blockingExists()) doReturn existValue
        whenever(d2.trackedEntityModule().trackedEntityDataValues().value("eventName", uid).blockingGet()) doReturn TrackedEntityDataValue.builder()
                .value(sameValue)
                .build()

        whenever(d2.dataElementModule().dataElements().uid(uid)) doReturn mock()
        whenever(d2.dataElementModule().dataElements().uid(uid).blockingGet()) doReturn mock()
        if (valueTypeIsImage) {
            whenever(d2.dataElementModule().dataElements().uid(uid).blockingGet().valueType()) doReturn ValueType.IMAGE
        } else {
            whenever(d2.dataElementModule().dataElements().uid(uid).blockingGet().valueType()) doReturn ValueType.BOOLEAN
        }

        whenever(d2.fileResourceModule().fileResources().blockingAdd(any())) doReturn sameValue
    }

    private fun mockValuesAttribute(newValue: String) {
        whenever(d2.trackedEntityModule().trackedEntityAttributes().uid("")) doReturn mock()
        whenever(d2.trackedEntityModule().trackedEntityAttributes().uid("").blockingExists()) doReturn true

        whenever(teiRepository.blockingGet()) doReturn mock()
        whenever(teiRepository.blockingGet().uid()) doReturn ""

        whenever(d2.trackedEntityModule().trackedEntityAttributeValues().value("", "")) doReturn mock()

        whenever(d2.trackedEntityModule().trackedEntityAttributes().uid("").blockingGet()) doReturn TrackedEntityAttribute.builder()
                .valueType(ValueType.IMAGE)
                .uid("")
                .build()

        whenever(d2.fileResourceModule().fileResources().blockingAdd(any())) doReturn newValue

        whenever(d2.trackedEntityModule().trackedEntityAttributeValues().value("", "").blockingExists()) doReturn true
        whenever(d2.trackedEntityModule().trackedEntityAttributeValues().value("", "").blockingGet()) doReturn TrackedEntityAttributeValue.builder()
                .value("value").build()

    }
}
