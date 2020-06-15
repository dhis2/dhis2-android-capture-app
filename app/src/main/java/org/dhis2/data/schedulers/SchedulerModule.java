package org.dhis2.data.schedulers;

import androidx.annotation.NonNull;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class SchedulerModule {
    private final SchedulerProvider schedulerProvider;

    public SchedulerModule(@NonNull SchedulerProvider schedulerProvider) {
        this.schedulerProvider = schedulerProvider;
    }

    @Provides
    @Singleton
    SchedulerProvider schedulerProvider() {
        return schedulerProvider;
    }
}
