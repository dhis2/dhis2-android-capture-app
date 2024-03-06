package org.dhis2.form.ui.provider

import org.dhis2.commons.date.DateUtils
import org.dhis2.form.data.metadata.FileResourceConfiguration
import org.dhis2.form.data.metadata.OptionSetConfiguration
import org.dhis2.form.data.metadata.OrgUnitConfiguration
import org.hisp.dhis.android.core.common.ValueType

class DisplayNameProviderImpl(
    private val optionSetConfiguration: OptionSetConfiguration,
    private val orgUnitConfiguration: OrgUnitConfiguration,
    private val fileResourceConfiguration: FileResourceConfiguration
) : DisplayNameProvider {

    override fun provideDisplayName(
        valueType: ValueType?,
        value: String?,
        optionSet: String?
    ): String? {
        return value?.let {
            optionSet?.let { optionSetUid ->
                return getOptionSetValue(value, optionSetUid)
            } ?: getValueTypeValue(value, valueType)
        }
    }

    private fun getOptionSetValue(value: String, optionSet: String): String {
        return optionSetConfiguration.optionInDataSetByCode(optionSet, value)?.displayName()
            ?: optionSetConfiguration.optionInDataSetByName(optionSet, value)?.displayName()
            ?: value
    }

    private fun getValueTypeValue(value: String, valueType: ValueType?): String? {
        return when (valueType) {
            ValueType.ORGANISATION_UNIT ->
                orgUnitConfiguration.orgUnitByUid(value)
                    ?.displayName()
                    ?: value

            ValueType.IMAGE, ValueType.FILE_RESOURCE ->
                fileResourceConfiguration.getFilePath(value) ?: value

            ValueType.DATE ->
                DateUtils.uiDateFormat().format(
                    DateUtils.oldUiDateFormat().parse(value) ?: ""
                )

            ValueType.DATETIME ->
                DateUtils.dateTimeFormat().format(
                    DateUtils.databaseDateFormatNoSeconds().parse(value) ?: ""
                )

            ValueType.TIME ->
                DateUtils.timeFormat().format(
                    DateUtils.timeFormat().parse(value) ?: ""
                )

            else -> value
        }
    }
}
