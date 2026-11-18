package org.dhis2.mobile.commons.extensions

import kotlinx.coroutines.test.runTest
import org.dhis2.mobile.commons.data.ValueParser
import org.dhis2.mobile.commons.model.internal.ValueInfo
import org.dhis2.mobile.commons.reporting.CrashReportController
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class StringExtensionsTest {
    private val dataElementUid = "dataElementUid"
    private val optionSetUid = "optionSetUid"
    private val expectedDateTime = "28/04/2024 - 23:10"
    private val expectedOptionName = "optionName"
    private val expectedOrgUnitName = "orgUnitName"
    private val expectedPath = "path"
    private val expectedPercentage = "10%"
    private val expectedValue = "Mary"
    private val expectedBooleanValue = "Yes"
    private val expectedTrueOnlyValue = "Yes"

    @BeforeTest
    fun setUp() {
        val valueParser =
            FakeValueParser(
                valueInfos =
                    listOf(
                        dateTimeValueInfo,
                        optionSetValueInfo,
                        orgUnitValueInfo,
                        fileValueInfo,
                        defaultValueInfo,
                        percentageValueInfo,
                        booleanValueInfo,
                        trueOnlyValueInfo,
                        trueOnlyValueInfo,
                    ),
                optionNames = mapOf((optionSetUid to "optionCode") to expectedOptionName),
                orgUnitNames = mapOf("orgUnitUid" to expectedOrgUnitName),
                fileNames = mapOf("fileUid" to expectedPath),
                booleanValues = mapOf("true" to "yes"),
                trueOnlyValues = mapOf("true" to "yes", "false" to ""),
            )
        startKoin {
            modules(
                module {
                    single<ValueParser> { valueParser }
                    single<CrashReportController> { NoOpCrashReportController }
                },
            )
        }
    }

    @AfterTest
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
                actual = "true".userFriendlyValue(dataElementUid),
            )
            assertEquals(
                expected = expectedTrueOnlyValue,
                actual = "true".userFriendlyValue(dataElementUid),
            )
            assertEquals(
                expected = "",
                actual = "false".userFriendlyValue(dataElementUid),
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
            isBooleanType = true,
        )

    private val trueOnlyValueInfo =
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
            isTrueOnlyType = true,
        )

    /** Answers [getValueInfo] with [valueInfos] in order, one per call. */
    private class FakeValueParser(
        valueInfos: List<ValueInfo>,
        private val optionNames: Map<Pair<String, String>, String>,
        private val orgUnitNames: Map<String, String>,
        private val fileNames: Map<String, String>,
        private val booleanValues: Map<String, String>,
        private val trueOnlyValues: Map<String, String>,
    ) : ValueParser {
        private val pendingValueInfos = ArrayDeque(valueInfos)

        override suspend fun getValueInfo(
            uid: String,
            value: String,
        ) = pendingValueInfos.removeFirst()

        override suspend fun valueFromOptionSetAsOptionName(
            optionSetUid: String,
            value: String,
        ) = optionNames.getValue(optionSetUid to value)

        override suspend fun valueFromOrgUnitAsOrgUnitName(value: String) = orgUnitNames.getValue(value)

        override suspend fun valueToFileName(value: String) = fileNames.getValue(value)

        override suspend fun valueFromBooleanType(value: String) = booleanValues.getValue(value)

        override suspend fun valueFromTrueOnlyType(value: String) = trueOnlyValues.getValue(value)

        override suspend fun valueAsOptionExists(
            optionSetUid: String,
            value: String,
        ) = error("Not used")

        override suspend fun valueAsOrgUnitExists(value: String) = error("Not used")

        override suspend fun valueAsFileExists(value: String) = error("Not used")

        override suspend fun valueFromMultiTextAsOptionNames(
            optionSetUid: String,
            value: String,
        ) = error("Not used")

        override suspend fun valueToFilePath(value: String) = error("Not used")

        override suspend fun valueFromCoordinateAsLatLong(value: String) = error("Not used")
    }

    private object NoOpCrashReportController : CrashReportController {
        override fun init() = Unit

        override fun close() = Unit

        override fun trackServer(serverDhisVersion: String?) = Unit

        override fun trackError(
            exception: Exception,
            message: String?,
        ) = Unit

        override fun addBreadCrumb(
            category: String,
            message: String,
        ) = Unit
    }
}
