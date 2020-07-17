package org.dhis2.usescases.eventsWithoutRegistration.eventCapture.indicators

import io.reactivex.functions.Consumer
import org.dhis2.data.tuples.Trio
import org.dhis2.usescases.general.AbstractActivityContracts
import org.hisp.dhis.android.core.program.ProgramIndicator

class EventIndicatorsContracts {
    interface View : AbstractActivityContracts.View {
        fun swapIndicators(): Consumer<List<Trio<ProgramIndicator, String, String>>>
    }

    interface Presenter : AbstractActivityContracts.Presenter {
        fun init(view: View)
    }
}