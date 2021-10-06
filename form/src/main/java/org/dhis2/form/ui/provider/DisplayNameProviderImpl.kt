package org.dhis2.form.ui.provider

import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.common.ValueType

class DisplayNameProviderImpl(val d2: D2) : DisplayNameProvider {

    override fun provideDisplayName(valueType: ValueType?, value: String?): String? {
        return when (valueType) {
            ValueType.ORGANISATION_UNIT -> value?.let {
                d2.organisationUnitModule()
                    .organisationUnits()
                    .uid(value)
                    .blockingGet()
                    .displayName()
            }
            else -> value
        }
    }
}
