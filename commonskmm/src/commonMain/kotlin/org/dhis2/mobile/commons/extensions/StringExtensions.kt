package org.dhis2.mobile.commons.extensions

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toArgb
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.format
import org.dhis2.mobile.commons.data.ValueParser
import org.dhis2.mobile.commons.dates.dateFormat
import org.dhis2.mobile.commons.dates.dateTimeFormat
import org.dhis2.mobile.commons.dates.timeFormat
import org.dhis2.mobile.commons.reporting.CrashReportController
import org.hisp.dhis.mobile.ui.designsystem.component.AgeInputType
import org.hisp.dhis.mobile.ui.designsystem.component.TimeUnitValues
import org.koin.mp.KoinPlatform.getKoin
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

expect fun String.toImageBitmap(): ImageBitmap?

suspend fun String.userFriendlyValue(
    uid: String,
    addPercentageSymbol: Boolean = true,
): String {
    return try {
        val valueParser = getKoin().get<ValueParser>()
        val valueInfo = valueParser.getValueInfo(uid, this)

        return when {
            valueInfo.isMultiText ->
                valueParser.valueFromMultiTextAsOptionNames(valueInfo.optionSetUid!!, this)

            valueInfo.parseToOptionName() ->
                valueParser.valueFromOptionSetAsOptionName(valueInfo.optionSetUid!!, this)

            valueInfo.parseToOrgUnitName() ->
                valueParser.valueFromOrgUnitAsOrgUnitName(this)

            valueInfo.parseToFileName() ->
                valueParser.valueToFileName(this)

            valueInfo.isDate -> this.toDateFormat()

            valueInfo.isDateTime -> this.toDateTimeFormat()

            valueInfo.isTime -> this.toTimeFormat()

            valueInfo.isPercentage -> {
                if (addPercentageSymbol) {
                    "$this%"
                } else {
                    this
                }
            }

            valueInfo.isBooleanType -> {
                valueParser
                    .valueFromBooleanType(this)
                    .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
            }

            valueInfo.isCoordinate ->
                valueParser.valueFromCoordinateAsLatLong(this)

            else -> this
        }
    } catch (e: IllegalStateException) {
        this
    }
}

private fun String.toDateTimeFormat(crashReportController: CrashReportController = getKoin().get<CrashReportController>()) =
    try {
        LocalDateTime.parse(this).format(dateTimeFormat)
    } catch (e: Exception) {
        crashReportController.trackError(e, e.message)
        this
    }

private fun String.toDateFormat(crashReportController: CrashReportController = getKoin().get<CrashReportController>()) =
    try {
        LocalDate.parse(this).format(dateFormat)
    } catch (e: Exception) {
        crashReportController.trackError(e, e.message)
        this
    }

private fun String.toTimeFormat(crashReportController: CrashReportController = getKoin().get<CrashReportController>()) =
    try {
        LocalTime.parse(this).format(timeFormat)
    } catch (e: Exception) {
        crashReportController.trackError(e, e.message)
        this
    }

fun String.toColor(): Color {
    val color = this.replace("#", "")
    val colorLong =
        when (color.length) {
            6 -> {
                (0xFF shl 24).toLong() or color.toLong(16)
            }

            8 -> color.toLong(16)
            else -> throw IllegalArgumentException("Unknown color: $this")
        }
    return Color(colorLong)
}

fun String.toColorInt(): Int {
    val color = toColor()
    return color.toArgb()
}

fun String.getDateFromAge(age: AgeInputType.Age): String? {
    val calendar = Calendar.getInstance()
    return try {
        when (age.unit) {
            TimeUnitValues.YEARS -> calendar.add(Calendar.YEAR, -age.value.text.toInt())
            TimeUnitValues.MONTHS -> calendar.add(Calendar.MONTH, -age.value.text.toInt())
            TimeUnitValues.DAYS -> calendar.add(Calendar.DAY_OF_MONTH, -age.value.text.toInt())
        }

        val dateFormat = SimpleDateFormat(DB_FORMAT, Locale.ENGLISH)
        dateFormat.format(calendar.time)
    } catch (e: Exception) {
        null
    }
}

fun String.hasDateFormat(format: String = DB_FORMAT): Boolean =
    try {
        val dateFormat = SimpleDateFormat(format, Locale.ENGLISH)
        dateFormat.parse(this)
        true
    } catch (e: Exception) {
        false
    }

private const val DB_FORMAT = "yyyy-MM-dd"
