package org.dhis2.data.fingerprint

import dagger.Module
import dagger.Provides
import org.dhis2.commons.di.dagger.PerActivity

@Module
object FingerPrintModule {

    @JvmStatic
    @Provides
    @PerActivity
    fun provideFingerPrintController(mapper: FingerPrintMapper): FingerPrintController {
        return FingerPrintControllerImpl(mapper)
    }

    @JvmStatic
    @Provides
    fun provideFingerPrintMapper(): FingerPrintMapper {
        return FingerPrintMapper()
    }
}
