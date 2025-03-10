package org.dhis2.mobile.aggregates.domain

import kotlinx.coroutines.test.runTest
import org.dhis2.mobile.aggregates.data.DataSetInstanceRepository
import org.dhis2.mobile.aggregates.data.OptionRepository
import org.dhis2.mobile.aggregates.model.CellValueExtra
import org.dhis2.mobile.aggregates.model.DataElementInfo
import org.dhis2.mobile.aggregates.model.InputType
import org.dhis2.mobile.aggregates.onRunBlocking
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import kotlin.test.assertTrue

internal class GetDataValueInputTest {
    private val mockedDataElementInfo = mock<DataElementInfo> {
        on { label } doReturn "DataElementLabel"
        on { description } doReturn "DataElementDescription"
        on { isRequired } doReturn true
    }
    private val repository = mock<DataSetInstanceRepository> {
        onRunBlocking { dataElementInfo(any(), any(), any()) } doReturn mockedDataElementInfo
        onRunBlocking { value(any(), any(), any(), any(), any()) } doReturn "Current value"
        onRunBlocking { conflicts(any(), any(), any(), any(), any(), any()) } doReturn Pair(
            emptyList(),
            emptyList(),
        )
        onRunBlocking { getCoordinatesFrom(any()) } doReturn Pair(0.0, 0.0)
    }

    private val optionRepository: OptionRepository = mock()

    val getDataValueInputTest = GetDataValueInput(
        dataSetUid = "dataSetUid",
        periodId = "periodId",
        orgUnitUid = "orgUnitUid",
        attrOptionComboUid = "attrOptionComboUid",
        repository = repository,
        optionRepository = optionRepository,
    )

    @Test
    fun `should return cell info`() = runTest {
        inputTypeValues.forEach { inputType ->
            whenever(mockedDataElementInfo.inputType) doReturn inputType
            whenever(optionRepository.optionCount(any())) doReturn 1
            val result = getDataValueInputTest(
                dataElementUid = "dataElementId",
                categoryOptionComboUidData = Pair("catOptionComboUid", emptyList()),
            )

            print("INPUTTYPE: $inputType, extra: ${result.inputExtra}")

            assertTrue(
                when (result.inputType) {
                    InputType.Coordinates -> result.inputExtra is CellValueExtra.Coordinates
                    InputType.MultiText -> result.inputExtra is CellValueExtra.Options
                    else -> result.inputExtra == null
                },
            )
        }
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
