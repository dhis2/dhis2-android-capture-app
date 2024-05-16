package org.dhis2.commons.dialogs.calendarpicker

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.widget.DatePicker
import android.widget.LinearLayout
import androidx.databinding.BindingAdapter
import org.dhis2.commons.R
import org.dhis2.commons.databinding.CalendarPickerViewBinding
import org.dhis2.commons.dialogs.calendarpicker.di.CalendarPickerComponentProvider
import java.util.Calendar
import java.util.Date
import javax.inject.Inject

class CalendarPicker(
    context: Context,
) : Dialog(context) {

    @Inject
    lateinit var repository: CalendarPickerRepository

    private var listener: OnDatePickerListener? = null

    private val binding: CalendarPickerViewBinding =
        CalendarPickerViewBinding.inflate(LayoutInflater.from(context))
    private val calendar: Calendar = Calendar.getInstance()
    private val datePicker: DatePicker = binding.datePicker
    private val calendarPicker: DatePicker = binding.datePickerCalendar

    private var title: String? = null
    private var initialDate: Date? = null
    private var minDate: Date? = null
    private var maxDate: Date? = null
    private var isFutureDatesAllowed: Boolean = false
    private var isFromOtherPeriods: Boolean = false
    private var scheduleInterval: Int? = null

    init {
        (context.applicationContext as CalendarPickerComponentProvider)
            .provideCalendarPickerComponent()?.inject(this)
    }

    fun setInitialDate(date: Date?) {
        initialDate = date
    }

    fun setMinDate(date: Date?) {
        minDate = date
    }

    fun setMaxDate(date: Date?) {
        maxDate = date
    }

    fun isFutureDatesAllowed(futureDatesAllowed: Boolean) {
        isFutureDatesAllowed = futureDatesAllowed
    }

    fun isFromOtherPeriods(fromOtherPeriods: Boolean) {
        isFromOtherPeriods = fromOtherPeriods
    }

    fun setScheduleInterval(days: Int) {
        scheduleInterval = days
    }

    fun setTitle(title: String?) {
        this.title = title
    }

    fun setListener(listener: OnDatePickerListener) {
        this.listener = listener
    }

    override fun show() {
        if (listener == null) {
            throw IllegalArgumentException("Listener must be set up")
        }
        setCalendar()
        datePickerVisibility(repository.isDatePickerStyle())
        setFuturesDates()
        setMinDates()
        setMaxDates()

        val dialog = dialogBuilder().create()

        binding.changeCalendarBtn.setOnClickListener {
            repository.setPickerStyle(repository.isDatePickerStyle())
            datePickerVisibility(repository.isDatePickerStyle())
        }

        binding.clearBtn.setOnClickListener {
            listener?.onNegativeClick()
            dialog.dismiss()
        }

        binding.acceptBtn.setOnClickListener {
            listener?.onPositiveClick(
                if (datePicker.visibility == View.VISIBLE) {
                    datePicker
                } else {
                    calendarPicker
                },
            )
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun dialogBuilder(): AlertDialog.Builder {
        val builder = AlertDialog.Builder(context, R.style.DatePickerTheme)
        title?.let { builder.setTitle(it) }
        if (isFromOtherPeriods) {
            binding.clearBtn.text = context.getString(R.string.next)
        }
        builder.setView(binding.root)

        return builder
    }

    private fun setCalendar() {
        initialDate?.let { calendar.time = it }
        scheduleInterval?.let {
            if (it > 0) {
                calendar.add(Calendar.DAY_OF_YEAR, it)
            }
        }

        datePicker.updateDate(
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH),
        )

        calendarPicker.updateDate(
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH),
        )
    }

    private fun datePickerVisibility(isDatePicker: Boolean) {
        if (isDatePicker) {
            datePicker.visibility = View.VISIBLE
            calendarPicker.visibility = View.GONE
        } else {
            datePicker.visibility = View.GONE
            calendarPicker.visibility = View.VISIBLE
        }
    }

    private fun setFuturesDates() {
        if (!isFutureDatesAllowed) {
            datePicker.maxDate = System.currentTimeMillis()
            calendarPicker.maxDate = System.currentTimeMillis()
        }
    }

    private fun setMinDates() {
        minDate?.let {
            datePicker.minDate = it.time
            calendarPicker.minDate = it.time
        }
    }

    private fun setMaxDates() {
        maxDate?.let {
            datePicker.maxDate = it.time
            calendarPicker.maxDate = it.time
        }
    }
}

@BindingAdapter("versionCustomVisibility")
fun setCustomVisibility(linearLayout: LinearLayout, check: Boolean) {
    if (check && Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
        linearLayout.visibility = View.GONE
    }
}
