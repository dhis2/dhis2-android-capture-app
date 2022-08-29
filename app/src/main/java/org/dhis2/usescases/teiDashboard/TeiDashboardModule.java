package org.dhis2.usescases.teiDashboard;

import androidx.annotation.NonNull;

import org.dhis2.commons.di.dagger.PerActivity;
import org.dhis2.commons.prefs.PreferenceProvider;
import org.dhis2.data.forms.EnrollmentFormRepository;
import org.dhis2.data.forms.FormRepository;
import org.dhis2.form.data.RulesRepository;
import org.dhis2.data.forms.dataentry.EnrollmentRuleEngineRepository;
import org.dhis2.data.forms.dataentry.RuleEngineRepository;
import org.dhis2.commons.schedulers.SchedulerProvider;
import org.dhis2.utils.analytics.AnalyticsHelper;
import org.dhis2.commons.matomo.MatomoAnalyticsController;
import org.dhis2.utils.customviews.navigationbar.NavigationPageConfigurator;
import org.dhis2.commons.filters.FilterManager;
import org.dhis2.commons.resources.ResourceManager;
import org.hisp.dhis.android.core.D2;

import dagger.Module;
import dagger.Provides;
import dhis2.org.analytics.charts.Charts;

/**
 * QUADRAM. Created by ppajuelo on 30/11/2017.
 */
@Module
public class TeiDashboardModule {

    public final String programUid;
    public final String teiUid;
    private final TeiDashboardContracts.View view;
    private final String enrollmentUid;
    private final boolean isPortrait;

    public TeiDashboardModule(TeiDashboardContracts.View view, String teiUid, String programUid, String enrollmentUid, boolean isPortrait) {
        this.view = view;
        this.teiUid = teiUid;
        this.programUid = programUid;
        this.enrollmentUid = enrollmentUid;
        this.isPortrait = isPortrait;
    }

    @Provides
    @PerActivity
    TeiDashboardContracts.View provideView(TeiDashboardMobileActivity mobileActivity) {
        return mobileActivity;
    }

    @Provides
    @PerActivity
    TeiDashboardContracts.Presenter providePresenter(DashboardRepository dashboardRepository,
                                                     SchedulerProvider schedulerProvider,
                                                     AnalyticsHelper analyticsHelper,
                                                     PreferenceProvider preferenceProvider,
                                                     FilterManager filterManager,
                                                     MatomoAnalyticsController matomoAnalyticsController) {
        return new TeiDashboardPresenter(view,
                teiUid,
                programUid,
                enrollmentUid,
                dashboardRepository,
                schedulerProvider,
                analyticsHelper,
                preferenceProvider,
                filterManager,
                matomoAnalyticsController);
    }

    @Provides
    @PerActivity
    DashboardRepository dashboardRepository(D2 d2, Charts charts, ResourceManager resources, TeiAttributesProvider teiAttributesProvider) {
        return new DashboardRepositoryImpl(d2, charts, teiUid, programUid, enrollmentUid, resources, teiAttributesProvider);
    }

    @Provides
    @PerActivity
    RulesRepository rulesRepository(@NonNull D2 d2) {
        return new RulesRepository(d2);
    }

    @Provides
    @PerActivity
    FormRepository formRepository(
            @NonNull RulesRepository rulesRepository,
            D2 d2) {
        String enrollmentUidToUse = enrollmentUid != null ? enrollmentUid : "";
        return new EnrollmentFormRepository(rulesRepository, enrollmentUidToUse, d2);
    }

    @Provides
    @PerActivity
    RuleEngineRepository ruleEngineRepository(D2 d2, FormRepository formRepository) {
        String enrollmentUidToUse = enrollmentUid != null ? enrollmentUid : "";
        return new EnrollmentRuleEngineRepository(formRepository, enrollmentUidToUse, d2);
    }

    @Provides
    @PerActivity
    NavigationPageConfigurator pageConfigurator(DashboardRepository dashboardRepository) {
        return new TeiDashboardPageConfigurator(dashboardRepository, isPortrait);
    }

    @Provides
    @PerActivity
    TeiAttributesProvider teiAttributesProvider(D2 d2) {
        return new TeiAttributesProvider(d2);
    }
}
