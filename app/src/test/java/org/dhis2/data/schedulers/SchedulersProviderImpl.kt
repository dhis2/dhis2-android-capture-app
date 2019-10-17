package org.dhis2.data.schedulers

import io.reactivex.Scheduler
import io.reactivex.schedulers.Schedulers

class SchedulersProviderImpl : SchedulerProvider {

    override fun computation(): Scheduler {
        return Schedulers.trampoline()
    }

    override fun io(): Scheduler {
        return Schedulers.trampoline()
    }

    override fun ui(): Scheduler {
        return Schedulers.trampoline()
    }
}
