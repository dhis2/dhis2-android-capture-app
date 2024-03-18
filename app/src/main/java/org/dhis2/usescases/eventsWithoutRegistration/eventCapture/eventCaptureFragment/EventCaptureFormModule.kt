package org.dhis2.usescases.eventsWithoutRegistration.eventCapture.eventCaptureFragment

import dagger.Module
import dagger.Provides
import org.dhis2.commons.di.dagger.PerFragment
import org.dhis2.commons.resources.ResourceManager
import org.dhis2.usescases.eventsWithoutRegistration.eventCapture.EventCaptureContract
import org.dhis2.usescases.eventsWithoutRegistration.eventCapture.domain.ReOpenEventUseCase
import org.dhis2.usescases.eventsWithoutRegistration.eventCapture.injection.EventDispatchers
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
        resourceManager: ResourceManager,
        reOpenEventUseCase: ReOpenEventUseCase,
        eventDispatchers: EventDispatchers,
    ): EventCaptureFormPresenter {
        return EventCaptureFormPresenter(
            view,
            activityPresenter,
            d2,
            eventUid,
            resourceManager,
            reOpenEventUseCase,
            eventDispatchers,
        )
    }

    @Provides
    @PerFragment
    fun provideReOpenEventUseCase(
        d2: D2,
        eventDispatchers: EventDispatchers,
    ) = ReOpenEventUseCase(eventDispatchers, d2)

    @Provides
    @PerFragment
    fun provideEventDispatchers() = EventDispatchers()
}
