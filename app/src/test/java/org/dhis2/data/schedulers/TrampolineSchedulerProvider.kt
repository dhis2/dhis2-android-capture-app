package org.dhis2.data.schedulers

import io.reactivex.schedulers.Schedulers
import org.dhis2.commons.schedulers.SchedulerProvider

class TrampolineSchedulerProvider : SchedulerProvider {
    override fun computation() = Schedulers.trampoline()
    override fun io() = Schedulers.trampoline()
    override fun ui() = Schedulers.trampoline()
}
