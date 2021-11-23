package org.dhis2.form.ui.event

import org.dhis2.commons.date.DateUtils
import org.hisp.dhis.android.core.common.ValueType
import timber.log.Timber

class UiEventFactoryImpl(
    val uid: String,
    val label: String,
    val valueType: ValueType,
    val allowFutureDates: Boolean?
) : UiEventFactory {
    override fun generateEvent(
        value: String?
    ): RecyclerViewUiEvents? {
        var uiEvent: RecyclerViewUiEvents? = null
        try {
            uiEvent = when (valueType) {
                ValueType.DATE -> RecyclerViewUiEvents.OpenCustomCalendar(
                    uid,
                    label,
                    value?.let { DateUtils.oldUiDateFormat().parse(it) },
                    allowFutureDates ?: true
                )
                ValueType.DATETIME -> RecyclerViewUiEvents.OpenCustomCalendar(
                    uid,
                    label,
                    value?.let { DateUtils.databaseDateFormatNoSeconds().parse(it) },
                    allowFutureDates ?: true,
                    isDateTime = true
                )
                ValueType.TIME -> RecyclerViewUiEvents.OpenTimePicker(
                    uid,
                    label,
                    value?.let { DateUtils.timeFormat().parse(it) }
                )
                else -> null
            }
        } catch (e: Exception) {
            Timber.d("wrong format")
        }

        return uiEvent
    }
}
