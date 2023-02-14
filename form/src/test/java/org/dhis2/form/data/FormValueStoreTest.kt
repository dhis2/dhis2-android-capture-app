package org.dhis2.form.data

import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import org.dhis2.commons.data.EntryMode
import org.dhis2.commons.network.NetworkUtils
import org.dhis2.commons.reporting.CrashReportController
import org.dhis2.commons.resources.ResourceManager
import org.dhis2.form.model.ValueStoreResult
import org.dhis2.form.model.ValueStoreResult.VALUE_CHANGED
import org.dhis2.form.model.ValueStoreResult.VALUE_NOT_UNIQUE
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.common.ValueType
import org.hisp.dhis.android.core.dataelement.DataElement
import org.hisp.dhis.android.core.option.Option
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttribute
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeValue
import org.hisp.dhis.android.core.trackedentity.TrackedEntityDataValue
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito

class FormValueStoreTest {
    private lateinit var attrValueStore: FormValueStore
    private lateinit var deValueStore: FormValueStore
    private lateinit var dvValueStore: FormValueStore
    private val d2: D2 = Mockito.mock(D2::class.java, Mockito.RETURNS_DEEP_STUBS)
    private val crashReportController: CrashReportController = mock()
    private val networkUtils: NetworkUtils = mock()
    private val resourceManager: ResourceManager = mock()

    @Before
    fun setUp() {
        attrValueStore =
            FormValueStore(
                d2,
                "recordUid",
                EntryMode.ATTR,
                null,
                crashReportController,
                networkUtils,
                resourceManager
            )
        deValueStore =
            FormValueStore(
                d2,
                "recordUid",
                EntryMode.DE,
                null,
                crashReportController,
                networkUtils,
                resourceManager
            )
        dvValueStore =
            FormValueStore(
                d2,
                "recordUid",
                EntryMode.DV,
                null,
                crashReportController,
                networkUtils,
                resourceManager
            )
    }

    @Test
    fun `Trying to save an unique attribute should return a valid response`() {
        mockCheckUniqueFilter()

        val result = attrValueStore.save("uid", "uniqueValue", null)

        assertEquals(result.valueStoreResult, VALUE_NOT_UNIQUE)
    }

    private fun mockCheckUniqueFilter() {
        whenever(
            d2.trackedEntityModule().trackedEntityAttributes().uid("uid").blockingGet()
        ) doReturn mockedUniqueAttribute()
        whenever(
            d2.trackedEntityModule().trackedEntityAttributeValues()
                .byTrackedEntityAttribute().eq("uid")
        ) doReturn mock()
        whenever(
            d2.trackedEntityModule().trackedEntityAttributeValues()
                .byTrackedEntityAttribute().eq("uid")
                .byTrackedEntityInstance()
        ) doReturn mock()
        whenever(
            d2.trackedEntityModule().trackedEntityAttributeValues()
                .byTrackedEntityAttribute().eq("uid")
                .byTrackedEntityInstance().neq("recordUid")
        ) doReturn mock()
        whenever(
            d2.trackedEntityModule().trackedEntityAttributeValues()
                .byTrackedEntityAttribute().eq("uid")
                .byTrackedEntityInstance().neq("recordUid")
                .byValue()
        ) doReturn mock()
        whenever(
            d2.trackedEntityModule().trackedEntityAttributeValues()
                .byTrackedEntityAttribute().eq("uid")
                .byTrackedEntityInstance().neq("recordUid")
                .byValue().eq("uniqueValue")
        ) doReturn mock()
        whenever(
            d2.trackedEntityModule().trackedEntityAttributeValues()
                .byTrackedEntityAttribute().eq("uid")
                .byTrackedEntityInstance().neq("recordUid")
                .byValue().eq("uniqueValue")
                .blockingGet()
        ) doReturn mockedAttributeValueList()
    }

