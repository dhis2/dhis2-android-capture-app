package org.dhis2.usescases.eventsWithoutRegistration.eventInitial;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.dhis2.R;
import org.dhis2.commons.di.dagger.PerActivity;
import org.dhis2.commons.matomo.MatomoAnalyticsController;
import org.dhis2.commons.prefs.PreferenceProvider;
import org.dhis2.commons.prefs.PreferenceProviderImpl;
import org.dhis2.commons.resources.ColorUtils;
import org.dhis2.commons.resources.MetadataIconProvider;
import org.dhis2.commons.resources.ResourceManager;
import org.dhis2.commons.schedulers.SchedulerProvider;
import org.dhis2.data.forms.EventRepository;
import org.dhis2.data.forms.FormRepository;
import org.dhis2.form.data.RulesRepository;
import org.dhis2.form.data.RulesUtilsProvider;
import org.dhis2.form.data.metadata.FileResourceConfiguration;
import org.dhis2.form.data.metadata.OptionSetConfiguration;
import org.dhis2.form.data.metadata.OrgUnitConfiguration;
import org.dhis2.form.ui.FieldViewModelFactory;
import org.dhis2.form.ui.FieldViewModelFactoryImpl;
import org.dhis2.form.ui.LayoutProviderImpl;
import org.dhis2.form.ui.provider.AutoCompleteProviderImpl;
import org.dhis2.form.ui.provider.DisplayNameProviderImpl;
import org.dhis2.form.ui.provider.HintProviderImpl;
import org.dhis2.form.ui.provider.KeyboardActionProviderImpl;
import org.dhis2.form.ui.provider.LegendValueProviderImpl;
import org.dhis2.form.ui.provider.UiEventTypesProviderImpl;
import org.dhis2.form.ui.provider.UiStyleProviderImpl;
import org.dhis2.form.ui.style.FormUiModelColorFactoryImpl;
import org.dhis2.form.ui.style.LongTextUiColorFactoryImpl;
import org.dhis2.mobileProgramRules.EvaluationType;
import org.dhis2.mobileProgramRules.RuleEngineHelper;
import org.dhis2.usescases.eventsWithoutRegistration.eventCapture.EventFieldMapper;
import org.dhis2.utils.analytics.AnalyticsHelper;
import org.hisp.dhis.android.core.D2;

import dagger.Module;
import dagger.Provides;

@Module
public class EventInitialModule {

    private final EventInitialContract.View view;
    private final String stageUid;
    @Nullable
    private final String eventUid;
    private final Context activityContext;

    public EventInitialModule(@NonNull EventInitialContract.View view,
                              @Nullable String eventUid,
                              String stageUid,
                              Context context) {
        this.view = view;
        this.eventUid = eventUid;
        this.stageUid = stageUid;
        this.activityContext = context;
    }

    @Provides
    @PerActivity
    EventInitialPresenter providesPresenter(@NonNull RulesUtilsProvider rulesUtilsProvider,
                                            @NonNull EventInitialRepository eventInitialRepository,
                                            @NonNull SchedulerProvider schedulerProvider,
                                            @NonNull PreferenceProvider preferenceProvider,
                                            @NonNull AnalyticsHelper analyticsHelper,
                                            @NonNull MatomoAnalyticsController matomoAnalyticsController,
                                            @NonNull EventFieldMapper eventFieldMapper) {
        return new EventInitialPresenter(
                view,
                rulesUtilsProvider,
                eventInitialRepository,
                schedulerProvider,
                preferenceProvider,
                analyticsHelper,
                matomoAnalyticsController,
                eventFieldMapper);
    }

    @Provides
    @PerActivity
    EventFieldMapper provideFieldMapper(Context context, FieldViewModelFactory fieldFactory) {
        return new EventFieldMapper(fieldFactory, context.getString(R.string.field_is_mandatory));
    }

    @Provides
    @PerActivity
    FieldViewModelFactory fieldFactory(Context context, D2 d2, ResourceManager resourceManager, ColorUtils colorUtils) {
        return new FieldViewModelFactoryImpl(
                new UiStyleProviderImpl(
                        new FormUiModelColorFactoryImpl(activityContext, colorUtils),
                        new LongTextUiColorFactoryImpl(activityContext, colorUtils),
                        true
                ),
                new LayoutProviderImpl(),
                new HintProviderImpl(context),
                new DisplayNameProviderImpl(
                        new OptionSetConfiguration(d2),
                        new OrgUnitConfiguration(d2),
                        new FileResourceConfiguration(d2)
                ),
                new UiEventTypesProviderImpl(),
                new KeyboardActionProviderImpl(),
                new LegendValueProviderImpl(d2, resourceManager),
                new AutoCompleteProviderImpl(new PreferenceProviderImpl(context))
        );
    }

    @Provides
    FormRepository formRepository(@NonNull RulesRepository rulesRepository,
                                  @NonNull D2 d2) {
        return new EventRepository(rulesRepository, eventUid, d2);
    }

    @Provides
    RulesRepository rulesRepository(@NonNull D2 d2) {
        return new RulesRepository(d2);
    }

    @Provides
    @PerActivity
    EventInitialRepository eventDetailRepository(
            D2 d2,
            @NonNull FieldViewModelFactory fieldViewModelFactory,
            @Nullable RuleEngineHelper ruleEngineHelper,
            MetadataIconProvider metadataIconProvider
    ) {
        return new EventInitialRepositoryImpl(eventUid,
                stageUid,
                d2,
                fieldViewModelFactory,
                ruleEngineHelper,
                metadataIconProvider);
    }

    @Provides
    @PerActivity
    @Nullable
    RuleEngineHelper ruleEngineRepository(D2 d2) {
        if (eventUid == null) return null;
        return new RuleEngineHelper(
                new EvaluationType.Event(eventUid),
                new org.dhis2.mobileProgramRules.RulesRepository(d2)
        );
    }
}
