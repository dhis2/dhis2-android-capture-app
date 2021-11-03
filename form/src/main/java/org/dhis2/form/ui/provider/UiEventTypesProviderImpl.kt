package org.dhis2.form.ui.provider

import org.dhis2.form.model.UiEventType
import org.dhis2.form.model.UiRenderType
import org.hisp.dhis.android.core.common.FeatureType
import org.hisp.dhis.android.core.common.ValueType
import org.hisp.dhis.android.core.common.ValueTypeRenderingType

class UiEventTypesProviderImpl : UiEventTypesProvider {

    override fun provideUiEvents(valueType: ValueType): List<UiEventType> {
        return when (valueType) {
            ValueType.DATE,
            ValueType.DATETIME,
            ValueType.TIME -> listOf(UiEventType.DATE_TIME)
            ValueType.ORGANISATION_UNIT -> listOf(UiEventType.ORG_UNIT)
            ValueType.AGE -> listOf(UiEventType.AGE_CALENDAR, UiEventType.AGE_YEAR_MONTH_DAY)
            ValueType.COORDINATE -> listOf(
                UiEventType.REQUEST_CURRENT_LOCATION,
                UiEventType.REQUEST_LOCATION_BY_MAP
            )
            ValueType.IMAGE -> listOf(UiEventType.SHOW_PICTURE, UiEventType.ADD_PICTURE)
            else -> emptyList()
        }
    }

    override fun provideUiRenderType(featureType: FeatureType?): UiRenderType {
        return when (featureType) {
            FeatureType.POINT -> UiRenderType.POINT
            FeatureType.POLYGON -> UiRenderType.POLYGON
            FeatureType.MULTI_POLYGON -> UiRenderType.MULTI_POLYGON
            else -> UiRenderType.DEFAULT
        }
    }

    override fun provideUiRenderType(
        valueTypeRenderingType: ValueTypeRenderingType?
    ): UiRenderType {
        return when (valueTypeRenderingType) {
            ValueTypeRenderingType.VERTICAL_RADIOBUTTONS -> UiRenderType.VERTICAL_RADIOBUTTONS
            ValueTypeRenderingType.HORIZONTAL_RADIOBUTTONS -> UiRenderType.HORIZONTAL_RADIOBUTTONS
            ValueTypeRenderingType.VERTICAL_CHECKBOXES -> UiRenderType.VERTICAL_CHECKBOXES
            ValueTypeRenderingType.HORIZONTAL_CHECKBOXES -> UiRenderType.HORIZONTAL_CHECKBOXES
            else -> UiRenderType.DEFAULT
        }
    }
}
