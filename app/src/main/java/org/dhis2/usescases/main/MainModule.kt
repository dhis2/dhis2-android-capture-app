package org.dhis2.usescases.main


import dagger.Module
import dagger.Provides
import org.dhis2.data.dagger.PerActivity
import org.dhis2.data.schedulers.SchedulerProvider
import org.hisp.dhis.android.core.D2

@Module
class MainModule(val view: MainView) {

    @Provides
    @PerActivity
    fun homePresenter(d2: D2, schedulerProvider: SchedulerProvider): MainPresenter {
        return MainPresenter(view, d2, schedulerProvider)
    }

}