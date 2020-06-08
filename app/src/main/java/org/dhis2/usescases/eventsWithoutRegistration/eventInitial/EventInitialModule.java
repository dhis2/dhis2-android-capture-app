package org.dhis2.usescases.eventsWithoutRegistration.eventInitial;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.dhis2.Bindings.ValueTypeExtensionsKt;
import org.dhis2.data.dagger.PerActivity;
import org.dhis2.data.forms.EventRepository;
import org.dhis2.data.forms.FormRepository;
import org.dhis2.data.forms.RulesRepository;
import org.dhis2.data.forms.dataentry.fields.FieldViewModelFactory;
import org.dhis2.data.forms.dataentry.fields.FieldViewModelFactoryImpl;
import org.dhis2.data.prefs.PreferenceProvider;
import org.dhis2.data.schedulers.SchedulerProvider;
import org.dhis2.usescases.eventsWithoutRegistration.eventSummary.EventSummaryRepository;
import org.dhis2.usescases.eventsWithoutRegistration.eventSummary.EventSummaryRepositoryImpl;
import org.dhis2.utils.analytics.AnalyticsHelper;
import org.hisp.dhis.android.core.D2;
import org.hisp.dhis.rules.RuleExpressionEvaluator;

import javax.annotation.Nonnull;

import dagger.Module;
import dagger.Provides;

@PerActivity
@Module
public class EventInitialModule {

    private final EventInitialContract.View view;
    @Nullable
    private String eventUid;

    public EventInitialModule(@Nonnull EventInitialContract.View view,
                              @Nullable String eventUid) {
        this.view = view;
        this.eventUid = eventUid;
    }

    @Provides
    @PerActivity
    EventInitialContract.Presenter providesPresenter(@NonNull EventSummaryRepository eventSummaryRepository,
                                                     @NonNull EventInitialRepository eventInitialRepository,
                                                     @NonNull SchedulerProvider schedulerProvider,
                                                     @Nonnull PreferenceProvider preferenceProvider,
                                                     @Nonnull AnalyticsHelper analyticsHelper) {
        return new EventInitialPresenter(
                view,
                eventSummaryRepository,
                eventInitialRepository,
                schedulerProvider,
                preferenceProvider,
                analyticsHelper);
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

    @Provides
    @PerActivity
    EventInitialRepository eventDetailRepository(D2 d2) {
        return new EventInitialRepositoryImpl(eventUid, d2);
    }
}
