package org.dhis2.utils;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
@Singleton
public final class UtilsModule {

    @Provides
    @Singleton
    CurrentDateProvider currentDateProvider() {
        return new CurrentDateProviderImpl();
    }

    @Provides
    @Singleton
    RulesUtilsProvider rulesUtilsProvider() {
        return new RulesUtilsProviderImpl();
    }
}
