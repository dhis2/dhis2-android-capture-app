package org.dhis2.usescases.eventsWithoutRegistration.eventCapture;

import android.content.Context;

import androidx.annotation.NonNull;

import org.dhis2.Bindings.ValueTypeExtensionsKt;
import org.dhis2.R;
import org.dhis2.commons.di.dagger.PerActivity;
import org.dhis2.commons.network.NetworkUtils;
import org.dhis2.commons.prefs.PreferenceProvider;
import org.dhis2.commons.resources.ResourceManager;
import org.dhis2.commons.schedulers.SchedulerProvider;
import org.dhis2.data.dhislogic.DhisEnrollmentUtils;
import org.dhis2.data.forms.EventRepository;
import org.dhis2.data.forms.FormRepository;
import org.dhis2.data.forms.dataentry.DataEntryStore;
import org.dhis2.form.ui.style.FormUiModelColorFactoryImpl;
import org.dhis2.data.forms.dataentry.RuleEngineRepository;
import org.dhis2.data.forms.dataentry.ValueStoreImpl;
import org.dhis2.form.ui.FieldViewModelFactory;
import org.dhis2.form.ui.FieldViewModelFactoryImpl;
import org.dhis2.form.data.FormRepositoryImpl;
import org.dhis2.form.data.FormValueStore;
import org.dhis2.form.data.RulesRepository;
import org.dhis2.form.data.RulesUtilsProviderImpl;
import org.dhis2.form.model.RowAction;
import org.dhis2.form.ui.LayoutProviderImpl;
import org.dhis2.form.ui.provider.DisplayNameProviderImpl;
import org.dhis2.form.ui.provider.HintProviderImpl;
import org.dhis2.form.ui.provider.KeyboardActionProviderImpl;
import org.dhis2.form.ui.provider.UiEventTypesProviderImpl;
import org.dhis2.form.ui.provider.UiStyleProviderImpl;
import org.dhis2.form.ui.style.LongTextUiColorFactoryImpl;
import org.dhis2.form.ui.validation.FieldErrorMessageProvider;
import org.dhis2.utils.customviews.navigationbar.NavigationPageConfigurator;
import org.dhis2.utils.reporting.CrashReportController;
import org.hisp.dhis.android.core.D2;

import dagger.Module;
import dagger.Provides;
import io.reactivex.processors.FlowableProcessor;
import io.reactivex.processors.PublishProcessor;

@Module
public class EventCaptureModule {

    private final String eventUid;
    private final EventCaptureContract.View view;
    private final Context activityContext;

    public EventCaptureModule(EventCaptureContract.View view, String eventUid, Context context) {
        this.view = view;
        this.eventUid = eventUid;
        this.activityContext = context;
    }

    @Provides
    @PerActivity
    EventCaptureContract.Presenter providePresenter(@NonNull EventCaptureContract.EventCaptureRepository eventCaptureRepository,
                                                    @NonNull FormValueStore valueStore,
                                                    SchedulerProvider schedulerProvider,
                                                    PreferenceProvider preferences) {
        return new EventCapturePresenterImpl(view, eventUid, eventCaptureRepository, valueStore, schedulerProvider,
                preferences);
    }

    @Provides
    @PerActivity
    EventFieldMapper provideFieldMapper(Context context, FieldViewModelFactory fieldFactory) {
        return new EventFieldMapper(fieldFactory, context.getString(R.string.field_is_mandatory));
    }

    @Provides
    @PerActivity
    EventCaptureContract.EventCaptureRepository provideRepository(D2 d2) {
        return new EventCaptureRepositoryImpl(eventUid, d2);
    }

    @Provides
    @PerActivity
    FieldViewModelFactory fieldFactory(
            Context context,
            D2 d2
    ) {
        return new FieldViewModelFactoryImpl(
                ValueTypeExtensionsKt.valueTypeHintMap(context),
                false,
                new UiStyleProviderImpl(
                        new FormUiModelColorFactoryImpl(activityContext, true),
                        new LongTextUiColorFactoryImpl(activityContext, true)
                ),
                new LayoutProviderImpl(),
                new HintProviderImpl(context),
                new DisplayNameProviderImpl(d2),
                new UiEventTypesProviderImpl(),
                new KeyboardActionProviderImpl());
    }

    @Provides
    @PerActivity
    RulesRepository rulesRepository(@NonNull D2 d2) {
        return new RulesRepository(d2);
    }

    @Provides
    @PerActivity
    RuleEngineRepository ruleEngineRepository(D2 d2, FormRepository formRepository) {
        return new EventRuleEngineRepository(d2, formRepository, eventUid);
    }

    @Provides
    @PerActivity
    FormRepository formRepository(@NonNull RulesRepository rulesRepository,
                                  @NonNull D2 d2) {
        return new EventRepository(rulesRepository, eventUid, d2);
    }

    @Provides
    @PerActivity
    FormValueStore valueStore(@NonNull D2 d2, CrashReportController crashReportController, NetworkUtils networkUtils) {
        return new ValueStoreImpl(
                d2,
                eventUid,
                DataEntryStore.EntryMode.DE,
                new DhisEnrollmentUtils(d2),
                crashReportController,
                networkUtils
        );
    }

    @Provides
    @PerActivity
    FlowableProcessor<RowAction> getProcessor() {
        return PublishProcessor.create();
    }

    @Provides
    @PerActivity
    org.dhis2.form.data.FormRepository provideEventsFormRepository(
            @NonNull D2 d2,
            org.dhis2.data.forms.dataentry.EventRepository eventDataEntryRepository,
            CrashReportController crashReportController,
            NetworkUtils networkUtils
    ) {
        return new FormRepositoryImpl(
                new ValueStoreImpl(
                        d2,
                        eventUid,
                        DataEntryStore.EntryMode.DE,
                        new DhisEnrollmentUtils(d2),
                        crashReportController,
                        networkUtils
                ),
                new FieldErrorMessageProvider(activityContext),
                new DisplayNameProviderImpl(d2),
                eventDataEntryRepository,
                new org.dhis2.form.data.EventRuleEngineRepository(d2, eventUid),
                new RulesUtilsProviderImpl(d2)
        );
    }

    @Provides
    @PerActivity
    org.dhis2.data.forms.dataentry.EventRepository provideEventDataEntryRepository(
            D2 d2,
            FieldViewModelFactory modelFactory,
            ResourceManager resourceManager
    ) {
        return new org.dhis2.data.forms.dataentry.EventRepository(
                modelFactory,
                eventUid,
                d2,
                resourceManager
        );
    }

    @Provides
    @PerActivity
    NavigationPageConfigurator pageConfigurator(
            EventCaptureContract.EventCaptureRepository repository
    ) {
        return new EventPageConfigurator(repository);
    }
}
