package org.dhis2.data.dispatcher

import dagger.Module
import dagger.Provides
import org.dhis2.form.model.DispatcherProvider
import org.dhis2.form.model.coroutine.FormDispatcher
import javax.inject.Singleton

@Module
open class DispatcherModule {

    @Provides
    @Singleton
    open fun provideDispatcherModule(): DispatcherProvider{
        return FormDispatcher()
    }
}