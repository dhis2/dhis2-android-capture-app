package org.dhis2.usescases.events

import org.dhis2.usescases.general.AbstractActivityContracts
import org.hisp.dhis.android.core.category.CategoryOption
import org.hisp.dhis.android.core.enrollment.Enrollment
import org.hisp.dhis.android.core.event.Event
import org.hisp.dhis.android.core.program.Program
import org.hisp.dhis.android.core.program.ProgramStage
import java.util.Date

class ScheduledEventContract {

    interface View : AbstractActivityContracts.View {
        fun setEvent(event: Event)
        fun setStage(programStage: ProgramStage, event: Event)
        fun setProgram(program: Program)
        fun openInitialActivity()
        fun openFormActivity()
    }

    interface Presenter {
        fun init()
        fun finish()
        fun setEventDate(date: Date)
        fun setDueDate(date: Date)
        fun skipEvent()
        fun setCatOptionCombo(catComboUid: String, arrayList: ArrayList<CategoryOption>)
        fun onBackClick()
        fun getEventTei(): String
        fun getEnrollment(): Enrollment?
    }
}
