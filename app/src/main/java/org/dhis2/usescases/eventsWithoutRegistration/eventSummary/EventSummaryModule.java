package org.dhis2.usescases.eventsWithoutRegistration.eventSummary;

import android.content.Context;

import androidx.annotation.NonNull;

import com.squareup.sqlbrite2.BriteDatabase;

import org.dhis2.data.dagger.PerActivity;
import org.dhis2.data.forms.EventRepository;
import org.dhis2.data.forms.FormRepository;
import org.dhis2.data.forms.RulesRepository;
import org.dhis2.data.schedulers.SchedulerProvider;
import org.hisp.dhis.android.core.D2;
import org.hisp.dhis.rules.RuleExpressionEvaluator;

import dagger.Module;
import dagger.Provides;

/**
 * Created by Cristian on 13/02/2018.
 *
 */
@PerActivity
@Module
public class EventSummaryModule {

    @NonNull
    private Context context;

    @NonNull
    private String eventUid;

    EventSummaryModule(@NonNull Context context, @NonNull String eventUid) {
        this.context = context;
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
                                                  @NonNull BriteDatabase briteDatabase,
                                                  @NonNull FormRepository formRepository,D2 d2) {
        return new EventSummaryRepositoryImpl(context, briteDatabase, formRepository, eventUid,d2);
    }

    @Provides
    FormRepository formRepository(@NonNull BriteDatabase briteDatabase,
                                  @NonNull RuleExpressionEvaluator evaluator,
                                  @NonNull RulesRepository rulesRepository,
                                  @NonNull D2 d2) {
        return new EventRepository(briteDatabase, evaluator, rulesRepository, eventUid,d2);
    }

    @Provides
    RulesRepository rulesRepository(@NonNull BriteDatabase briteDatabase,@NonNull D2 d2) {
        return new RulesRepository(briteDatabase,d2);
    }
}
