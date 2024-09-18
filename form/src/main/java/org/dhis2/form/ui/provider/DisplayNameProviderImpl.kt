package org.dhis2.form.ui.provider

import org.dhis2.commons.extensions.toDate
import org.dhis2.commons.resources.DhisPeriodUtils
import org.dhis2.form.data.metadata.FileResourceConfiguration
import org.dhis2.form.data.metadata.OptionSetConfiguration
import org.dhis2.form.data.metadata.OrgUnitConfiguration
import org.hisp.dhis.android.core.common.ValueType
import org.hisp.dhis.android.core.period.PeriodType
import java.util.Locale

class DisplayNameProviderImpl(
    private val optionSetConfiguration: OptionSetConfiguration,
    private val orgUnitConfiguration: OrgUnitConfiguration,
    private val fileResourceConfiguration: FileResourceConfiguration,
    private val periodUtils: DhisPeriodUtils,
) : DisplayNameProvider {

    override fun provideDisplayName(
        valueType: ValueType?,
        value: String?,
        optionSet: String?,
        periodType: PeriodType?,
    ): String? {
        return value?.let {
            optionSet?.let { optionSetUid ->
                return getOptionSetValue(value, optionSetUid)
            } ?: getValueTypeValue(value, valueType, periodType)
        }
    }

    private fun getOptionSetValue(value: String, optionSet: String): String {
        return optionSetConfiguration.optionInDataSetByCode(optionSet, value)?.displayName()
            ?: optionSetConfiguration.optionInDataSetByName(optionSet, value)?.displayName()
            ?: value
    }

    private fun getValueTypeValue(
        value: String,
        valueType: ValueType?,
        periodType: PeriodType?,
    ): String {
        return when (valueType) {
            ValueType.ORGANISATION_UNIT ->
                orgUnitConfiguration.orgUnitByUid(value)
                    ?.displayName()
                    ?: value

            ValueType.IMAGE, ValueType.FILE_RESOURCE ->
                fileResourceConfiguration.getFilePath(value) ?: value

            ValueType.DATE -> {
                value.toDate()?.let { date ->
                    periodType?.let {
                        periodUtils.getPeriodUIString(it, date, Locale.getDefault())
                    } ?: value
                } ?: value
            }

            else -> value
        }
    }
}
