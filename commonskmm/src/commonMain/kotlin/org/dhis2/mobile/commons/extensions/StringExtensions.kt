package org.dhis2.mobile.commons.extensions

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.format
import org.dhis2.mobile.commons.data.ValueParser
import org.dhis2.mobile.commons.dates.dateFormat
import org.dhis2.mobile.commons.dates.dateTimeFormat
import org.dhis2.mobile.commons.dates.timeFormat
import org.koin.mp.KoinPlatform.getKoin

suspend fun String.userFriendlyValue(
    uid: String,
    addPercentageSymbol: Boolean = true,
): String {
    return try {
        val valueParser = getKoin().get<ValueParser>()

        val valueInfo = valueParser.getValueInfo(uid, this)

        return when {
            valueInfo.parseToOptionName() ->
                valueParser.valueFromOptionSetAsOptionName(valueInfo.optionSetUid!!, this)

            valueInfo.parseToOrgUnitName() ->
                valueParser.valueFromOrgUnitAsOrgUnitName(this)

            valueInfo.parseToFilePath() ->
                valueParser.valueFromFileAsPath(this)

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

            else -> this
        }
    } catch (e: IllegalStateException) {
        this
    }
}

fun String.toDateTimeFormat() = LocalDateTime.parse(this).format(dateTimeFormat)
fun String.toDateFormat() = LocalDateTime.parse(this).format(dateFormat)
fun String.toTimeFormat() = LocalDateTime.parse(this).format(timeFormat)

fun String.toColor(): Color {
    val color = this.replace("#", "")
    val colorLong = when (color.length) {
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