    @Test
    fun `Trying to save an attribute should return a valid response`() {
        whenever(
            d2.trackedEntityModule().trackedEntityAttributes().uid("uid").blockingGet()
        ) doReturn mockedAttribute()
        whenever(
            d2.trackedEntityModule().trackedEntityAttributeValues()
                .byTrackedEntityAttribute().eq("uid")
        ) doReturn mock()
        whenever(
            d2.trackedEntityModule().trackedEntityAttributeValues()
                .byTrackedEntityAttribute().eq("uid").byValue()
        ) doReturn mock()
        whenever(
            d2.trackedEntityModule().trackedEntityAttributeValues()
                .byTrackedEntityAttribute().eq("uid").byValue().eq("uniqueValue")
        ) doReturn mock()
        whenever(
            d2.trackedEntityModule().trackedEntityAttributeValues()
                .byTrackedEntityAttribute().eq("uid").byValue().eq("uniqueValue").blockingGet()
        ) doReturn mockedAttributeValueList()

        val result = attrValueStore.save("uid", "uniqueValue", null)
        assertEquals(result.valueStoreResult, VALUE_CHANGED)
    }

    @Test
    fun `Trying to save a DataElement should return a valid response`() {
        whenever(
            d2.dataElementModule().dataElements().uid("uid").blockingGet()
        ) doReturn mockedDataElement()

        val result = deValueStore.save("uid", "value", null)

        assertEquals(result.valueStoreResult, VALUE_CHANGED)
    }

    @Test
    fun `Null value should remove`() {
        whenever(
            d2.dataElementModule().dataElements().uid("uid").blockingGet()
        ) doReturn mockedDataElement()
        whenever(
            d2.trackedEntityModule().trackedEntityDataValues().value(
                "recordUid",
                "uid"
            )
        ) doReturn mock()
        whenever(
            d2.trackedEntityModule().trackedEntityDataValues().value(
                "recordUid",
                "uid"
            ).blockingExists()
        ) doReturn true
        whenever(
            d2.trackedEntityModule().trackedEntityDataValues().value(
                "recordUid",
                "uid"
            ).blockingGet()
        ) doReturn mockedDataElementValue()

        val result = deValueStore.save("uid", null, null)

        assertEquals(result.valueStoreResult, VALUE_CHANGED)
    }

    @Test
    fun `Uid which is not linked to a DE or an ATTR should end with correct result`() {
        whenever(
            d2.dataElementModule().dataElements().uid("wrongUid").blockingExists()
        ) doReturn false
        whenever(
            d2.trackedEntityModule().trackedEntityAttributes().uid("wrongUid").blockingExists()
        ) doReturn false

        val testSubscriber = deValueStore.saveWithTypeCheck("wrongUid", "test").test()

        testSubscriber.assertValueCount(1)
        testSubscriber.assertValue {
            it.valueStoreResult == ValueStoreResult.UID_IS_NOT_DE_OR_ATTR
        }
    }

    @Test
    fun `Should not delete data element value if field is option set`() {
        whenever(d2.optionModule().options().uid("optionUid").blockingGet()) doReturn
            Option.builder()
                .name("optionName")
                .uid("optionUid")
                .code("optionCode")
                .build()
        whenever(
            d2.trackedEntityModule().trackedEntityAttributeValues().value(
                "recordUid",
                "fieldUid"
            )
        ) doReturn mock()
        whenever(
            d2.trackedEntityModule().trackedEntityDataValues().value(
                "recordUid",
                "fieldUid"
            ).blockingExists()
        ) doReturn true

        whenever(
            d2.trackedEntityModule().trackedEntityDataValues().value(
                "recordUid",
                "fieldUid"
            ).blockingGet()
        ) doReturn TrackedEntityDataValue.builder()
            .dataElement("fieldUid")
            .event("recordUid")
            .value("optionCode")
            .build()
        whenever(
            d2.dataElementModule().dataElements()
                .uid("fieldUid").blockingGet()
        ) doReturn
            DataElement.builder()
                .uid("fieldUid")
                .valueType(ValueType.TEXT)
                .build()
        val storeResult = deValueStore.deleteOptionValueIfSelected(
            "fieldUid",
            "optionUid"
        )
        assert(storeResult.valueStoreResult == VALUE_CHANGED)
    }

