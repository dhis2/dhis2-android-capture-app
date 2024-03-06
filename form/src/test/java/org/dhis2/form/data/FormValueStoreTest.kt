package org.dhis2.form.data

import java.io.File
import org.dhis2.commons.data.EntryMode
import org.dhis2.commons.network.NetworkUtils
import org.dhis2.commons.reporting.CrashReportController
import org.dhis2.commons.resources.ResourceManager
import org.dhis2.form.model.EnrollmentDetail
import org.dhis2.form.model.ValueStoreResult
import org.dhis2.form.model.ValueStoreResult.VALUE_CHANGED
import org.dhis2.form.model.ValueStoreResult.VALUE_NOT_UNIQUE
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.common.FeatureType
import org.hisp.dhis.android.core.common.ObjectWithUid
import org.hisp.dhis.android.core.common.ValueType
import org.hisp.dhis.android.core.dataelement.DataElement
import org.hisp.dhis.android.core.enrollment.Enrollment
import org.hisp.dhis.android.core.enrollment.EnrollmentObjectRepository
import org.hisp.dhis.android.core.maintenance.D2Error
import org.hisp.dhis.android.core.maintenance.D2ErrorCode
import org.hisp.dhis.android.core.option.Option
import org.hisp.dhis.android.core.option.OptionGroup
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttribute
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeValue
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeValueObjectRepository
import org.hisp.dhis.android.core.trackedentity.TrackedEntityDataValue
import org.hisp.dhis.android.core.trackedentity.TrackedEntityDataValueObjectRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class FormValueStoreTest {
    private lateinit var attrValueStore: FormValueStore
    private lateinit var deValueStore: FormValueStore
    private lateinit var dvValueStore: FormValueStore
    private val d2: D2 = Mockito.mock(D2::class.java, Mockito.RETURNS_DEEP_STUBS)
    private val crashReportController: CrashReportController = mock()
    private val networkUtils: NetworkUtils = mock()
    private val resourceManager: ResourceManager = mock()
    private val fileController: FileController = mock()
    private val enrollmentRepository: EnrollmentObjectRepository = mock()
    private val uniqueAttributeController: UniqueAttributeController = mock()

    @Before
    fun setUp() {
        attrValueStore =
            FormValueStore(
                d2,
                "recordUid",
                EntryMode.ATTR,
                enrollmentRepository,
                crashReportController,
                networkUtils,
                resourceManager,
                fileController,
                uniqueAttributeController
            )
        deValueStore =
            FormValueStore(
                d2,
                "recordUid",
                EntryMode.DE,
                null,
                crashReportController,
                networkUtils,
                resourceManager,
                fileController,
                uniqueAttributeController
            )
        dvValueStore =
            FormValueStore(
                d2,
                "recordUid",
                EntryMode.DV,
                null,
                crashReportController,
                networkUtils,
                resourceManager,
                fileController,
                uniqueAttributeController
            )
    }

    @Test
    fun `Should build form value store`() {
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

    @Test(expected = IllegalArgumentException::class)
    fun `Should throw exception when saving for data value entry mode`() {
        dvValueStore.save(
            uid = "uid",
            value = "value",
            extraData = null
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun `Should throw exception when storing file for data value entry mode`() {
        dvValueStore.storeFile(
            uid = "uid",
            "filePath"
        )
    }

    @Test
    fun `Should return error updating value if file path is null for data element entry mode`() {
        val mockedDataElement: DataElement = mock {
            on { valueType() } doReturn ValueType.IMAGE
        }
        whenever(
            d2.dataElementModule().dataElements().uid(any()).blockingGet()
        ) doReturn mockedDataElement
        val result = deValueStore.storeFile(
            uid = "uid",
            filePath = null
        )

        assertTrue(result?.valueStoreResult == ValueStoreResult.ERROR_UPDATING_VALUE)
    }

    @Test
    fun `Should return error updating value if file path is null for attribute entry mode`() {
        val mockedAttribute: TrackedEntityAttribute = mock {
            on { valueType() } doReturn ValueType.IMAGE
        }
        whenever(
            d2.trackedEntityModule().trackedEntityAttributes().uid(any()).blockingGet()
        ) doReturn mockedAttribute
        val result = attrValueStore.storeFile(
            uid = "uid",
            filePath = null
        )

        assertTrue(result?.valueStoreResult == ValueStoreResult.ERROR_UPDATING_VALUE)
    }

    @Test
    fun `Should return error updating value when generating file resource`() {
        val mockedDataElement: DataElement = mock {
            on { valueType() } doReturn ValueType.FILE_RESOURCE
        }
        whenever(
            d2.dataElementModule().dataElements().uid(any()).blockingGet()
        ) doReturn mockedDataElement
        whenever(
            d2.fileResourceModule().fileResources()
        ) doReturn mock()
        whenever(
            d2.fileResourceModule().fileResources().blockingAdd(File("filePath"))
        ) doThrow D2Error.builder()
            .errorCode(D2ErrorCode.UNEXPECTED)
            .errorDescription("error test")
            .build()
        val result = deValueStore.storeFile(
            uid = "uid",
            filePath = "filePath"
        )

        assertTrue(result?.valueStoreResult == ValueStoreResult.ERROR_UPDATING_VALUE)
    }

    @Test
    fun `Should return file resource uid result`() {
        val generatedUid = "fileResourceUid"
        val mockedDataElement: DataElement = mock {
            on { valueType() } doReturn ValueType.FILE_RESOURCE
        }
        whenever(
            d2.dataElementModule().dataElements().uid(any()).blockingGet()
        ) doReturn mockedDataElement
        whenever(
            d2.fileResourceModule().fileResources()
        ) doReturn mock()
        whenever(
            d2.fileResourceModule().fileResources().blockingAdd(File("filePath"))
        ) doReturn generatedUid
        val result = deValueStore.storeFile(
            uid = "uid",
            filePath = "filePath"
        )

        assertTrue(result?.valueStoreResult == ValueStoreResult.FILE_SAVED)
        assertTrue(result?.uid == generatedUid)
    }

    @Test
    fun `Should try to resize image`() {
        val generatedUid = "fileResourceUid"
        val mockedDataElement: DataElement = mock {
            on { valueType() } doReturn ValueType.IMAGE
        }
        whenever(
            d2.dataElementModule().dataElements().uid(any()).blockingGet()
        ) doReturn mockedDataElement
        whenever(
            d2.fileResourceModule().fileResources()
        ) doReturn mock()
        whenever(
            d2.fileResourceModule().fileResources().blockingAdd(File("filePath"))
        ) doReturn generatedUid
        deValueStore.storeFile(
            uid = "uid",
            filePath = "filePath"
        )

        verify(fileController).resize("filePath")
    }

    @Test
    fun `Should set enrollment date`() {
        val uid = EnrollmentDetail.ENROLLMENT_DATE_UID.name
        val result = attrValueStore.save(uid, "2023-01-01'T'00:00:00", null)
        assertTrue(result.valueStoreResult == VALUE_CHANGED)
    }

    @Test
    fun `Should set incident date`() {
        val uid = EnrollmentDetail.INCIDENT_DATE_UID.name
        val result = attrValueStore.save(uid, "2023-01-01'T'00:00:00", null)
        assertTrue(result.valueStoreResult == VALUE_CHANGED)
    }

    @Test
    fun `Should set enrollment org unit`() {
        val uid = EnrollmentDetail.ORG_UNIT_UID.name
        val result = attrValueStore.save(uid, "orgUnitUid", null)
        assertTrue(result.valueStoreResult == VALUE_CHANGED)
    }

    @Test
    fun `Should set tei coordinate`() {
        val uid = EnrollmentDetail.TEI_COORDINATES_UID.name
        val point = "[12.0, 12.0]"
        whenever(
            d2.trackedEntityModule().trackedEntityInstances().uid(any())
        ) doReturn mock()
        val mockedEnrollment: Enrollment = mock {
            on { trackedEntityInstance() } doReturn "teiUid"
        }
        whenever(
            enrollmentRepository.blockingGet()
        ) doReturn mockedEnrollment
        whenever(
            d2.trackedEntityModule().trackedEntityInstances().uid("teiUid")
        ) doReturn mock()
        val result = attrValueStore.save(uid, point, FeatureType.POINT.name)
        verify(d2.trackedEntityModule().trackedEntityInstances().uid("teiUid")).setGeometry(any())
        assertTrue(result.valueStoreResult == VALUE_CHANGED)
    }

    @Test
    fun `Should set enrollment coordinate`() {
        val uid = EnrollmentDetail.ENROLLMENT_COORDINATES_UID.name
        val point = "[12.0, 12.0]"
        val result = attrValueStore.save(uid, point, FeatureType.POINT.name)
        verify(enrollmentRepository).setGeometry(any())
        assertTrue(result.valueStoreResult == VALUE_CHANGED)
    }

    @Test
    fun `Should throw exception when updating enrollment coordinate`() {
        val uid = EnrollmentDetail.ENROLLMENT_COORDINATES_UID.name
        val point = "[12.0, 12.0]"
        whenever(
            enrollmentRepository.setGeometry(any())
        ) doThrow D2Error.builder()
            .errorCode(D2ErrorCode.UNEXPECTED)
            .errorDescription("error test")
            .build()
        val result = attrValueStore.save(uid, point, FeatureType.POINT.name)
        verify(enrollmentRepository).setGeometry(any())
        verify(crashReportController).trackError(any(), any())
        assertTrue(result.valueStoreResult == ValueStoreResult.ERROR_UPDATING_VALUE)
    }

    @Test
    fun `Trying to save an unique attribute should return a valid response`() {
        mockCheckUniqueFilter()

        val result = attrValueStore.save("uid", "uniqueValue", null)

        assertEquals(result.valueStoreResult, VALUE_NOT_UNIQUE)
    }

    @Test
    fun `Trying to save a null unique attribute should return a valid response`() {
        mockCheckUniqueFilter()

        val result = attrValueStore.save("uid", null, null)

        assertEquals(result.valueStoreResult, ValueStoreResult.VALUE_HAS_NOT_CHANGED)
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
    fun `Trying to save an unique attribute should return a not unique response online`() {
        mockOnlineCheckUniqueFilter(unique = true, orgUnitScope = false, checkResult = false)
        val result = attrValueStore.save("uid", "uniqueValue", null)
        assertEquals(result.valueStoreResult, VALUE_NOT_UNIQUE)
    }

    @Test
    fun `Trying to save a null unique attribute should return a unique response online`() {
        mockOnlineCheckUniqueFilter(unique = true, orgUnitScope = false, checkResult = false)
        val result = attrValueStore.save("uid", null, null)
        assertEquals(result.valueStoreResult, ValueStoreResult.VALUE_HAS_NOT_CHANGED)
    }

    @Test
    fun `Trying to save a non unique attribute should return a unique response online`() {
        mockOnlineCheckUniqueFilter(unique = false, orgUnitScope = false, checkResult = false)
        val result = attrValueStore.save("uid", "value", null)
        assertEquals(result.valueStoreResult, VALUE_CHANGED)
    }

    private fun mockOnlineCheckUniqueFilter(
        unique: Boolean,
        orgUnitScope: Boolean,
        checkResult: Boolean
    ) {
        whenever(networkUtils.isOnline()) doReturn true
        val mockedEnrollment: Enrollment = mock {
            on { program() } doReturn "programUid"
        }
        whenever(enrollmentRepository.blockingGet()) doReturn mockedEnrollment
        val mockedAttr: TrackedEntityAttribute = mock {
            on { uid() } doReturn "uid"
            on { unique() } doReturn unique
            on { orgUnitScope() } doReturn orgUnitScope
        }
        whenever(
            d2.trackedEntityModule().trackedEntityAttributes().uid("uid").blockingGet()
        ) doReturn mockedAttr

        whenever(
            uniqueAttributeController.checkAttributeOnline(
                orgUnitScope,
                "programUid",
                "teiUid",
                "uid",
                "uniqueValue"
            )
        ) doReturn checkResult
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
    fun `Should log error when trying to save an attribute`() {
        val mockedRepository: TrackedEntityAttributeValueObjectRepository = mock {
            on { blockingExists() } doReturn false
            on { blockingSet(any()) } doThrow D2Error.builder()
                .errorCode(D2ErrorCode.UNEXPECTED)
                .errorDescription("error test")
                .build()
        }
        whenever(
            d2.trackedEntityModule().trackedEntityAttributeValues()
                .value("uid", "recordUid")
        )doReturn mockedRepository
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
        verify(crashReportController).addBreadCrumb(
            "blockingSetCheck Crash",
            "Attribute: uid," +
                "" + " value: uniqueValue"
        )
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
    fun `Should not delete attribute value if field is option set`() {
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
            d2.trackedEntityModule().trackedEntityAttributeValues().value(
                "fieldUid",
                "recordUid"
            ).blockingExists()
        ) doReturn true

        whenever(
            d2.trackedEntityModule().trackedEntityAttributeValues().value(
                "fieldUid",
                "recordUid"
            ).blockingGet()
        ) doReturn TrackedEntityAttributeValue.builder()
            .trackedEntityAttribute("fieldUid")
            .trackedEntityInstance("recordUid")
            .value("optionCode")
            .build()
        whenever(
            d2.trackedEntityModule().trackedEntityAttributes()
                .uid("fieldUid").blockingGet()
        ) doReturn
            TrackedEntityAttribute.builder()
                .uid("fieldUid")
                .valueType(ValueType.TEXT)
                .build()
        val storeResult = attrValueStore.deleteOptionValueIfSelected(
            "fieldUid",
            "optionUid"
        )
        assert(storeResult.valueStoreResult == VALUE_CHANGED)
    }

    @Test
    fun `Should delete attribute value if field is option set`() {
        whenever(d2.optionModule().options().uid("optionUid").blockingGet()) doReturn
            Option.builder()
                .name("optionName")
                .uid("optionUid")
                .code("optionCode")
                .build()
        whenever(
            d2.trackedEntityModule().trackedEntityAttributeValues().value(

                "fieldUid",
                "recordUid"
            )
        ) doReturn mock()
        whenever(
            d2.trackedEntityModule().trackedEntityAttributeValues().value(
                "fieldUid",
                "recordUid"
            ).blockingExists()
        ) doReturn false
        val storeResult = attrValueStore.deleteOptionValueIfSelected("fieldUid", "optionUid")
        assert(
            storeResult.valueStoreResult == ValueStoreResult.VALUE_HAS_NOT_CHANGED
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun `Should throw exception when deleting option value in data value entry mode`() {
        val storeResult = dvValueStore.deleteOptionValueIfSelected("fieldUid", "optionUid")
        assert(
            storeResult.valueStoreResult == ValueStoreResult.VALUE_HAS_NOT_CHANGED
        )
    }

    @Test
    fun `Should delete data element value if not in group`() {
        mockOptionsInGroup()
        val mockedDataValueRepository: TrackedEntityDataValueObjectRepository = mock {
            on { blockingExists() } doReturn true
            on { blockingGet() } doReturn TrackedEntityDataValue.builder()
                .event("recordUid")
                .dataElement("uid")
                .value("option_code_1")
                .build()
        }
        whenever(
            d2.trackedEntityModule().trackedEntityDataValues().value("recordUid", "uid")
        )doReturn mockedDataValueRepository

        whenever(
            d2.dataElementModule().dataElements().uid("uid").blockingGet()
        ) doReturn mockedDataElement()

        val result = deValueStore.deleteOptionValueIfSelectedInGroup(
            field = "uid",
            optionGroupUid = "optionGroupUid",
            isInGroup = true
        )
        assertTrue(result.valueStoreResult == ValueStoreResult.VALUE_CHANGED)
    }

    @Test
    fun `Should not delete data element value if does not exist`() {
        mockOptionsInGroup()
        val mockedDataValueRepository: TrackedEntityDataValueObjectRepository = mock {
            on { blockingExists() } doReturn false
            on { blockingGet() } doReturn TrackedEntityDataValue.builder()
                .event("recordUid")
                .dataElement("uid")
                .value("option_code_1")
                .build()
        }
        whenever(
            d2.trackedEntityModule().trackedEntityDataValues().value("recordUid", "uid")
        )doReturn mockedDataValueRepository

        whenever(
            d2.dataElementModule().dataElements().uid("uid").blockingGet()
        ) doReturn mockedDataElement()

        val result = deValueStore.deleteOptionValueIfSelectedInGroup(
            field = "uid",
            optionGroupUid = "optionGroupUid",
            isInGroup = true
        )
        assertTrue(result.valueStoreResult == ValueStoreResult.VALUE_HAS_NOT_CHANGED)
    }

    @Test
    fun `Should not delete data element value if not in group`() {
        mockOptionsInGroup()
        val mockedDataValueRepository: TrackedEntityDataValueObjectRepository = mock {
            on { blockingExists() } doReturn true
            on { blockingGet() } doReturn TrackedEntityDataValue.builder()
                .event("recordUid")
                .dataElement("uid")
                .value("option_code_3")
                .build()
        }
        whenever(
            d2.trackedEntityModule().trackedEntityDataValues().value("recordUid", "uid")
        )doReturn mockedDataValueRepository

        whenever(
            d2.dataElementModule().dataElements().uid("uid").blockingGet()
        ) doReturn mockedDataElement()

        val result = deValueStore.deleteOptionValueIfSelectedInGroup(
            field = "uid",
            optionGroupUid = "optionGroupUid",
            isInGroup = true
        )
        assertTrue(result.valueStoreResult == ValueStoreResult.VALUE_HAS_NOT_CHANGED)
    }

    @Test
    fun `Should delete attribute value if not in group`() {
        mockOptionsInGroup()
        val mockedDataValueRepository: TrackedEntityAttributeValueObjectRepository = mock {
            on { blockingExists() } doReturn true
            on { blockingGet() } doReturn TrackedEntityAttributeValue.builder()
                .trackedEntityInstance("recordUid")
                .trackedEntityAttribute("uid")
                .value("option_code_1")
                .build()
        }
        whenever(
            d2.trackedEntityModule().trackedEntityAttributeValues().value("uid", "recordUid")
        )doReturn mockedDataValueRepository

        whenever(
            d2.trackedEntityModule().trackedEntityAttributes().uid("uid").blockingGet()
        ) doReturn mockedAttribute()

        val result = attrValueStore.deleteOptionValueIfSelectedInGroup(
            field = "uid",
            optionGroupUid = "optionGroupUid",
            isInGroup = true
        )
        assertTrue(result.valueStoreResult == ValueStoreResult.VALUE_CHANGED)
    }

    @Test
    fun `Should not delete attribute value if does not exist`() {
        mockOptionsInGroup()
        val mockedDataValueRepository: TrackedEntityAttributeValueObjectRepository = mock {
            on { blockingExists() } doReturn false
            on { blockingGet() } doReturn TrackedEntityAttributeValue.builder()
                .trackedEntityInstance("recordUid")
                .trackedEntityAttribute("uid")
                .value("option_code_1")
                .build()
        }
        whenever(
            d2.trackedEntityModule().trackedEntityAttributeValues().value("uid", "recordUid")
        )doReturn mockedDataValueRepository

        whenever(
            d2.trackedEntityModule().trackedEntityAttributes().uid("uid").blockingGet()
        ) doReturn mockedAttribute()

        val result = attrValueStore.deleteOptionValueIfSelectedInGroup(
            field = "uid",
            optionGroupUid = "optionGroupUid",
            isInGroup = true
        )
        assertTrue(result.valueStoreResult == ValueStoreResult.VALUE_HAS_NOT_CHANGED)
    }

    @Test
    fun `Should not delete attribute value if not in group`() {
        mockOptionsInGroup()
        val mockedDataValueRepository: TrackedEntityAttributeValueObjectRepository = mock {
            on { blockingExists() } doReturn true
            on { blockingGet() } doReturn TrackedEntityAttributeValue.builder()
                .trackedEntityInstance("recordUid")
                .trackedEntityAttribute("uid")
                .value("option_code_3")
                .build()
        }
        whenever(
            d2.trackedEntityModule().trackedEntityAttributeValues().value("uid", "recordUid")
        )doReturn mockedDataValueRepository

        whenever(
            d2.trackedEntityModule().trackedEntityAttributes().uid("uid").blockingGet()
        ) doReturn mockedAttribute()

        val result = attrValueStore.deleteOptionValueIfSelectedInGroup(
            field = "uid",
            optionGroupUid = "optionGroupUid",
            isInGroup = true
        )
        assertTrue(result.valueStoreResult == ValueStoreResult.VALUE_HAS_NOT_CHANGED)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `Should throw exception when deleting option value`() {
        mockOptionsInGroup()
        dvValueStore.deleteOptionValueIfSelectedInGroup(
            field = "uid",
            optionGroupUid = "optionGroupUid",
            isInGroup = true
        )
    }

    private fun mockOptionsInGroup() {
        val mockedOption1: Option = mock {
            on { uid() } doReturn "option_1"
            on { code() } doReturn "option_code_1"
        }
        val mockedOption2: Option = mock {
            on { uid() } doReturn "option_2"
            on { code() } doReturn "option_code_2"
        }
        val mockedOptionGroup: OptionGroup = mock {
            on { options() } doReturn listOf(
                ObjectWithUid.create("option_1"),
                ObjectWithUid.create("option_2")
            )
        }

        whenever(
            d2.optionModule().optionGroups()
                .withOptions()
                .uid("optionGroupUid")
                .blockingGet()
        ) doReturn mockedOptionGroup

        whenever(
            d2.optionModule().options().uid("option_1").blockingGet()
        ) doReturn mockedOption1
        whenever(
            d2.optionModule().options().uid("option_2").blockingGet()
        ) doReturn mockedOption2
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

    private fun mockedAttribute(valueType: ValueType = ValueType.TEXT): TrackedEntityAttribute {
        return TrackedEntityAttribute.builder()
            .uid("uid")
            .valueType(valueType)
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
