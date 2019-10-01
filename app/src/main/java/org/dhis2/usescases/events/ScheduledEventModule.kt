package org.dhis2.usescases.events

import dagger.Module
import dagger.Provides
import org.dhis2.data.dagger.PerActivity
import org.hisp.dhis.android.core.D2

/**
 * QUADRAM. Created by ppajuelo on 07/02/2018.
 */

@Module
@PerActivity
class ScheduledEventModule(val eventUid: String) {

    @Provides
    @PerActivity
    internal fun providePresenter(d2: D2): ScheduledEventContract.Presenter {
        return ScheduledEventPresenterImpl(d2, eventUid)
    }


}
