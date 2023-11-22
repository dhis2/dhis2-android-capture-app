package org.dhis2.form.ui.provider

import org.hisp.dhis.android.core.common.ValueType

interface DisplayNameProvider {

    fun provideDisplayName(
        valueType: ValueType?,
        value: String?,
        optionSet: String? = null,
    ): String?
}
