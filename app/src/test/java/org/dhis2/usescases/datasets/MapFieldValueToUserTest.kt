package org.dhis2.usescases.datasets

import org.dhis2.commons.resources.ResourceManager
import org.dhis2.data.forms.dataentry.tablefields.edittext.EditTextViewModel
import org.dhis2.usescases.datasets.dataSetTable.dataSetSection.DataValueRepository
import org.dhis2.usescases.datasets.dataSetTable.dataSetSection.MapFieldValueToUser
import org.hisp.dhis.android.core.common.ValueType
import org.hisp.dhis.android.core.dataelement.DataElement
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

const val UNSUPPORTED_VALUES = "Unsupported Values"
const val DATAELEMENT_UID = "dataElement_uid"

class MapFieldValueToUserTest {

    private val resources: ResourceManager = mock()
    private val repository: DataValueRepository = mock()

    private lateinit var mapFieldValueToUser: MapFieldValueToUser

    @Before
    fun setUp() {
        mapFieldValueToUser = MapFieldValueToUser(resources, repository)
    }

    @Test
    fun `Should Map Boolean`() {
        val fieldViewModel = EditTextViewModel.create(
            "",
            "",
            true,
            "True",
            "",
            1,
            ValueType.BOOLEAN,
            "",
            true,
            "",
            "none",
            listOf(),
            "",
            0,
            1,
            "1",
            "",
        )
        val dataElement =
            DataElement.builder().uid(DATAELEMENT_UID).valueType(ValueType.BOOLEAN).build()

        whenever(resources.getString(any())) doReturn "Yes"
        val result = mapFieldValueToUser.map(fieldViewModel, dataElement)
        assert(result == "Yes")
    }

    @Test
    fun `Should Map Org unit`() {
        val orgUnit = "Madrid"
        val fieldViewModel = EditTextViewModel.create(
            "",
            "",
            true,
            orgUnit,
            "",
            1,
            ValueType.ORGANISATION_UNIT,
            "",
            true,
            "",
            "none",
            listOf(),
            "",
            0,
            1,
            "1",
            "",
        )
        val dataElement =
            DataElement.builder().uid(DATAELEMENT_UID).valueType(ValueType.ORGANISATION_UNIT)
                .build()

        mapFieldValueToUser.map(fieldViewModel, dataElement)

        verify(repository).getOrgUnitById(orgUnit)
    }

    @Test
    fun `Should not map image values`() {
        val fieldViewModel = EditTextViewModel.create(
            "",
            "",
            true,
            "True",
            "",
            1,
            ValueType.IMAGE,
            "",
            true,
            "",
            "none",
            listOf(),
            "",
            0,
            1,
            "1",
            "",
        )
        whenever(resources.getString(any())) doReturn UNSUPPORTED_VALUES
        val dataElement =
            DataElement.builder().uid(DATAELEMENT_UID).valueType(ValueType.IMAGE).build()

        val result = mapFieldValueToUser.map(fieldViewModel, dataElement)
        assert(result == UNSUPPORTED_VALUES)
    }

    @Test
    fun `Should map file resources`() {
        val file = "file"
        val fieldViewModel = EditTextViewModel.create(
            "",
            "",
            true,
            file,
            "",
            1,
            ValueType.FILE_RESOURCE,
            "",
            true,
            "",
            "none",
            listOf(),
            "",
            0,
            1,
            "1",
            "",
        )
        val dataElement =
            DataElement.builder().uid(DATAELEMENT_UID).valueType(ValueType.FILE_RESOURCE).build()

        val result = mapFieldValueToUser.map(fieldViewModel, dataElement)
        assert(result == file)
    }

    @Test
    fun `Should not map tracker associate`() {
        val fieldViewModel = EditTextViewModel.create(
            "",
            "",
            true,
            "True",
            "",
            1,
            ValueType.TRACKER_ASSOCIATE,
            "",
            true,
            "",
            "none",
            listOf(),
            "",
            0,
            1,
            "1",
            "",
        )
        whenever(resources.getString(any())) doReturn UNSUPPORTED_VALUES
        val dataElement =
            DataElement.builder().uid(DATAELEMENT_UID).valueType(ValueType.TRACKER_ASSOCIATE)
                .build()

        val result = mapFieldValueToUser.map(fieldViewModel, dataElement)
        assert(result == UNSUPPORTED_VALUES)
    }

    @Test
    fun `Should not map reference`() {
        val fieldViewModel = EditTextViewModel.create(
            "",
            "",
            true,
            "True",
            "",
            1,
            ValueType.REFERENCE,
            "",
            true,
            "",
            "none",
            listOf(),
            "",
            0,
            1,
            "1",
            "",
        )
        whenever(resources.getString(any())) doReturn UNSUPPORTED_VALUES
        val dataElement =
            DataElement.builder().uid(DATAELEMENT_UID).valueType(ValueType.REFERENCE).build()

        val result = mapFieldValueToUser.map(fieldViewModel, dataElement)
        assert(result == UNSUPPORTED_VALUES)
    }

    @Test
    fun `Should not map geojson`() {
        val fieldViewModel = EditTextViewModel.create(
            "",
            "",
            true,
            "True",
            "",
            1,
            ValueType.GEOJSON,
            "",
            true,
            "",
            "none",
            listOf(),
            "",
            0,
            1,
            "1",
            "",
        )
        whenever(resources.getString(any())) doReturn UNSUPPORTED_VALUES
        val dataElement =
            DataElement.builder().uid(DATAELEMENT_UID).valueType(ValueType.GEOJSON).build()

        val result = mapFieldValueToUser.map(fieldViewModel, dataElement)
        assert(result == UNSUPPORTED_VALUES)
    }

    @Test
    fun `Should not map username`() {
        val fieldViewModel = EditTextViewModel.create(
            "",
            "",
            true,
            "True",
            "",
            1,
            ValueType.USERNAME,
            "",
            true,
            "",
            "none",
            listOf(),
            "",
            0,
            1,
            "1",
            "",
        )
        whenever(resources.getString(any())) doReturn UNSUPPORTED_VALUES
        val dataElement =
            DataElement.builder().uid(DATAELEMENT_UID).valueType(ValueType.USERNAME).build()

        val result = mapFieldValueToUser.map(fieldViewModel, dataElement)
        assert(result == UNSUPPORTED_VALUES)
    }
}
