package org.dhis2.form.ui.provider

import org.dhis2.form.model.UiEventType
import org.dhis2.form.model.UiRenderType
import org.hisp.dhis.android.core.common.FeatureType
import org.hisp.dhis.android.core.common.ValueType

interface UiEventTypesProvider {

    fun provideUiEvents(valueType: ValueType): List<UiEventType>

    fun provideUiRenderType(featureType: FeatureType?): UiRenderType
}
