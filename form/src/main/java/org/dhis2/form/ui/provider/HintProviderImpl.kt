package org.dhis2.form.ui.provider

import android.content.Context
import org.dhis2.form.R
import org.hisp.dhis.android.core.common.ValueType

class HintProviderImpl(val context: Context) : HintProvider {

    override fun provideDateHint(valueType: ValueType) =
        when (valueType) {
            ValueType.TIME -> context.getString(R.string.select_time)
            else -> context.getString(R.string.choose_date)
        }
}
