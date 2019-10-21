package org.dhis2.data.fingerprint

import android.content.Context
import co.infinum.goldfinger.rx.RxGoldfinger
import dagger.Module
import dagger.Provides
import org.dhis2.data.dagger.PerActivity
import org.dhis2.BuildConfig

@Module
@PerActivity
object FingerPrintModule {

    @JvmStatic
    @Provides
    @PerActivity
    fun provideFingerPrintController(goldfinger: RxGoldfinger, mapper:FingerPrintMapper)
            : FingerPrintController{
        return FingerPrintControllerImpl(goldfinger, mapper)
    }

    @JvmStatic
    @Provides
    fun provideFingerPrintModule(context: Context) : RxGoldfinger {
        return RxGoldfinger.Builder(context).setLogEnabled(BuildConfig.DEBUG).build()
    }


    @JvmStatic
    @Provides
    fun provideFingerPrintMapper() : FingerPrintMapper {
        return FingerPrintMapper()
    }
}