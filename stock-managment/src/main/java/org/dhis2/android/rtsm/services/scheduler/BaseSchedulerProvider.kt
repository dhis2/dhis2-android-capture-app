package org.dhis2.android.rtsm.services.scheduler

import io.reactivex.Scheduler

interface BaseSchedulerProvider {
    fun computation(): Scheduler
    fun io(): Scheduler
    fun ui(): Scheduler
}