package org.dhis2.form.ui.provider

import org.dhis2.form.model.UiEventType
import org.hisp.dhis.android.core.common.ValueType

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
            ValueType.COORDINATE -> TODO()
            ValueType.PHONE_NUMBER -> TODO()
            ValueType.EMAIL -> TODO()
            ValueType.USERNAME -> TODO()
            ValueType.ORGANISATION_UNIT -> TODO()
            ValueType.TRACKER_ASSOCIATE -> TODO()
            ValueType.AGE -> listOf(UiEventType.AGE_CALENDAR, UiEventType.AGE_YEAR_MONTH_DAY)
            ValueType.URL -> TODO()
            ValueType.IMAGE -> TODO()
        }
    }
}
