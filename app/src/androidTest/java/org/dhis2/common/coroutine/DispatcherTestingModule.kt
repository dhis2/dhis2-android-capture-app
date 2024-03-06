package org.dhis2.common.coroutine

import dagger.Module
import dagger.Provides
import org.dhis2.data.dispatcher.DispatcherModule
import org.dhis2.commons.viewmodel.DispatcherProvider
import org.dhis2.form.model.coroutine.EspressoTestingDispatcher
import javax.inject.Singleton

@Module
class DispatcherTestingModule: DispatcherModule() {

    @Provides
    @Singleton
    override fun provideDispatcherModule(): DispatcherProvider {
        return EspressoTestingDispatcher()
    }
}