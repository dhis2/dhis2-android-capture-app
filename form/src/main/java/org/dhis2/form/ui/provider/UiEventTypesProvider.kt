package org.dhis2.form.ui.provider

import org.dhis2.form.model.UiEventType
import org.hisp.dhis.android.core.common.ValueType

interface UiEventTypesProvider {

    fun provideUiEvents(valueType: ValueType): List<UiEventType>
}
