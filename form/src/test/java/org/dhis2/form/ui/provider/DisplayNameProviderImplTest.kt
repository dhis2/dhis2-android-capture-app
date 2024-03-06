package org.dhis2.form.ui.provider

import org.dhis2.form.data.metadata.FileResourceConfiguration
import org.dhis2.form.data.metadata.OptionSetConfiguration
import org.dhis2.form.data.metadata.OrgUnitConfiguration
import org.hisp.dhis.android.core.common.ValueType
import org.hisp.dhis.android.core.option.Option
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class DisplayNameProviderImplTest {

    private val optionSetConfiguration: OptionSetConfiguration = mock()
    private val orgUnitConfiguration: OrgUnitConfiguration = mock()
    private val fileResourceConfiguration: FileResourceConfiguration = mock()
    private val displayNameProvider =
        DisplayNameProviderImpl(
            optionSetConfiguration,
            orgUnitConfiguration,
            fileResourceConfiguration
        )

    @Test
    fun `Should return null if value is null`() {
        val result =
            displayNameProvider.provideDisplayName(ValueType.ORGANISATION_UNIT, null)
        assertTrue(result == null)
    }

    @Test
    fun `Should return option name if code exist`() {
        val testingValue = "value"
        val testingOptionSetUid = "optionSet"
        val testingOptionName = "valueName"
        mockOptionSetByCode(testingValue, testingOptionSetUid, testingOptionName, true)
        mockOptionSetByName(testingValue, testingOptionSetUid, testingOptionName, false)

        val result = displayNameProvider.provideDisplayName(
            ValueType.INTEGER,
            testingValue,
            testingOptionSetUid
        )

        assertTrue(result == testingOptionName)
    }

    @Test
    fun `Should return option name if name exist`() {
        val testingValue = "value"
        val testingOptionSetUid = "optionSet"
        val testingOptionName = "valueName"
        mockOptionSetByCode(testingValue, testingOptionSetUid, testingOptionName, false)
        mockOptionSetByName(testingValue, testingOptionSetUid, testingOptionName, true)

        val result = displayNameProvider.provideDisplayName(
            ValueType.INTEGER,
            testingValue,
            testingOptionSetUid
        )

        assertTrue(result == testingOptionName)
    }

    @Test
    fun `Should return value if name and code do not exist`() {
        val testingValue = "value"
        val testingOptionSetUid = "optionSet"
        val testingOptionName = "valueName"
        mockOptionSetByCode(testingValue, testingOptionSetUid, testingOptionName, false)
        mockOptionSetByName(testingValue, testingOptionSetUid, testingOptionName, false)

        val result = displayNameProvider.provideDisplayName(
            ValueType.INTEGER,
            testingValue,
            testingOptionSetUid
        )

        assertTrue(result == "value")
    }

    @Test
    fun `Should return org unit name`() {
        val testingOrgUnitUid = "orgUnitUid"
        val testingOrgUnitName = "orgUnitName"
        whenever(
            orgUnitConfiguration.orgUnitByUid(testingOrgUnitUid)
        ) doReturn OrganisationUnit.builder()
            .uid(testingOrgUnitUid)
            .displayName(testingOrgUnitName)
            .build()
        val result =
            displayNameProvider.provideDisplayName(ValueType.ORGANISATION_UNIT, testingOrgUnitUid)
        assertTrue(result == testingOrgUnitName)
    }

    @Test
    fun `Should return value if org unit is not found`() {
        val testingOrgUnitUid = "orgUnitUid"

        whenever(
            orgUnitConfiguration.orgUnitByUid(testingOrgUnitUid)
        ) doReturn null
        val result =
            displayNameProvider.provideDisplayName(ValueType.ORGANISATION_UNIT, testingOrgUnitUid)
        assertTrue(result == testingOrgUnitUid)
    }

    @Test
    fun `Should return file path for valueType IMAGE when value is the file path`() {
        val value = "filePath"
        whenever(fileResourceConfiguration.getFilePath(value)) doReturn null
        val result = displayNameProvider.provideDisplayName(
            valueType = ValueType.IMAGE,
            value = value,
            optionSet = null
        )
        assertEquals(value, result)
    }

    @Test
    fun `Should return file path for valueType IMAGE when value is the file uid`() {
        val filePath = "filePath"
        val value = "uid"
        whenever(fileResourceConfiguration.getFilePath(value)) doReturn filePath
        val result = displayNameProvider.provideDisplayName(
            valueType = ValueType.IMAGE,
            value = value,
            optionSet = null
        )
        assertEquals(filePath, result)
    }

    private fun mockOptionSetByCode(
        value: String,
        optionSetUid: String,
        optionName: String,
        exists: Boolean
    ) {
        whenever(
            optionSetConfiguration.optionInDataSetByCode(optionSetUid, value)
        ) doReturn if (exists) {
            Option.builder()
                .uid("optionUid")
                .displayName(optionName)
                .build()
        } else {
            null
        }
    }

    private fun mockOptionSetByName(
        value: String,
        optionSetUid: String,
        optionName: String,
        exists: Boolean
    ) {
        whenever(
            optionSetConfiguration.optionInDataSetByName(optionSetUid, value)
        ) doReturn if (exists) {
            Option.builder()
                .uid("optionUid")
                .displayName(optionName)
                .build()
        } else {
            null
        }
    }
}
