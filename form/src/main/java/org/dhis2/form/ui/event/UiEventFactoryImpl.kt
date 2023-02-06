package org.dhis2.form.ui.event

import android.content.Intent
import java.util.Calendar
import org.dhis2.commons.date.DateUtils
import org.dhis2.commons.extensions.toDate
import org.dhis2.form.model.FieldUiModel
import org.dhis2.form.model.UiEventType
import org.dhis2.form.model.UiEventType.ADD_FILE
import org.dhis2.form.model.UiEventType.ADD_PICTURE
import org.dhis2.form.model.UiEventType.ADD_SIGNATURE
import org.dhis2.form.model.UiEventType.AGE_CALENDAR
import org.dhis2.form.model.UiEventType.AGE_YEAR_MONTH_DAY
import org.dhis2.form.model.UiEventType.COPY_TO_CLIPBOARD
import org.dhis2.form.model.UiEventType.DATE_TIME
import org.dhis2.form.model.UiEventType.EMAIL
import org.dhis2.form.model.UiEventType.OPEN_FILE
import org.dhis2.form.model.UiEventType.OPTION_SET
import org.dhis2.form.model.UiEventType.ORG_UNIT
import org.dhis2.form.model.UiEventType.PHONE_NUMBER
import org.dhis2.form.model.UiEventType.QR_CODE
import org.dhis2.form.model.UiEventType.REQUEST_CURRENT_LOCATION
import org.dhis2.form.model.UiEventType.REQUEST_LOCATION_BY_MAP
import org.dhis2.form.model.UiEventType.SHOW_DESCRIPTION
import org.dhis2.form.model.UiEventType.SHOW_PICTURE
import org.dhis2.form.model.UiRenderType
import org.hisp.dhis.android.core.common.FeatureType
import org.hisp.dhis.android.core.common.ValueType
import timber.log.Timber

class UiEventFactoryImpl(
    val uid: String,
    val label: String,
    val description: String?,
    val valueType: ValueType,
    val allowFutureDates: Boolean?,
    val optionSet: String?
) : UiEventFactory {
    override fun generateEvent(
        value: String?,
        uiEventType: UiEventType?,
        renderingType: UiRenderType?,
        fieldUiModel: FieldUiModel
    ): RecyclerViewUiEvents? {
        var uiEvent: RecyclerViewUiEvents? = null
        try {
            uiEvent = when (uiEventType) {
                DATE_TIME -> {
                    when (valueType) {
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
                }
                AGE_CALENDAR -> RecyclerViewUiEvents.OpenCustomCalendar(
                    uid = uid,
                    label = label,
                    date = value?.toDate(),
                    allowFutureDates = allowFutureDates ?: false
                )
                AGE_YEAR_MONTH_DAY -> {
                    val yearMonthDay = valueToYearMonthDay(value)
                    RecyclerViewUiEvents.OpenYearMonthDayAgeCalendar(
                        uid = uid,
                        year = yearMonthDay[0],
                        month = yearMonthDay[1],
                        day = yearMonthDay[2]
                    )
                }
                ORG_UNIT -> RecyclerViewUiEvents.OpenOrgUnitDialog(
                    uid,
                    label,
                    value
                )
                REQUEST_CURRENT_LOCATION -> RecyclerViewUiEvents.RequestCurrentLocation(
                    uid = uid
                )
                REQUEST_LOCATION_BY_MAP -> RecyclerViewUiEvents.RequestLocationByMap(
                    uid = uid,
                    featureType = getFeatureType(renderingType),
                    value = value
                )
                SHOW_DESCRIPTION -> RecyclerViewUiEvents.ShowDescriptionLabelDialog(
                    title = label,
                    message = description
                )
                ADD_PICTURE -> RecyclerViewUiEvents.AddImage(uid)
                SHOW_PICTURE -> RecyclerViewUiEvents.ShowImage(
                    label,
                    value ?: ""
                )
                COPY_TO_CLIPBOARD -> RecyclerViewUiEvents.CopyToClipboard(
                    value = value
                )
                QR_CODE -> {
                    if (value.isNullOrEmpty() && fieldUiModel.editable) {
                        RecyclerViewUiEvents.ScanQRCode(
                            uid = uid,
                            optionSet = optionSet,
                            renderingType = renderingType
                        )
                    } else if (value != null) {
                        RecyclerViewUiEvents.DisplayQRCode(
                            uid = uid,
                            optionSet = optionSet,
                            value = value,
                            renderingType = renderingType,
                            editable = fieldUiModel.editable
                        )
                    } else {
                        null
                    }
                }
                OPTION_SET -> RecyclerViewUiEvents.OpenOptionSetDialog(fieldUiModel)
                ADD_SIGNATURE -> RecyclerViewUiEvents.AddSignature(uid, label)
                ADD_FILE -> RecyclerViewUiEvents.OpenFileSelector(fieldUiModel)
                OPEN_FILE -> RecyclerViewUiEvents.OpenFile(fieldUiModel)
                EMAIL -> RecyclerViewUiEvents.OpenChooserIntent(Intent.ACTION_SENDTO, value)
                PHONE_NUMBER -> RecyclerViewUiEvents.OpenChooserIntent(Intent.ACTION_DIAL, value)
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
            else -> FeatureType.NONE
        }
    }

    private fun valueToYearMonthDay(value: String?) = value?.toDate()?.let {
        Calendar.getInstance().time = it
        DateUtils.getDifference(it, Calendar.getInstance().time)
    } ?: intArrayOf(0, 0, 0)
}
