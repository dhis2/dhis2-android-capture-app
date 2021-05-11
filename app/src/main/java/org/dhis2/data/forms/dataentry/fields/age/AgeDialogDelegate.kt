package org.dhis2.data.forms.dataentry.fields.age

import org.dhis2.form.model.ActionType
import org.dhis2.form.model.RowAction
import org.dhis2.form.ui.FormViewModel
import org.dhis2.utils.DateUtils
import java.util.Calendar
import java.util.Date

class AgeDialogDelegate(val viewModel: FormViewModel) {

    fun handleDateInput(
        uid: String,
        year: Int,
        month: Int,
        day: Int
    ) {
        val currentCalendar = Calendar.getInstance()
        val ageDate = with(currentCalendar) {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month)
            set(Calendar.DAY_OF_MONTH, day)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            return@with time
        }
        createAndPushRowActionWithValue(uid, ageDate)
    }

    fun handleYearMonthDayInput(uid: String, year: Int, month: Int, day: Int) {
        val currentCalendar = Calendar.getInstance()
        val ageDate = with(currentCalendar){
            add(Calendar.YEAR, year)
            add(Calendar.MONTH, month)
            add(Calendar.DAY_OF_MONTH, day)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            return@with time
        }
        createAndPushRowActionWithValue(uid, ageDate)
    }

    private fun createAndPushRowActionWithValue(uid: String, ageDate: Date?) {
        val action = RowAction(
            uid,
            if (ageDate == null) null else DateUtils.oldUiDateFormat()
                .format(ageDate),
            false,
            null,
            null,
            null,
            null,
            ActionType.ON_SAVE
        )
        viewModel.onItemAction(action)
    }

    fun handleClearInput(uid: String) {
        val action = RowAction(
            uid,
            null,
            false,
            null,
            null,
            null,
            null,
            ActionType.ON_SAVE
        )
        viewModel.onItemAction(action)
    }
}
