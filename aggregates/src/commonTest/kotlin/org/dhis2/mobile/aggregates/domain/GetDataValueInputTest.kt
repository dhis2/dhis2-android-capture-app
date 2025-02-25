package org.dhis2.mobile.aggregates.domain

import kotlinx.coroutines.test.runTest
import org.dhis2.mobile.aggregates.data.DataSetInstanceRepository
import org.dhis2.mobile.aggregates.model.DataElementInfo
import org.dhis2.mobile.aggregates.model.InputType
import org.dhis2.mobile.aggregates.onRunBlocking
import org.dhis2.mobile.aggregates.ui.inputs.TableId
import org.dhis2.mobile.aggregates.ui.inputs.TableIdType
import org.dhis2.mobile.aggregates.ui.states.InputExtra
import org.junit.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class GetDataValueInputTest {
    private val mockedDataElementInfo = mock<DataElementInfo> {
        on { label } doReturn "DataElementLabel"
        on { description } doReturn "DataElementDescription"
        on { isRequired } doReturn true
    }
    private val repository = mock<DataSetInstanceRepository> {
        onRunBlocking { dataElementInfo(any(), any(), any()) } doReturn mockedDataElementInfo
        onRunBlocking { value(any(), any(), any(), any(), any()) } doReturn null
        onRunBlocking { conflicts(any(), any(), any(), any(), any(), any()) } doReturn Pair(
            emptyList(),
            emptyList(),
        )
        onRunBlocking { getCoordinatesFrom(any()) } doReturn Pair(0.0, 0.0)
    }

    val getDataValueInputTest = GetDataValueInput(
        dataSetUid = "dataSetUid",
        periodId = "periodId",
        orgUnitUid = "orgUnitUid",
        attrOptionComboUid = "attrOptionComboUid",
        repository = repository,
    )

    @Test
    fun `should return cell info`() = runTest {
        inputTypeValues.forEach { inputType ->
            whenever(mockedDataElementInfo.inputType) doReturn inputType
            val result = getDataValueInputTest(
                rowIds = listOf(TableId("dataElementId", TableIdType.DataElement)),
                columnIds = listOf(TableId("catOptionComboUid", TableIdType.CategoryOptionCombo)),
            )

            assertTrue(
                when (result.inputType) {
                    InputType.Age -> result.inputExtra is InputExtra.Age
                    InputType.Date, InputType.Time, InputType.DateTime -> result.inputExtra is InputExtra.Date
                    InputType.Coordinates -> result.inputExtra is InputExtra.Coordinate
                    InputType.FileResource -> result.inputExtra is InputExtra.File
                    else -> result.inputExtra is InputExtra.None
                },
            )
        }
    }

    @Test
    fun `should throw error if more than one data element is provided`() = runTest {
        val exception = assertThrows<IllegalStateException> {
            getDataValueInputTest(
                rowIds = listOf(TableId("dataElementId", TableIdType.DataElement)),
                columnIds = listOf(TableId("dataElementId", TableIdType.DataElement)),
            )
        }
        assertEquals(
            "Only one data element can be provided",
            exception.message,
        )
    }

    @Test
    fun `should throw error if more than one category option combo is provided`() = runTest {
        val exception = assertThrows<IllegalStateException> {
            getDataValueInputTest(
                rowIds = listOf(
                    TableId("dataElementUid", TableIdType.DataElement),
                    TableId("catOptionComboUid", TableIdType.CategoryOptionCombo),
                ),
                columnIds = listOf(TableId("catOptionComboUid", TableIdType.CategoryOptionCombo)),
            )
        }
        assertEquals(
            "Only one category option combo can be provided",
            exception.message,
        )
    }

    @Test
    fun `should throw error if category options and category option combos are provided`() =
        runTest {
            val exception = assertThrows<IllegalStateException> {
                getDataValueInputTest(
                    rowIds = listOf(
                        TableId("dataElementUid", TableIdType.DataElement),
                        TableId("catOptionUid", TableIdType.CategoryOption),
                    ),
                    columnIds = listOf(
                        TableId("catOptionComboUid", TableIdType.CategoryOptionCombo),
                    ),
                )
            }
            assertEquals(
                "Category options and category option combos cannot be provided at the same time",
                exception.message,
            )
        }

    private val inputTypeValues = listOf(
        InputType.Text,
        InputType.LongText,
        InputType.Letter,
        InputType.PhoneNumber,
        InputType.Email,
        InputType.Boolean,
        InputType.TrueOnly,
        InputType.DateTime,
        InputType.Date,
        InputType.Time,
        InputType.Number,
        InputType.UnitInterval,
        InputType.Percentage,
        InputType.Integer,
        InputType.IntegerPositive,
        InputType.IntegerNegative,
        InputType.IntegerZeroOrPositive,
        InputType.TrackerAssociate,
        InputType.Username,
        InputType.Coordinates,
        InputType.OrganisationUnit,
        InputType.Reference,
        InputType.Age,
        InputType.Url,
        InputType.FileResource,
        InputType.Image,
        InputType.GeoJson,
        InputType.MultiText,
    )
}
