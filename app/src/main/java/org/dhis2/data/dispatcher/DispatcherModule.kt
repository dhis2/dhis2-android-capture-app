package org.dhis2.data.dispatcher

import dagger.Module
import dagger.Provides
import org.dhis2.form.model.DispatcherProvider
import javax.inject.Singleton

@Module
class DispatcherModule {

    @Provides
    @Singleton
    fun provideDispatcherModule(): DispatcherProvider{
        return FormDispatcher()
    }
}