package org.dhis2.android.rtsm.services.scheduler

import io.reactivex.Scheduler
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class SchedulerProviderImpl @Inject constructor() : BaseSchedulerProvider {
    override fun computation() = Schedulers.computation()
    override fun io() = Schedulers.io()
    override fun ui(): Scheduler = AndroidSchedulers.mainThread()
}
