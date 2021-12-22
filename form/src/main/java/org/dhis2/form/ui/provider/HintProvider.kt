package org.dhis2.form.ui.provider

import org.hisp.dhis.android.core.common.ValueType

interface HintProvider {

    fun provideDateHint(valueType: ValueType): String
}