    @Test
    fun `Should delete data element value if field is option set`() {
        whenever(d2.optionModule().options().uid("optionUid").blockingGet()) doReturn
            Option.builder()
                .name("optionName")
                .uid("optionUid")
                .code("optionCode")
                .build()
        whenever(
            d2.trackedEntityModule().trackedEntityDataValues().value(
                "recordUid",
                "fieldUid"
            )
        ) doReturn mock()
        whenever(
            d2.trackedEntityModule().trackedEntityDataValues().value(
                "recordUid",
                "fieldUid"
            ).blockingExists()
        ) doReturn false
        val storeResult = deValueStore.deleteOptionValueIfSelected("fieldUid", "optionUid")
        assert(
            storeResult.valueStoreResult == ValueStoreResult.VALUE_HAS_NOT_CHANGED
        )
    }

    @Test
    fun `Should return error when saving null value for a DE and no previous value exist`() {
        val testingUid = "uid"
        whenever(
            d2.dataElementModule().dataElements().uid(testingUid).blockingExists()
        ) doReturn true
        whenever(
            d2.trackedEntityModule().trackedEntityDataValues()
                .value("recordUid", testingUid)
        ) doReturn mock()
        whenever(
            d2.dataElementModule().dataElements().uid(testingUid).blockingGet()
        ) doReturn DataElement.builder()
            .uid(testingUid)
            .valueType(ValueType.TEXT)
            .build()
        whenever(
            d2.trackedEntityModule().trackedEntityDataValues()
                .value("recordUid", testingUid)
                .blockingExists()
        ) doReturn false
        deValueStore.saveWithTypeCheck(testingUid, null)
            .test()
            .assertNoErrors()
            .assertValue { result ->
                result.valueStoreResult == ValueStoreResult.VALUE_HAS_NOT_CHANGED
            }
    }

    @Test
    fun `Should return error when saving null value for an attr and no previous value exist`() {
        val testingUid = "uid"
        whenever(
            d2.dataElementModule().dataElements().uid(testingUid).blockingExists()
        ) doReturn false
        whenever(
            d2.trackedEntityModule().trackedEntityAttributes().uid(testingUid).blockingExists()
        ) doReturn true
        whenever(
            d2.trackedEntityModule().trackedEntityAttributes().uid(testingUid).blockingGet()
        ) doReturn TrackedEntityAttribute.builder()
            .uid(testingUid)
            .unique(false)
            .build()
        whenever(
            d2.trackedEntityModule().trackedEntityAttributeValues()
                .value(testingUid, "recordUid")
        ) doReturn mock()
        whenever(
            d2.trackedEntityModule().trackedEntityAttributes().uid(testingUid).blockingGet()
        ) doReturn TrackedEntityAttribute.builder()
            .uid(testingUid)
            .valueType(ValueType.TEXT)
            .build()
        whenever(
            d2.trackedEntityModule().trackedEntityAttributeValues()
                .value(testingUid, "recordUid")
                .blockingExists()
        ) doReturn false
        deValueStore.saveWithTypeCheck(testingUid, null)
            .test()
            .assertNoErrors()
            .assertValue { result ->
                result.valueStoreResult == ValueStoreResult.VALUE_HAS_NOT_CHANGED
            }
    }

    private fun mockedAttribute(): TrackedEntityAttribute {
        return TrackedEntityAttribute.builder()
            .uid("uid")
            .build()
    }

    private fun mockedDataElement(): DataElement {
        return DataElement.builder()
            .uid("uid")
            .valueType(ValueType.TEXT)
            .build()
    }

    private fun mockedUniqueAttribute(): TrackedEntityAttribute {
        return TrackedEntityAttribute.builder()
            .uid("uid")
            .unique(true)
            .build()
    }

    private fun mockedDataElementValue(): TrackedEntityDataValue {
        return TrackedEntityDataValue.builder()
            .dataElement("uid")
            .event("recordUid")
            .value("value")
            .build()
    }

    private fun mockedAttributeValueList(): List<TrackedEntityAttributeValue> {
        return arrayListOf(
            TrackedEntityAttributeValue.builder()
                .trackedEntityAttribute("uid")
                .trackedEntityInstance("tei")
                .value("uniqueValue")
                .build()
        )
    }
}
