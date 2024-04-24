package org.dhis2.form.ui.provider

import org.dhis2.form.model.LegendValue

interface LegendValueProvider {

    fun provideLegendValue(dataElementUid: String, value: String?): LegendValue?
}
