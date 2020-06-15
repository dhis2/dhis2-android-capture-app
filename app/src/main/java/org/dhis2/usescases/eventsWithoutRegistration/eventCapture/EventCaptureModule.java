package org.dhis2.usescases.eventsWithoutRegistration.eventCapture;

import android.content.Context;

import androidx.annotation.NonNull;

import org.dhis2.Bindings.ValueTypeExtensionsKt;
import org.dhis2.data.dagger.PerActivity;
import org.dhis2.data.forms.EventRepository;
import org.dhis2.data.forms.FormRepository;
import org.dhis2.data.forms.RulesRepository;
import org.dhis2.data.forms.dataentry.DataEntryStore;
import org.dhis2.data.forms.dataentry.ValueStore;
import org.dhis2.data.forms.dataentry.ValueStoreImpl;
import org.dhis2.data.forms.dataentry.fields.FieldViewModelFactory;
import org.dhis2.data.forms.dataentry.fields.FieldViewModelFactoryImpl;
import org.dhis2.data.schedulers.SchedulerProvider;
import org.dhis2.utils.RulesUtilsProvider;
import org.hisp.dhis.android.core.D2;
import org.hisp.dhis.rules.RuleExpressionEvaluator;

import dagger.Module;
import dagger.Provides;

@PerActivity
@Module
public class EventCaptureModule {


    private final String eventUid;
    private final EventCaptureContract.View view;

    public EventCaptureModule(EventCaptureContract.View view, String eventUid) {
        this.view = view;
        this.eventUid = eventUid;
    }

    @Provides
    @PerActivity
    EventCaptureContract.Presenter providePresenter(@NonNull EventCaptureContract.EventCaptureRepository eventCaptureRepository,
                                                    @NonNull RulesUtilsProvider ruleUtils,
                                                    @NonNull ValueStore valueStore,
                                                    SchedulerProvider schedulerProvider) {
        return new EventCapturePresenterImpl(view, eventUid, eventCaptureRepository, ruleUtils, valueStore, schedulerProvider);
    }

    @Provides
    @PerActivity
    EventCaptureContract.EventCaptureRepository provideRepository(Context context,
                                                                  FormRepository formRepository, D2 d2) {
        FieldViewModelFactory fieldFactory = new FieldViewModelFactoryImpl(ValueTypeExtensionsKt.valueTypeHintMap(context));
        return new EventCaptureRepositoryImpl(fieldFactory, formRepository, eventUid, d2);
    }

    @Provides
    @PerActivity
    RulesRepository rulesRepository(@NonNull D2 d2) {
        return new RulesRepository(d2);
    }

    @Provides
    @PerActivity
    FormRepository formRepository(@NonNull RuleExpressionEvaluator evaluator,
                                  @NonNull RulesRepository rulesRepository,
                                  @NonNull D2 d2) {
        return new EventRepository(evaluator, rulesRepository, eventUid, d2);
    }

    @Provides
    @PerActivity
    ValueStore valueStore(@NonNull D2 d2) {
        return new ValueStoreImpl(d2, eventUid, DataEntryStore.EntryMode.DE);
    }

}
