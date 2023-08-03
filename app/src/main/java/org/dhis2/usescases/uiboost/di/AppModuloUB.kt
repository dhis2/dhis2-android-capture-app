package org.dhis2.usescases.uiboost.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.dhis2.usescases.uiboost.data.repository.UBDataStoreRepository
import org.dhis2.usescases.uiboost.data.repository.UBDataStoreRepositoryImpl
import org.hisp.dhis.android.core.D2
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModuloUB {

    @Provides
    @Singleton
    fun provideUBDataStoreRepository(d2: D2) : UBDataStoreRepository{
      return UBDataStoreRepositoryImpl(d2)
    }
}