package org.dhis2.data.schedulers

import javax.inject.Singleton

import dagger.Module
import dagger.Provides

@Module
class SchedulerModule(private val schedulerProvider: SchedulerProvider) {

    @Provides
    @Singleton
    internal fun schedulerProvider(): SchedulerProvider {
        return schedulerProvider
    }
}
