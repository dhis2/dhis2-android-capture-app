package org.dhis2.usescases.eventsWithoutRegistration.eventCapture.eventCaptureFragment

import dagger.Module
import dagger.Provides
import org.dhis2.commons.di.dagger.PerFragment
import org.dhis2.usescases.eventsWithoutRegistration.eventCapture.EventCaptureContract
import org.hisp.dhis.android.core.D2

@Module
class EventCaptureFormModule(
    val view: EventCaptureFormView,
    val eventUid: String,
) {

    @Provides
    @PerFragment
    fun providePresenter(
        activityPresenter: EventCaptureContract.Presenter,
        d2: D2,
    ): EventCaptureFormPresenter {
        return EventCaptureFormPresenter(
            view,
            activityPresenter,
            d2,
            eventUid,
        )
    }
}
