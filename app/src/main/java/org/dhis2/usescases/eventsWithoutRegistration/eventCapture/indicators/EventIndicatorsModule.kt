package org.dhis2.usescases.eventsWithoutRegistration.eventCapture.indicators

import com.squareup.sqlbrite2.BriteDatabase
import dagger.Module
import dagger.Provides
import org.dhis2.data.dagger.PerFragment
import org.dhis2.data.forms.FormRepository
import org.dhis2.data.forms.dataentry.EventsRuleEngineRepository
import org.dhis2.data.forms.dataentry.RuleEngineRepository
import org.dhis2.data.schedulers.SchedulerProvider
import org.dhis2.usescases.eventsWithoutRegistration.eventCapture.EventCaptureContract.EventCaptureRepository
import org.hisp.dhis.android.core.D2

@PerFragment
@Module
class EventIndicatorsModule(private val programUid: String, private val eventUid: String) {
    @Provides
    @PerFragment
    fun providesPresenter(
        d2: D2,
        eventCaptureRepository: EventCaptureRepository,
        ruleEngineRepository: RuleEngineRepository,
        schedulerProvider: SchedulerProvider
    ): EventIndicatorsContracts.Presenter {
        return EventIndicatorsPresenter(
            d2,
            programUid,
            eventUid,
            eventCaptureRepository,
            ruleEngineRepository,
            schedulerProvider
        )
    }

    @Provides
    @PerFragment
    fun ruleEngineRepository(
        briteDatabase: BriteDatabase,
        formRepository: FormRepository
    ): RuleEngineRepository {
        return EventsRuleEngineRepository(briteDatabase, formRepository, eventUid)
    }
}