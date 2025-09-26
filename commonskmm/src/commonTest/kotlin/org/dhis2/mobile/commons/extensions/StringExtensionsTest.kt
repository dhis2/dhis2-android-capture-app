package org.dhis2.mobile.commons.extensions

import kotlinx.coroutines.test.runTest
import org.dhis2.mobile.commons.data.ValueParser
import org.dhis2.mobile.commons.di.commonsModule
import org.dhis2.mobile.commons.model.internal.ValueInfo
import org.dhis2.mobile.commons.reporting.CrashReportController
import org.junit.After
import org.junit.Before
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.test.KoinTest
import org.koin.test.mock.MockProvider
import org.koin.test.mock.declareMock
import org.mockito.Mockito.mock
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.doReturnConsecutively
import org.mockito.kotlin.whenever
import kotlin.test.Test
import kotlin.test.assertEquals

class StringExtensionsTest : KoinTest {
    private val dataElementUid = "dataElementUid"
    private val optionSetUid = "optionSetUid"
    private val expectedDateTime = "28/04/2024 - 23:10"
    private val expectedOptionName = "optionName"
    private val expectedOrgUnitName = "orgUnitName"
    private val expectedPath = "path"
    private val expectedPercentage = "10%"
    private val expectedValue = "Mary"
    private val expectedBooleanValue = "yes"

    @Before
    fun setUp() =
        runTest {
            startKoin {
                modules(commonsModule)
                MockProvider.register {
                    mock(it.java)
                }
            }

            val valueParser = declareMock<ValueParser>()
            val crashReportController = declareMock<CrashReportController>()

            whenever(valueParser.getValueInfo(any(), any())) doReturnConsecutively
                listOf(
                    dateTimeValueInfo,
                    optionSetValueInfo,
                    orgUnitValueInfo,
                    fileValueInfo,
                    defaultValueInfo,
                    percentageValueInfo,
                    booleanValueInfo,
                )

            whenever(valueParser.valueFromOptionSetAsOptionName(optionSetUid, "optionCode")) doReturn expectedOptionName
            whenever(valueParser.valueFromOrgUnitAsOrgUnitName("orgUnitUid")) doReturn expectedOrgUnitName
            whenever(valueParser.valueToFileName("fileUid")) doReturn expectedPath
            whenever(valueParser.valueFromBooleanType("true")) doReturn expectedBooleanValue
        }

    @After
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun getFormat() =
        runTest {
            assertEquals(
                expected = expectedDateTime,
                actual = "2024-04-28T23:10:59".userFriendlyValue(dataElementUid),
            )
            assertEquals(
                expected = expectedOptionName,
                actual = "optionCode".userFriendlyValue(dataElementUid),
            )
            assertEquals(
                expected = expectedOrgUnitName,
                actual = "orgUnitUid".userFriendlyValue(dataElementUid),
            )
            assertEquals(
                expected = expectedPath,
                actual = "fileUid".userFriendlyValue(dataElementUid),
            )
            assertEquals(
                expected = expectedValue,
                actual = "Mary".userFriendlyValue(dataElementUid),
            )
            assertEquals(
                expected = expectedPercentage,
                actual = "10".userFriendlyValue(dataElementUid),
            )
            assertEquals(
                expected = expectedBooleanValue,
                actual = "yes".userFriendlyValue(dataElementUid),
            )
        }

    private val dateTimeValueInfo =
        ValueInfo(
            isDateTime = true,
            isDate = false,
            isTime = false,
            isPercentage = false,
            isFile = false,
            isOrganisationUnit = false,
            isMultiText = false,
            optionSetUid = null,
            valueIsValidOption = false,
            valueIsAValidOrgUnit = false,
            valueIsAValidFile = false,
            isCoordinate = false,
            isBooleanType = false,
        )
    private val optionSetValueInfo =
        ValueInfo(
            isDateTime = false,
            isDate = false,
            isTime = false,
            isPercentage = false,
            isFile = false,
            isOrganisationUnit = false,
            isMultiText = false,
            optionSetUid = optionSetUid,
            valueIsValidOption = true,
            valueIsAValidOrgUnit = false,
            valueIsAValidFile = false,
            isCoordinate = false,
            isBooleanType = false,
        )
    private val orgUnitValueInfo =
        ValueInfo(
            isDateTime = false,
            isDate = false,
            isTime = false,
            isPercentage = false,
            isFile = false,
            isOrganisationUnit = true,
            isMultiText = false,
            optionSetUid = null,
            valueIsValidOption = false,
            valueIsAValidOrgUnit = true,
            valueIsAValidFile = false,
            isCoordinate = false,
            isBooleanType = false,
        )
    private val fileValueInfo =
        ValueInfo(
            isDateTime = false,
            isDate = false,
            isTime = false,
            isPercentage = false,
            isFile = true,
            isOrganisationUnit = false,
            isMultiText = false,
            optionSetUid = null,
            valueIsValidOption = false,
            valueIsAValidOrgUnit = false,
            valueIsAValidFile = true,
            isCoordinate = false,
            isBooleanType = false,
        )
    private val defaultValueInfo =
        ValueInfo(
            isDateTime = false,
            isDate = false,
            isTime = false,
            isPercentage = false,
            isFile = false,
            isOrganisationUnit = false,
            isMultiText = false,
            optionSetUid = null,
            valueIsValidOption = false,
            valueIsAValidOrgUnit = false,
            valueIsAValidFile = false,
            isCoordinate = false,
            isBooleanType = false,
        )

    private val percentageValueInfo =
        ValueInfo(
            isDateTime = false,
            isDate = false,
            isTime = false,
            isPercentage = true,
            isFile = false,
            isOrganisationUnit = false,
            isMultiText = false,
            optionSetUid = null,
            valueIsValidOption = false,
            valueIsAValidOrgUnit = false,
            valueIsAValidFile = false,
            isCoordinate = false,
            isBooleanType = false,
        )

    private val booleanValueInfo =
        ValueInfo(
            isDateTime = false,
            isDate = false,
            isTime = false,
            isPercentage = false,
            isFile = false,
            isOrganisationUnit = false,
            isMultiText = false,
            optionSetUid = null,
            valueIsValidOption = false,
            valueIsAValidOrgUnit = false,
            valueIsAValidFile = false,
            isCoordinate = false,
            isBooleanType = false,
        )
}
