package org.dhis2.usescases.eventsWithoutRegistration.eventCapture.eventCaptureFragment

import dagger.Module
import dagger.Provides
import io.reactivex.processors.FlowableProcessor
import org.dhis2.data.dagger.PerFragment
import org.dhis2.data.forms.dataentry.ValueStore
import org.dhis2.data.forms.dataentry.fields.RowAction
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
        schedulerProvider: SchedulerProvider,
        onFieldActionProcessor: FlowableProcessor<RowAction>,
        focusProcessor: FlowableProcessor<HashMap<String, Boolean>>
    ): EventCaptureFormPresenter {
        return EventCaptureFormPresenter(
            view,
            activityPresenter,
            d2,
            valueStore,
            schedulerProvider,
            onFieldActionProcessor,
            focusProcessor
        )
    }
}
