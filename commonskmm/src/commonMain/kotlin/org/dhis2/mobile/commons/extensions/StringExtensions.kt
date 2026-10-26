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
import org.koin.mp.KoinPlatform.getKoin

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
                    .replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
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
