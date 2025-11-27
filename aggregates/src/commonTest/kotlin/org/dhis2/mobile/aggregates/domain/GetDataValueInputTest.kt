package org.dhis2.mobile.aggregates.domain

import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.dhis2.mobile.aggregates.data.DataSetInstanceRepository
import org.dhis2.mobile.aggregates.data.OptionRepository
import org.dhis2.mobile.aggregates.model.CellValueExtra
import org.dhis2.mobile.aggregates.model.DataElementInfo
import org.dhis2.mobile.aggregates.onRunBlocking
import org.dhis2.mobile.commons.data.ValueParser
import org.dhis2.mobile.commons.di.commonsModule
import org.dhis2.mobile.commons.input.InputType
import org.dhis2.mobile.commons.model.internal.ValueInfo
import org.junit.Rule
import org.junit.Test
import org.koin.test.KoinTest
import org.koin.test.KoinTestRule
import org.koin.test.mock.MockProviderRule
import org.koin.test.mock.declareMock
import org.mockito.Mockito
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import kotlin.test.assertTrue

internal class GetDataValueInputTest : KoinTest {
    @get:Rule
    val koinTestRule =
        KoinTestRule.create {
            modules(commonsModule)
        }

    @get:Rule
    val mockProvider =
        MockProviderRule.create { clazz ->
            Mockito.mock(clazz.java)
        }

    private val mockedDataElementInfo =
        mock<DataElementInfo> {
            on { label } doReturn "DataElementLabel"
            on { description } doReturn "DataElementDescription"
            on { isRequired } doReturn true
        }
    private val repository =
        mock<DataSetInstanceRepository> {
            onRunBlocking { dataElementInfo(any(), any(), any()) } doReturn mockedDataElementInfo
            onRunBlocking { value(any(), any(), any(), any(), any()) } doReturn "Current value"
            onRunBlocking { conflicts(any(), any(), any(), any(), any(), any()) } doReturn
                Pair(
                    emptyList(),
                    emptyList(),
                )
            onRunBlocking { getCoordinatesFrom(any()) } doReturn Pair(0.0, 0.0)
        }

    private val optionRepository: OptionRepository = mock()

    val getDataValueInputTest =
        GetDataValueInput(
            dataSetUid = "dataSetUid",
            periodId = "periodId",
            orgUnitUid = "orgUnitUid",
            attrOptionComboUid = "attrOptionComboUid",
            repository = repository,
            optionRepository = optionRepository,
        )

    @Test
    fun `should return cell info`() =
        runTest {
            declareMock<ValueParser> {
                whenever(
                    runBlocking { getValueInfo("dataElementId", "Current value") },
                ) doReturn
                    ValueInfo(
                        optionSetUid = "optionSetUid",
                        valueIsValidOption = true,
                        isMultiText = false,
                        isOrganisationUnit = false,
                        isFile = false,
                        isDate = false,
                        isDateTime = true,
                        isTime = false,
                        isPercentage = false,
                        valueIsAValidOrgUnit = false,
                        valueIsAValidFile = false,
                        isCoordinate = false,
                        isBooleanType = false,
                    )
            }
            inputTypeValues.forEach { inputType ->
                whenever(mockedDataElementInfo.inputType) doReturn inputType
                whenever(optionRepository.optionCount(any())) doReturn 1
                whenever(optionRepository.options(any())) doReturn mock()
                val result =
                    getDataValueInputTest(
                        dataElementUid = "dataElementId",
                        categoryOptionComboUidData = Pair("catOptionComboUid", emptyList()),
                    )

                assertTrue(
                    when (result.inputType) {
                        InputType.Coordinates -> result.inputExtra is CellValueExtra.Coordinates
                        InputType.MultiText, InputType.OptionSet -> result.inputExtra is CellValueExtra.Options
                        InputType.FileResource, InputType.Image -> result.inputExtra is CellValueExtra.FileResource
                        else -> result.inputExtra == null
                    },
                )
            }
        }

    private val inputTypeValues =
        listOf(
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
