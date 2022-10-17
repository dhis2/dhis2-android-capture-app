package org.dhis2

import dagger.Module
import dagger.Provides
import dispatch.core.DispatcherProvider
import javax.inject.Singleton

@Module
class CustomDispatcherModule {
    @Provides
    @Singleton
    fun provideCustomDispatcherProvider(): DispatcherProvider {
        return DispatcherProvider()
    }
}
