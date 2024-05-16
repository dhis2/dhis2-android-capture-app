package org.dhis2.android.rtsm.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import io.reactivex.disposables.CompositeDisposable

@Module
@InstallIn(ViewModelComponent::class)
object SystemModule {
    @Provides
    fun providesDisposable(): CompositeDisposable {
        return CompositeDisposable()
    }
}
