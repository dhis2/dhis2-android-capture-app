package org.dhis2.usescases.eventsWithoutRegistration.eventSummary;

import android.content.Context;

import androidx.annotation.NonNull;

import org.dhis2.Bindings.ValueTypeExtensionsKt;
import org.dhis2.data.dagger.PerActivity;
import org.dhis2.data.forms.EventRepository;
import org.dhis2.data.forms.FormRepository;
import org.dhis2.data.forms.RulesRepository;
import org.dhis2.data.forms.dataentry.fields.FieldViewModelFactory;
import org.dhis2.data.forms.dataentry.fields.FieldViewModelFactoryImpl;
import org.dhis2.data.schedulers.SchedulerProvider;
import org.hisp.dhis.android.core.D2;
import org.hisp.dhis.rules.RuleExpressionEvaluator;

import dagger.Module;
import dagger.Provides;

/**
 * Created by Cristian on 13/02/2018.
 */
@PerActivity
@Module
public class EventSummaryModule {

    @NonNull
    private String eventUid;

    EventSummaryModule(@NonNull String eventUid) {
        this.eventUid = eventUid;
    }

    @Provides
    @PerActivity
    EventSummaryContract.View provideView(EventSummaryContract.View activity) {
        return activity;
    }

    @Provides
    @PerActivity
    EventSummaryContract.Presenter providesPresenter(EventSummaryContract.Interactor interactor) {
        return new EventSummaryPresenter(interactor);
    }

    @Provides
    @PerActivity
    EventSummaryContract.Interactor provideInteractor(@NonNull EventSummaryRepository eventSummaryRepository,
                                                      @NonNull SchedulerProvider schedulerProvider) {
        return new EventSummaryInteractor(eventSummaryRepository, schedulerProvider);
    }

    @Provides
    @PerActivity
    EventSummaryRepository eventSummaryRepository(@NonNull Context context,
                                                  @NonNull FormRepository formRepository, D2 d2) {
        FieldViewModelFactory fieldViewModelFactory = new FieldViewModelFactoryImpl(
                ValueTypeExtensionsKt.valueTypeHintMap(context)
        );
        return new EventSummaryRepositoryImpl(fieldViewModelFactory, formRepository, eventUid, d2);
    }

    @Provides
    FormRepository formRepository(@NonNull RuleExpressionEvaluator evaluator,
                                  @NonNull RulesRepository rulesRepository,
                                  @NonNull D2 d2) {
        return new EventRepository(evaluator, rulesRepository, eventUid, d2);
    }

    @Provides
    RulesRepository rulesRepository(@NonNull D2 d2) {
        return new RulesRepository(d2);
    }
}
