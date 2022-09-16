package org.dhis2.usescases.eventsWithoutRegistration.eventCapture;

import android.content.Context;

import androidx.annotation.NonNull;

import org.dhis2.R;
import org.dhis2.commons.data.EntryMode;
import org.dhis2.commons.di.dagger.PerActivity;
import org.dhis2.commons.network.NetworkUtils;
import org.dhis2.commons.prefs.PreferenceProvider;
import org.dhis2.commons.reporting.CrashReportController;
import org.dhis2.commons.resources.ResourceManager;
import org.dhis2.commons.schedulers.SchedulerProvider;
import org.dhis2.data.dhislogic.DhisEnrollmentUtils;
import org.dhis2.data.forms.EventRepository;
import org.dhis2.data.forms.FormRepository;
import org.dhis2.data.forms.dataentry.RuleEngineRepository;
import org.dhis2.data.forms.dataentry.SearchTEIRepository;
import org.dhis2.data.forms.dataentry.SearchTEIRepositoryImpl;
import org.dhis2.form.data.FormRepositoryImpl;
import org.dhis2.form.data.FormValueStore;
import org.dhis2.form.data.RulesRepository;
import org.dhis2.form.data.RulesUtilsProviderImpl;
import org.dhis2.form.data.metadata.OptionSetConfiguration;
import org.dhis2.form.data.metadata.OrgUnitConfiguration;
import org.dhis2.form.model.RowAction;
import org.dhis2.form.ui.FieldViewModelFactory;
import org.dhis2.form.ui.FieldViewModelFactoryImpl;
import org.dhis2.form.ui.LayoutProviderImpl;
import org.dhis2.form.ui.provider.DisplayNameProviderImpl;
import org.dhis2.form.ui.provider.HintProviderImpl;
import org.dhis2.form.ui.provider.KeyboardActionProviderImpl;
import org.dhis2.form.ui.provider.LegendValueProviderImpl;
import org.dhis2.form.ui.provider.UiEventTypesProviderImpl;
import org.dhis2.form.ui.provider.UiStyleProviderImpl;
import org.dhis2.form.ui.style.FormUiModelColorFactoryImpl;
import org.dhis2.form.ui.style.LongTextUiColorFactoryImpl;
import org.dhis2.form.ui.validation.FieldErrorMessageProvider;
import org.dhis2.usescases.eventsWithoutRegistration.eventCapture.domain.ConfigureEventCompletionDialog;
import org.dhis2.usescases.eventsWithoutRegistration.eventCapture.provider.EventCaptureResourcesProvider;
import org.dhis2.utils.customviews.navigationbar.NavigationPageConfigurator;
import org.dhis2.commons.reporting.CrashReportControllerImpl;
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
                                                    SchedulerProvider schedulerProvider,
                                                    PreferenceProvider preferences,
                                                    ConfigureEventCompletionDialog configureEventCompletionDialog) {
        return new EventCapturePresenterImpl(
                view,
                eventUid,
                eventCaptureRepository,
                schedulerProvider,
                preferences,
                configureEventCompletionDialog);
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
            D2 d2,
            ResourceManager resourceManager
    ) {
        return new FieldViewModelFactoryImpl(
                false,
                new UiStyleProviderImpl(
                        new FormUiModelColorFactoryImpl(activityContext, true),
                        new LongTextUiColorFactoryImpl(activityContext, true)
                ),
                new LayoutProviderImpl(),
                new HintProviderImpl(context),
                new DisplayNameProviderImpl(
                        new OptionSetConfiguration(d2),
                        new OrgUnitConfiguration(d2)
                ),
                new UiEventTypesProviderImpl(),
                new KeyboardActionProviderImpl(),
                new LegendValueProviderImpl(d2, resourceManager));
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
    FormValueStore valueStore(
            @NonNull D2 d2,
            CrashReportController crashReportController,
            NetworkUtils networkUtils,
            ResourceManager resourceManager
    ) {
        return new FormValueStore(
                d2,
                eventUid,
                EntryMode.DE,
                null,
                crashReportController,
                networkUtils,
                resourceManager
        );
    }

    @Provides
    @PerActivity
    SearchTEIRepository searchTEIRepository(D2 d2) {
        return new SearchTEIRepositoryImpl(d2, new DhisEnrollmentUtils(d2), new CrashReportControllerImpl());
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
            org.dhis2.form.data.EventRepository eventDataEntryRepository,
            CrashReportController crashReportController,
            NetworkUtils networkUtils,
            ResourceManager resourceManager
    ) {
        FieldErrorMessageProvider fieldErrorMessageProvider = new FieldErrorMessageProvider(activityContext);
        return new FormRepositoryImpl(
                new FormValueStore(
                        d2,
                        eventUid,
                        EntryMode.DE,
                        null,
                        crashReportController,
                        networkUtils,
                        resourceManager
                ),
                fieldErrorMessageProvider,
                new DisplayNameProviderImpl(
                        new OptionSetConfiguration(d2),
                        new OrgUnitConfiguration(d2)
                ),
                eventDataEntryRepository,
                new org.dhis2.form.data.EventRuleEngineRepository(d2, eventUid),
                new RulesUtilsProviderImpl(d2),
                new LegendValueProviderImpl(d2, resourceManager)
        );
    }

    @Provides
    @PerActivity
    org.dhis2.form.data.EventRepository provideEventDataEntryRepository(
            D2 d2,
            FieldViewModelFactory modelFactory
    ) {
        return new org.dhis2.form.data.EventRepository(
                modelFactory,
                eventUid,
                d2
        );
    }

    @Provides
    @PerActivity
    NavigationPageConfigurator pageConfigurator(
            EventCaptureContract.EventCaptureRepository repository
    ) {
        return new EventPageConfigurator(repository);
    }

    @Provides
    @PerActivity
    ConfigureEventCompletionDialog provideConfigureEventCompletionDialog(
            EventCaptureResourcesProvider eventCaptureResourcesProvider
    ) {
        return new ConfigureEventCompletionDialog(eventCaptureResourcesProvider);
    }

    @Provides
    @PerActivity
    EventCaptureResourcesProvider provideEventCaptureResourcesProvider(
            ResourceManager resourceManager
    ) {
        return new EventCaptureResourcesProvider(resourceManager);
    }
}
