package org.dhis2.form.ui.event

import org.dhis2.commons.date.DateUtils
import org.dhis2.commons.extensions.toDate
import org.dhis2.form.model.UiEventType
import org.dhis2.form.model.UiRenderType
import org.hisp.dhis.android.core.common.FeatureType
import org.hisp.dhis.android.core.common.ValueType
import timber.log.Timber
import java.util.Calendar

class UiEventFactoryImpl(
    val uid: String,
    val label: String,
    val valueType: ValueType,
    val allowFutureDates: Boolean?
) : UiEventFactory {
    override fun generateEvent(
        value: String?,
        uiEventType: UiEventType?,
        renderingType: UiRenderType?
    ): RecyclerViewUiEvents? {
        var uiEvent: RecyclerViewUiEvents? = null
        try {
            uiEvent = when (valueType) {
                ValueType.AGE -> {
                    uiEventType?.let {
                        when (it) {
                            UiEventType.AGE_CALENDAR -> {
                                RecyclerViewUiEvents.OpenCustomCalendar(
                                    uid = uid,
                                    label = label,
                                    date = value?.toDate(),
                                    allowFutureDates = allowFutureDates ?: false
                                )
                            }
                            UiEventType.AGE_YEAR_MONTH_DAY -> {
                                val yearMonthDay = valueToYearMonthDay(value)
                                RecyclerViewUiEvents.OpenYearMonthDayAgeCalendar(
                                    uid = uid,
                                    year = yearMonthDay[0],
                                    month = yearMonthDay[1],
                                    day = yearMonthDay[2]
                                )
                            }
                            else -> null
                        }
                    }
                }
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
                ValueType.ORGANISATION_UNIT -> RecyclerViewUiEvents.OpenOrgUnitDialog(
                    uid,
                    label,
                    value
                )
                ValueType.COORDINATE -> {
                    uiEventType?.let {
                        when (it) {
                            UiEventType.REQUEST_CURRENT_LOCATION -> {
                                RecyclerViewUiEvents.RequestCurrentLocation(
                                    uid = uid
                                )
                            }
                            UiEventType.REQUEST_LOCATION_BY_MAP -> {
                                RecyclerViewUiEvents.RequestLocationByMap(
                                    uid = uid,
                                    featureType = getFeatureType(renderingType),
                                    value = value
                                )
                            }
                            else -> null
                        }
                    }
                }
                else -> null
            }
        } catch (e: Exception) {
            Timber.d("wrong format")
        }

        return uiEvent
    }

    private fun getFeatureType(renderingType: UiRenderType?): FeatureType {
        return when (renderingType) {
            UiRenderType.DEFAULT -> FeatureType.NONE
            UiRenderType.POINT -> FeatureType.POINT
            UiRenderType.POLYGON -> FeatureType.POLYGON
            UiRenderType.MULTI_POLYGON -> FeatureType.MULTI_POLYGON
            null -> FeatureType.NONE
        }
    }

    private fun valueToYearMonthDay(value: String?) = value?.toDate()?.let {
        Calendar.getInstance().time = it
        DateUtils.getDifference(it, Calendar.getInstance().time)
    } ?: intArrayOf(0, 0, 0)
}
