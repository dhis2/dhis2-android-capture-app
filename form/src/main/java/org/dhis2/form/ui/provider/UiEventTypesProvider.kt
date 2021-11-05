package org.dhis2.form.ui.provider

import org.dhis2.form.model.UiRenderType
import org.hisp.dhis.android.core.common.FeatureType
import org.hisp.dhis.android.core.common.ValueType
import org.hisp.dhis.android.core.common.ValueTypeRenderingType

interface UiEventTypesProvider {

    fun provideUiRenderType(featureType: FeatureType?): UiRenderType

    fun provideUiRenderType(valueTypeRenderingType: ValueTypeRenderingType?): UiRenderType
}
