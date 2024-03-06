package dhis2.org.analytics.charts.formatters

import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.formatter.ValueFormatter
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Date
import java.util.Locale

const val EMPTY_LABEL = "-"
const val BLANK_LABEL = ""

class DateLabelFormatter(
    val datePattern: String,
    val dateFromValue: (Long) -> Date?,
    val localDateFromValue: ((Long) -> LocalDate)? = null
) : ValueFormatter() {

    private val monthFormat = SimpleDateFormat("MMM yyyy", Locale.getDefault())
    private var prevCalendar: Calendar? = null
    override fun getAxisLabel(value: Float, axis: AxisBase?): String {
        return localDateFromValue?.invoke(value.toLong())?.format(
            DateTimeFormatter.ofPattern("dd MMM yyyy")
        )
            ?: kotlin.run {
                val labelDate = dateFromValue(value.toLong())

                return labelDate?.let {
                    val calendar = Calendar.getInstance()
                    calendar.timeInMillis = it.time

                    if (shouldFormatLabel(calendar)) {
                        prevCalendar = calendar
                        monthFormat.format(calendar.time)
                    } else {
                        ""
                    }
                } ?: EMPTY_LABEL
            }
    }

    private fun shouldFormatLabel(calendar: Calendar) =
        prevCalendar == null || prevCalendar?.get(Calendar.MONTH) != calendar[Calendar.MONTH]
}
