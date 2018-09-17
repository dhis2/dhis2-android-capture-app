package org.dhis2.data.schedulers;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class SchedulersModule {

    @Provides
    @Singleton
    SchedulerProvider schedulerProvider() {
        return new SchedulersProviderImpl();
    }
}
