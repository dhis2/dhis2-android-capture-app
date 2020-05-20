package org.dhis2.usescases.eventsWithoutRegistration.eventCapture.EventCaptureFragment

import dagger.Module
import dagger.Provides
import org.dhis2.data.dagger.PerFragment
import org.dhis2.data.forms.dataentry.ValueStore
import org.dhis2.data.schedulers.SchedulerProvider
import org.dhis2.usescases.eventsWithoutRegistration.eventCapture.EventCaptureContract
import org.hisp.dhis.android.core.D2

@PerFragment
@Module
class EventCaptureFormModule(
    val view: EventCaptureFormView,
    val eventUid: String
) {

    @Provides
    @PerFragment
    fun providePresenter(
        d2: D2,
        activityPresenter: EventCaptureContract.Presenter,
        valueStore: ValueStore,
        schedulerProvider: SchedulerProvider
    ): EventCaptureFormPresenter {
        return EventCaptureFormPresenter(view, activityPresenter, d2, valueStore, schedulerProvider)
    }
}
