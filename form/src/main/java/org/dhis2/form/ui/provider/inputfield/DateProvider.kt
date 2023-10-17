package org.dhis2.form.ui.provider.inputfield

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import org.dhis2.commons.date.DateUtils
import org.dhis2.commons.extensions.toDate
import org.dhis2.form.extensions.inputState
import org.dhis2.form.extensions.legend
import org.dhis2.form.extensions.supportingText
import org.dhis2.form.model.FieldUiModel
import org.dhis2.form.ui.event.RecyclerViewUiEvents
import org.dhis2.form.ui.intent.FormIntent
import org.hisp.dhis.android.core.common.ValueType
import org.hisp.dhis.mobile.ui.designsystem.component.DateTimeActionIconType
import org.hisp.dhis.mobile.ui.designsystem.component.InputDateTime
import org.hisp.dhis.mobile.ui.designsystem.component.internal.DateTimeTransformation
import org.hisp.dhis.mobile.ui.designsystem.component.internal.DateTransformation
import org.hisp.dhis.mobile.ui.designsystem.component.internal.TimeTransformation
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun ProvideInputDate(
    modifier: Modifier,
    fieldUiModel: FieldUiModel,
    intentHandler: (FormIntent) -> Unit,
    uiEventHandler: (RecyclerViewUiEvents) -> Unit,
) {

    val (actionType, visualTransformation) = when (fieldUiModel.valueType) {
        ValueType.DATETIME -> DateTimeActionIconType.DATE_TIME to DateTimeTransformation()
        ValueType.TIME -> DateTimeActionIconType.TIME to TimeTransformation()
        else -> DateTimeActionIconType.DATE to DateTransformation()
    }

    var value by remember(fieldUiModel.value) {
        mutableStateOf(fieldUiModel.value)
    }

    InputDateTime(
        title = fieldUiModel.label,
        value = value?.let {
            formatStoredDateToUI(it)
        },
        actionIconType = actionType,
        onActionClicked = {
            when (actionType) {
                DateTimeActionIconType.DATE -> uiEventHandler.invoke(
                    RecyclerViewUiEvents.OpenCustomCalendar(
                        uid = fieldUiModel.uid,
                        label = fieldUiModel.label,
                        date = value?.toDate(),
                        allowFutureDates = fieldUiModel.allowFutureDates ?: true,
                        isDateTime = false,
                    )
                )

                DateTimeActionIconType.TIME -> uiEventHandler.invoke(
                    RecyclerViewUiEvents.OpenTimePicker(
                        uid = fieldUiModel.uid,
                        label = fieldUiModel.label,
                        date = value?.let { DateUtils.timeFormat().parse(it) },
                        isDateTime = false,
                    )
                )

                DateTimeActionIconType.DATE_TIME -> uiEventHandler.invoke(
                    RecyclerViewUiEvents.OpenCustomCalendar(
                        uid = fieldUiModel.uid,
                        label = fieldUiModel.label,
                        date = value?.let {
                            DateUtils.databaseDateFormatNoSeconds().parse(it)
                        },
                        allowFutureDates = fieldUiModel.allowFutureDates ?: true,
                        isDateTime = true,
                    )
                )
            }
        },
        modifier = modifier,
        state = fieldUiModel.inputState(),
        legendData = fieldUiModel.legend(),
        supportingText = fieldUiModel.supportingText(),
        isRequired = fieldUiModel.mandatory,
        visualTransformation = visualTransformation,
        onFocusChanged = {},
        onValueChanged = {
            value = it
        }
    )
}

private fun formatStoredDateToUI(inputDateString: String): String? {
    val inputFormat = SimpleDateFormat(DB_FORMAT, Locale.getDefault())
    val outputFormat = SimpleDateFormat(UI_FORMAT, Locale.getDefault())

    return try {
        inputFormat.parse(inputDateString)?.let {
            outputFormat.format(it)
        }
    } catch (e: ParseException) {
        null
    }
}

private const val UI_FORMAT = "ddMMyyyy"
private const val DB_FORMAT = "yyyy-MM-dd"
