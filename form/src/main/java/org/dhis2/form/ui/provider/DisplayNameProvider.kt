package org.dhis2.form.ui.provider

import org.hisp.dhis.android.core.common.ValueType
import org.hisp.dhis.android.core.period.PeriodType

interface DisplayNameProvider {
    fun provideDisplayName(
        valueType: ValueType?,
        value: String?,
        optionSet: String? = null,
        periodType: PeriodType? = null,
    ): String?
}
