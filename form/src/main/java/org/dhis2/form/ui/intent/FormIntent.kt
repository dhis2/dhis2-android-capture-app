package org.dhis2.form.ui.intent

import org.dhis2.form.mvi.MviIntent

sealed class FormIntent : MviIntent {
    data class SelectDateFromAgeCalendar(val uid: String, val date: String?) : FormIntent()
    data class ClearDateFromAgeCalendar(val uid: String) : FormIntent()
}
