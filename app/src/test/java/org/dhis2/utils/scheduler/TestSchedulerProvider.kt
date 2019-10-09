package org.dhis2.utils.scheduler

import io.reactivex.schedulers.TestScheduler
import org.dhis2.data.schedulers.SchedulerProvider

class TestSchedulerProvider(private val scheduler: TestScheduler) : SchedulerProvider {
    override fun computation() = scheduler
    override fun ui() = scheduler
    override fun io() = scheduler
}