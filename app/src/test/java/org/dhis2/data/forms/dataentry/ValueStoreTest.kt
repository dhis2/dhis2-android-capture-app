package org.dhis2.data.forms.dataentry

import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.common.ValueType
import org.hisp.dhis.android.core.dataelement.DataElement
import org.hisp.dhis.android.core.option.Option
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttribute
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeValue
import org.hisp.dhis.android.core.trackedentity.TrackedEntityDataValue
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito

class ValueStoreTest {

    private lateinit var attrValueStore: ValueStore
    private lateinit var deValueStore: ValueStore
    private lateinit var dvValueStore: ValueStore
    private val d2: D2 = Mockito.mock(D2::class.java, Mockito.RETURNS_DEEP_STUBS)

    @Before
    fun setUp() {
        attrValueStore = ValueStoreImpl(d2, "recordUid", DataEntryStore.EntryMode.ATTR)
        deValueStore = ValueStoreImpl(d2, "recordUid", DataEntryStore.EntryMode.DE)
        dvValueStore = ValueStoreImpl(d2, "recordUid", DataEntryStore.EntryMode.DV)
    }

    @Test
    fun `Trying to save an unique attribute should return a valid response`() {
        whenever(
            d2.trackedEntityModule().trackedEntityAttributes().uid("uid").blockingGet()
        ) doReturn mockedUniqueAttribute()
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

        val testSubscriber = attrValueStore.save("uid", "uniqueValue").test()

        testSubscriber.assertValueCount(1)
        testSubscriber.assertValue {
            it.valueStoreResult == ValueStoreImpl.ValueStoreResult.VALUE_NOT_UNIQUE
        }
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

        val testSubscriber = attrValueStore.save("uid", "uniqueValue").test()

        testSubscriber.assertValueCount(1)
        testSubscriber.assertValue {
            it.valueStoreResult == ValueStoreImpl.ValueStoreResult.VALUE_CHANGED
        }
    }

    @Test
    fun `Trying to save a DataElement should return a valid response`() {
        whenever(
            d2.dataElementModule().dataElements().uid("uid").blockingGet()
        ) doReturn mockedDataElement()

        val testSubscriber = deValueStore.save("uid", "value").test()

        testSubscriber.assertValueCount(1)
        testSubscriber.assertValue {
            it.valueStoreResult == ValueStoreImpl.ValueStoreResult.VALUE_CHANGED
        }
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

        val testSubscriber = deValueStore.save("uid", null).test()

        testSubscriber.assertValueCount(1)
        testSubscriber.assertValue {
            it.valueStoreResult == ValueStoreImpl.ValueStoreResult.VALUE_CHANGED
        }
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
            it.valueStoreResult == ValueStoreImpl.ValueStoreResult.UID_IS_NOT_DE_OR_ATTR
        }
    }

    @Test
    fun `Should not delete data element value if field is option set`() {
        whenever(d2.optionModule().options().uid("optionUid").blockingGet()) doReturn Option.builder()
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
        whenever(d2.dataElementModule().dataElements().uid("fieldUid").blockingGet())doReturn DataElement.builder()
            .uid("fieldUid")
            .valueType(ValueType.TEXT)
            .build()
        val storeResult = deValueStore.deleteOptionValueIfSelected("fieldUid", "optionUid")
        assert(storeResult.valueStoreResult == ValueStoreImpl.ValueStoreResult.VALUE_CHANGED)
    }

    @Test
    fun `Should delete data element value if field is option set`() {
        whenever(d2.optionModule().options().uid("optionUid").blockingGet()) doReturn Option.builder()
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
        assert(storeResult.valueStoreResult == ValueStoreImpl.ValueStoreResult.VALUE_HAS_NOT_CHANGED)
    }

    fun mockedAttribute(): TrackedEntityAttribute {
        return TrackedEntityAttribute.builder()
            .uid("uid")
            .build()
    }

    fun mockedDataElement(): DataElement {
        return DataElement.builder()
            .uid("uid")
            .valueType(ValueType.TEXT)
            .build()
    }

    fun mockedUniqueAttribute(): TrackedEntityAttribute {
        return TrackedEntityAttribute.builder()
            .uid("uid")
            .unique(true)
            .build()
    }

    fun mockedDataElementValue(): TrackedEntityDataValue {
        return TrackedEntityDataValue.builder()
            .dataElement("uid")
            .event("recordUid")
            .value("value")
            .build()
    }

    fun mockedAttributeValueList(): List<TrackedEntityAttributeValue> {
        return arrayListOf(
            TrackedEntityAttributeValue.builder()
                .trackedEntityAttribute("uid")
                .trackedEntityInstance("tei")
                .value("uniqueValue")
                .build()
        )
    }
}
