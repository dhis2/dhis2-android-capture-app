package org.dhis2.usescases.events

import dagger.Module
import dagger.Provides
import org.dhis2.commons.di.dagger.PerActivity
import org.dhis2.data.dhislogic.DhisEventUtils
import org.hisp.dhis.android.core.D2

@Module
class ScheduledEventModule(val eventUid: String, val view: ScheduledEventContract.View) {

    @Provides
    @PerActivity
    internal fun providePresenter(
        d2: D2,
        eventUtils: DhisEventUtils,
    ): ScheduledEventContract.Presenter {
        return ScheduledEventPresenterImpl(view, d2, eventUid, eventUtils)
    }
}
