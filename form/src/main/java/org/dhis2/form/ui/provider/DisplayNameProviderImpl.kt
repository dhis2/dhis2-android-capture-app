package org.dhis2.form.ui.provider

import org.dhis2.commons.date.DateUtils
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.common.ValueType

class DisplayNameProviderImpl(val d2: D2) : DisplayNameProvider {

    override fun provideDisplayName(
        valueType: ValueType?,
        value: String?,
        optionSet: String?
    ): String? {
        return value?.let {
            when (valueType) {
                ValueType.ORGANISATION_UNIT ->
                    d2.organisationUnitModule()
                        .organisationUnits()
                        .uid(value)
                        .blockingGet()
                        .displayName()

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
                ValueType.TEXT -> optionSet?.let {
                    val byCode = d2.optionModule().options()
                        .byOptionSetUid().eq(optionSet)
                        .byCode().eq(value).one()
                    val byName = d2.optionModule().options()
                        .byOptionSetUid().eq(optionSet)
                        .byDisplayName().eq(value).one()
                    when {
                        byCode.blockingExists() -> return byCode.blockingGet().displayName()
                        byName.blockingExists() -> return byName.blockingGet().displayName()
                        else -> value
                    }
                }
                else -> value
            }
        }
    }
}
