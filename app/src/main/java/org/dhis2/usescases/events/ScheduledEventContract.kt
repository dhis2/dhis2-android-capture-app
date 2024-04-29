package org.dhis2.usescases.events

import org.dhis2.usescases.eventsWithoutRegistration.eventDetails.providers.InputDateValues
import org.dhis2.usescases.general.AbstractActivityContracts
import org.hisp.dhis.android.core.category.CategoryOption
import org.hisp.dhis.android.core.enrollment.Enrollment
import org.hisp.dhis.android.core.event.Event
import org.hisp.dhis.android.core.program.Program
import org.hisp.dhis.android.core.program.ProgramStage
import org.hisp.dhis.mobile.ui.designsystem.component.SelectableDates
import java.util.Date

class ScheduledEventContract {

    interface View : AbstractActivityContracts.View {
        fun setEvent(event: Event)
        fun setStage(programStage: ProgramStage, event: Event)
        fun setProgram(program: Program)
        fun openFormActivity()
    }

    interface Presenter {
        fun init()
        fun finish()
        fun setEventDate(date: Date)
        fun formatDateValues(date: InputDateValues): Date
        fun setDueDate(date: Date)
        fun getDateFormatConfiguration(): String?
        fun skipEvent()
        fun setCatOptionCombo(catComboUid: String, arrayList: ArrayList<CategoryOption>)
        fun onBackClick()
        fun getEventTei(): String
        fun getEnrollment(): Enrollment?
        fun getSelectableDates(program: Program, isDueDate: Boolean): SelectableDates?
    }
}
