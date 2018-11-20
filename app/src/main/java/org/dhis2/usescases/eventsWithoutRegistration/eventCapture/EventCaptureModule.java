package org.dhis2.usescases.eventsWithoutRegistration.eventCapture;

import android.content.Context;
import android.support.annotation.NonNull;

import com.squareup.sqlbrite2.BriteDatabase;

import org.dhis2.data.dagger.PerActivity;
import org.dhis2.data.forms.EventRepository;
import org.dhis2.data.forms.FormRepository;
import org.dhis2.data.forms.RulesRepository;
import org.dhis2.utils.RulesUtilsProvider;
import org.hisp.dhis.rules.RuleExpressionEvaluator;

import dagger.Module;
import dagger.Provides;

/**
 * QUADRAM. Created by ppajuelo on 19/11/2018.
 */

@PerActivity
@Module
public class EventCaptureModule {


    private final String eventUid;
    private final String programUid;

    public EventCaptureModule(String eventUid, String programUid) {
        this.eventUid = eventUid;
        this.programUid = programUid;
    }

    @Provides
    @PerActivity
    EventCaptureContract.Presenter providePresenter(@NonNull EventCaptureContract.EventCaptureRepository eventCaptureRepository,
                                                    @NonNull RulesUtilsProvider ruleUtils) {
        return new EventCapturePresenterImpl(eventCaptureRepository, ruleUtils);
    }

    @Provides
    @PerActivity
    EventCaptureContract.EventCaptureRepository provideRepository(Context context,
                                                                  @NonNull BriteDatabase briteDatabase,
                                                                  FormRepository formRepository) {
        return new EventCaptureRepositoryImpl(context, briteDatabase, formRepository, eventUid);
    }

    @Provides
    RulesRepository rulesRepository(@NonNull BriteDatabase briteDatabase) {
        return new RulesRepository(briteDatabase);
    }

    @Provides
    FormRepository formRepository(@NonNull BriteDatabase briteDatabase,
                                  @NonNull RuleExpressionEvaluator evaluator,
                                  @NonNull RulesRepository rulesRepository) {
        return new EventRepository(briteDatabase, evaluator, rulesRepository, eventUid);
    }


}
