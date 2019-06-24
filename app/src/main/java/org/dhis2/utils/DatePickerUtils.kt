package org.dhis2.utils

import android.app.Dialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.DatePicker
import androidx.appcompat.app.AlertDialog
import org.dhis2.R
import org.dhis2.databinding.WidgetDatepickerBinding
import java.util.*

class DatePickerUtils {
    companion object {
        fun getDatePickerDialog(context: Context,
                                buttonListener: OnDatePickerClickListener): Dialog {

            val c = Calendar.getInstance()

            return buildDialog(context, c, null, true, buttonListener)
        }

        fun getDatePickerDialog(context: Context,
                                title: String?,
                                currentDate: Date?,
                                allowFutureDates: Boolean,
                                buttonListener: OnDatePickerClickListener): Dialog {


            val c = Calendar.getInstance()
            if (currentDate != null)
                c.time = currentDate


            return buildDialog(context, c, title, allowFutureDates, buttonListener)
        }

        fun getDatePickerDialog(context: Context,
                                title: String?,
                                currentDate: Date?,
                                futureOnly: Boolean,
                                maxDate: Date,
                                minDate: Date,
                                buttonListener: OnDatePickerClickListener): Dialog {


            val c = Calendar.getInstance()
            if (currentDate != null)
                c.time = currentDate
            if (futureOnly)
                c.add(Calendar.DAY_OF_YEAR, 1)

            return buildDialog(context, c, title, true, buttonListener)
        }

        private fun buildDialog(context: Context, c: Calendar, title: String?,
                                allowFutureDates: Boolean,
                                buttonListener: OnDatePickerClickListener): Dialog {
            val layoutInflater = LayoutInflater.from(context)
            val widgetBinding = WidgetDatepickerBinding.inflate(layoutInflater)
            val datePicker = widgetBinding.widgetDatepicker
            val calendarPicker = widgetBinding.widgetDatepickerCalendar

            datePicker.updateDate(
                    c.get(Calendar.YEAR),
                    c.get(Calendar.MONTH),
                    c.get(Calendar.DAY_OF_MONTH))

            calendarPicker.updateDate(
                    c.get(Calendar.YEAR),
                    c.get(Calendar.MONTH),
                    c.get(Calendar.DAY_OF_MONTH))

            if (!allowFutureDates) {
                datePicker.maxDate = System.currentTimeMillis()
                calendarPicker.maxDate = System.currentTimeMillis()
            }

            val alertDialog = AlertDialog.Builder(context, R.style.DatePickerTheme)
            if (title != null)
                alertDialog.setTitle(title)

            alertDialog.setView(widgetBinding.root)
            val dialog = alertDialog.create()

            widgetBinding.changeCalendarButton.setOnClickListener { calendarButton ->
                datePicker.visibility = if (datePicker.visibility == View.VISIBLE) View.GONE else View.VISIBLE
                calendarPicker.visibility = if (datePicker.visibility == View.VISIBLE) View.GONE else View.VISIBLE
            }
            widgetBinding.clearButton.setOnClickListener { clearButton ->
                buttonListener.onNegativeClick()
                dialog.dismiss()
            }
            widgetBinding.acceptButton.setOnClickListener { acceptButton ->
                buttonListener.onPositiveClick(if (datePicker.visibility == View.VISIBLE) datePicker else calendarPicker)
                dialog.dismiss()
            }

            return dialog
        }

    }


}

public interface OnDatePickerClickListener {
    fun onNegativeClick()

    fun onPositiveClick(datePicker: DatePicker)
}
