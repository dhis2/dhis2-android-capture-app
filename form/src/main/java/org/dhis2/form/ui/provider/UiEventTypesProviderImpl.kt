package org.dhis2.form.ui.provider

import org.dhis2.form.model.UiEventType
import org.dhis2.form.model.UiRenderType
import org.hisp.dhis.android.core.common.FeatureType
import org.hisp.dhis.android.core.common.ValueType
import org.hisp.dhis.android.core.common.ValueTypeRenderingType

class UiEventTypesProviderImpl : UiEventTypesProvider {

    override fun provideUiEvents(valueType: ValueType): List<UiEventType> {
        return when (valueType) {
            ValueType.TEXT -> TODO()
            ValueType.LONG_TEXT -> TODO()
            ValueType.LETTER -> TODO()
            ValueType.BOOLEAN -> TODO()
            ValueType.TRUE_ONLY -> TODO()
            ValueType.DATE,
            ValueType.DATETIME,
            ValueType.TIME -> listOf(UiEventType.DATE_TIME)
            ValueType.NUMBER -> TODO()
            ValueType.UNIT_INTERVAL -> TODO()
            ValueType.PERCENTAGE -> TODO()
            ValueType.INTEGER -> TODO()
            ValueType.INTEGER_POSITIVE -> TODO()
            ValueType.INTEGER_NEGATIVE -> TODO()
            ValueType.INTEGER_ZERO_OR_POSITIVE -> TODO()
            ValueType.FILE_RESOURCE -> TODO()
            ValueType.COORDINATE -> listOf(
                UiEventType.REQUEST_CURRENT_LOCATION,
                UiEventType.REQUEST_LOCATION_BY_MAP
            )
            ValueType.PHONE_NUMBER -> TODO()
            ValueType.EMAIL -> TODO()
            ValueType.USERNAME -> TODO()
            ValueType.ORGANISATION_UNIT -> listOf(UiEventType.ORG_UNIT)
            ValueType.TRACKER_ASSOCIATE -> TODO()
            ValueType.AGE -> listOf(UiEventType.AGE_CALENDAR, UiEventType.AGE_YEAR_MONTH_DAY)
            ValueType.URL -> TODO()
            ValueType.IMAGE -> listOf(UiEventType.SHOW_PICTURE, UiEventType.ADD_PICTURE)
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
