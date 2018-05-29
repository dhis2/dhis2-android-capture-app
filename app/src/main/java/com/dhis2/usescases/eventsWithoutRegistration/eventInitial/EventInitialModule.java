package com.dhis2.usescases.eventsWithoutRegistration.eventInitial;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.dhis2.data.dagger.PerActivity;
import com.dhis2.data.forms.EventRepository;
import com.dhis2.data.forms.FormRepository;
import com.dhis2.data.forms.RulesRepository;
import com.dhis2.data.metadata.MetadataRepository;
import com.dhis2.data.schedulers.SchedulerProvider;
import com.dhis2.usescases.eventsWithoutRegistration.eventSummary.EventSummaryRepository;
import com.dhis2.usescases.eventsWithoutRegistration.eventSummary.EventSummaryRepositoryImpl;
import com.dhis2.usescases.programDetail.ProgramRepository;
import com.dhis2.usescases.programDetail.ProgramRepositoryImpl;
import com.dhis2.utils.CodeGenerator;
import com.squareup.sqlbrite2.BriteDatabase;

import org.hisp.dhis.android.core.data.database.DatabaseAdapter;
import org.hisp.dhis.rules.RuleExpressionEvaluator;

import dagger.Module;
import dagger.Provides;

/**
 * Created by Cristian on 13/02/2018.
 */
@PerActivity
@Module
public class EventInitialModule {

    @Nullable
    private String eventUid;

    public EventInitialModule(@Nullable String eventUid) {
        this.eventUid = eventUid;
    }

    @Provides
    @PerActivity
    EventInitialContract.View provideView(EventInitialContract.View activity) {
        return activity;
    }

    @Provides
    @PerActivity
    EventInitialContract.Presenter providesPresenter(EventInitialContract.Interactor interactor) {
        return new EventInitialPresenter(interactor);
    }


    @Provides
    @PerActivity
    EventInitialContract.Interactor provideInteractor(@NonNull EventSummaryRepository eventSummaryRepository,
                                                      @NonNull EventInitialRepository eventInitialRepository,
                                                      @NonNull MetadataRepository metadataRepository,
                                                      @NonNull SchedulerProvider schedulerProvider) {
        return new EventInitialInteractor(eventSummaryRepository, eventInitialRepository, metadataRepository, schedulerProvider);
    }

    @Provides
    @PerActivity
    EventSummaryRepository eventSummaryRepository(@NonNull Context context,
                                                  @NonNull BriteDatabase briteDatabase,
                                                  @NonNull FormRepository formRepository) {
        return new EventSummaryRepositoryImpl(context, briteDatabase, formRepository, eventUid);
    }

    @Provides
    FormRepository formRepository(@NonNull BriteDatabase briteDatabase,
                                  @NonNull RuleExpressionEvaluator evaluator,
                                  @NonNull RulesRepository rulesRepository) {
        return new EventRepository(briteDatabase, evaluator, rulesRepository, eventUid);
    }

    @Provides
    RulesRepository rulesRepository(@NonNull BriteDatabase briteDatabase) {
        return new RulesRepository(briteDatabase);
    }

    @Provides
    @PerActivity
    EventInitialRepository eventDetailRepository(@NonNull CodeGenerator codeGenerator, BriteDatabase briteDatabase, DatabaseAdapter databaseAdapter) {
        return new EventInitialRepositoryImpl(codeGenerator, briteDatabase, databaseAdapter);
    }

    @Provides
    @PerActivity
    ProgramRepository homeRepository(BriteDatabase briteDatabase) {
        return new ProgramRepositoryImpl(briteDatabase);
    }
}
