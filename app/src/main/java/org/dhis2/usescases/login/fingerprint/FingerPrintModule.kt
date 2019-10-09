package org.dhis2.usescases.login.fingerprint

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
    fun provideFingerPrintModule(context: Context) =
            RxGoldfinger.Builder(context).setLogEnabled(BuildConfig.DEBUG).build()
}