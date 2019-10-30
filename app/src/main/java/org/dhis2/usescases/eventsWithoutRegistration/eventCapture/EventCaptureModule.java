package org.dhis2.usescases.eventsWithoutRegistration.eventCapture;

import android.content.Context;

import androidx.annotation.NonNull;

import com.squareup.sqlbrite2.BriteDatabase;

import org.dhis2.data.dagger.PerActivity;
import org.dhis2.data.forms.EventRepository;
import org.dhis2.data.forms.FormRepository;
import org.dhis2.data.forms.RulesRepository;
import org.dhis2.data.forms.dataentry.DataEntryStore;
import org.dhis2.data.forms.dataentry.DataValueStore;
import org.dhis2.data.schedulers.SchedulerProvider;
import org.dhis2.data.user.UserRepository;
import org.dhis2.utils.RulesUtilsProvider;
import org.hisp.dhis.android.core.D2;
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
                                                    @NonNull RulesUtilsProvider ruleUtils,
                                                    @NonNull DataEntryStore dataEntryStore,
                                                    SchedulerProvider schedulerProvider) {
        return new EventCapturePresenterImpl(eventUid, eventCaptureRepository, ruleUtils, dataEntryStore, schedulerProvider);
    }

    @Provides
    @PerActivity
    EventCaptureContract.EventCaptureRepository provideRepository(Context context,
                                                                  @NonNull BriteDatabase briteDatabase,
                                                                  FormRepository formRepository, D2 d2) {
        return new EventCaptureRepositoryImpl(context, briteDatabase, formRepository, eventUid, d2);
    }

    @Provides
    @PerActivity
    RulesRepository rulesRepository(@NonNull BriteDatabase briteDatabase, @NonNull D2 d2) {
        return new RulesRepository(d2);
    }

    @Provides
    @PerActivity
    FormRepository formRepository(@NonNull BriteDatabase briteDatabase,
                                  @NonNull RuleExpressionEvaluator evaluator,
                                  @NonNull RulesRepository rulesRepository,
                                  @NonNull D2 d2) {
        return new EventRepository(briteDatabase, evaluator, rulesRepository, eventUid, d2);
    }

    @Provides
    @PerActivity
    DataEntryStore dataValueStore(@NonNull BriteDatabase briteDatabase,
                                  @NonNull UserRepository userRepository,
                                  @NonNull D2 d2) {
        return new DataValueStore(d2,briteDatabase, userRepository, eventUid);
    }

}
