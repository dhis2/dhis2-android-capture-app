package org.dhis2.data.dispatcher

import dagger.Module
import dagger.Provides
import javax.inject.Singleton
import org.dhis2.commons.viewmodel.DispatcherProvider
import org.dhis2.form.model.coroutine.FormDispatcher

@Module
open class DispatcherModule {

    @Provides
    @Singleton
    open fun provideDispatcherModule(): DispatcherProvider {
        return FormDispatcher()
    }
}
