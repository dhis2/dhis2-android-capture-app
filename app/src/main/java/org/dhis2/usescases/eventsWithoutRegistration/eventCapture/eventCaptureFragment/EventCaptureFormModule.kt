package org.dhis2.usescases.eventsWithoutRegistration.eventCapture.eventCaptureFragment

import dagger.Module
import dagger.Provides
import io.reactivex.processors.FlowableProcessor
import org.dhis2.commons.di.dagger.PerFragment
import org.dhis2.commons.schedulers.SchedulerProvider
import org.dhis2.form.data.FormRepository
import org.dhis2.form.model.RowAction
import org.dhis2.usescases.eventsWithoutRegistration.eventCapture.EventCaptureContract

@PerFragment
@Module
class EventCaptureFormModule(
    val view: EventCaptureFormView,
    val eventUid: String
) {

    @Provides
    @PerFragment
    fun providePresenter(
        activityPresenter: EventCaptureContract.Presenter,
        schedulerProvider: SchedulerProvider,
        onFieldActionProcessor: FlowableProcessor<RowAction>,
        formRepository: FormRepository
    ): EventCaptureFormPresenter {
        return EventCaptureFormPresenter(
            view,
            activityPresenter,
            schedulerProvider,
            onFieldActionProcessor,
            formRepository
        )
    }
}
