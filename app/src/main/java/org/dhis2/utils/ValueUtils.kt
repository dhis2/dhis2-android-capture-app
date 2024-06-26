package org.dhis2.utils

import org.dhis2.commons.date.DateUtils
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.common.ValueType
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeValue
import java.text.ParseException

/**
 * QUADRAM. Created by ppajuelo on 25/09/2018.
 */
class ValueUtils private constructor() {
    init {
        throw IllegalStateException("Utility class")
    }

    companion object {
        fun transform(
            d2: D2,
            attributeValue: TrackedEntityAttributeValue,
            valueType: ValueType?,
            optionSetUid: String?,
        ): TrackedEntityAttributeValue {
            val transformedValue =
                transformValue(d2, attributeValue.value(), valueType, optionSetUid)

            return if (transformedValue != attributeValue.value()) {
                attributeValue.toBuilder()
                    .value(transformedValue)
                    .build()
            } else {
                attributeValue
            }
        }

        fun transformValue(
            d2: D2,
            value: String?,
            valueType: ValueType?,
            optionSetUid: String?,
        ): String? {
            var teAttrValue = value
            when (valueType) {
                ValueType.ORGANISATION_UNIT -> {
                    if (!d2.organisationUnitModule().organisationUnits().byUid().eq(value)
                            .blockingIsEmpty()
                    ) {
                        val orgUnitName = d2.organisationUnitModule().organisationUnits()
                            .byUid().eq(value)
                            .one().blockingGet()!!.displayName()!!
                        teAttrValue = orgUnitName
                    }
                }
                ValueType.DATE, ValueType.AGE -> {
                    teAttrValue = formatDate(teAttrValue)
                }
                ValueType.DATETIME -> {
                    teAttrValue = formatDateTime(teAttrValue)
                }

                ValueType.PERCENTAGE -> {
                    teAttrValue?.let {
                        if (it.isNotEmpty()) {
                            teAttrValue = "$it %"
                        }
                    }
                }
                else -> {
                    teAttrValue = transformOptionSet(optionSetUid, d2, value)
                }
            }
            return teAttrValue
        }

        private fun transformOptionSet(optionSetUid: String?, d2: D2, value: String?): String? {
            var teAttrValue = value
            if (optionSetUid != null) {
                val optionCode = value
                if (optionCode != null) {
                    val option =
                        d2.optionModule().options().byOptionSetUid().eq(optionSetUid).byCode()
                            .eq(optionCode).one().blockingGet()
                    if (option != null && (option.code() == optionCode || option.name() == optionCode)) {
                        teAttrValue = option.displayName()
                    }
                }
            }
            return teAttrValue
        }

        private fun formatDate(value: String?): String {
            return if (value?.isNotEmpty() == true) {
                var formattedDate = ""
                val date = try {
                    DateUtils.oldUiDateFormat().parse(value)
                } catch (e: ParseException) {
                    null
                }
                date?.let {
                    formattedDate = DateUtils.uiDateFormat().format(date)
                }
                formattedDate
            } else {
                ""
            }
        }

        private fun formatDateTime(value: String?): String {
            return if (value?.isNotEmpty() == true) {
                var formattedDate = ""
                val date = try {
                    DateUtils.databaseDateFormatNoSeconds().parse(value)
                } catch (e: ParseException) {
                    null
                }
                date?.let {
                    formattedDate = DateUtils.uiDateTimeFormat().format(date)
                }
                formattedDate
            } else {
                ""
            }
        }
    }
}
