package org.dhis2.usescases.teiDashboard

import dagger.Module
import dagger.Provides
import dhis2.org.analytics.charts.Charts
import org.dhis2.commons.data.ProgramConfigurationRepository
import org.dhis2.commons.di.dagger.PerActivity
import org.dhis2.commons.featureconfig.data.FeatureConfigRepository
import org.dhis2.commons.matomo.MatomoAnalyticsController
import org.dhis2.commons.prefs.PreferenceProvider
import org.dhis2.commons.resources.EventResourcesProvider
import org.dhis2.commons.resources.MetadataIconProvider
import org.dhis2.commons.resources.ResourceManager
import org.dhis2.commons.schedulers.SchedulerProvider
import org.dhis2.commons.viewmodel.DispatcherProvider
import org.dhis2.form.data.metadata.EnrollmentConfiguration
import org.dhis2.mobileProgramRules.EvaluationType
import org.dhis2.mobileProgramRules.RuleEngineHelper
import org.dhis2.usescases.enrollment.DateEditionWarningHandler
import org.dhis2.utils.analytics.AnalyticsHelper
import org.dhis2.utils.customviews.navigationbar.NavigationPageConfigurator
import org.hisp.dhis.android.core.D2

@Module
class TeiDashboardModule(
    private val view: TeiDashboardContracts.View,
    val teiUid: String,
    val programUid: String?,
    private val enrollmentUid: String?,
    private val isPortrait: Boolean,
) {
    @Provides
    @PerActivity
    fun provideView(mobileActivity: TeiDashboardMobileActivity): TeiDashboardContracts.View = mobileActivity

    @Provides
    @PerActivity
    fun providePresenter(
        dashboardRepository: DashboardRepository,
        schedulerProvider: SchedulerProvider,
        analyticsHelper: AnalyticsHelper,
        preferenceProvider: PreferenceProvider,
        matomoAnalyticsController: MatomoAnalyticsController,
    ): TeiDashboardContracts.Presenter =
        TeiDashboardPresenter(
            view,
            programUid,
            dashboardRepository,
            schedulerProvider,
            analyticsHelper,
            preferenceProvider,
            matomoAnalyticsController,
        )

    @Provides
    @PerActivity
    fun provideEnrollmentConfiguration(
        d2: D2,
        dispatcher: DispatcherProvider,
    ) = enrollmentUid?.let { EnrollmentConfiguration(d2, it, dispatcher) }

    @Provides
    @PerActivity
    fun provideDateEditionWarningHandler(
        enrollmentConfiguration: EnrollmentConfiguration?,
        eventResourcesProvider: EventResourcesProvider,
    ) = DateEditionWarningHandler(
        enrollmentConfiguration,
        eventResourcesProvider,
    )

    @Provides
    @PerActivity
    fun dashboardRepository(
        d2: D2,
        charts: Charts,
        preferenceProvider: PreferenceProvider,
        teiAttributesProvider: TeiAttributesProvider,
        metadataIconProvider: MetadataIconProvider,
        programConfigurationRepository: ProgramConfigurationRepository,
        featureConfigRepository: FeatureConfigRepository,
    ): DashboardRepository =
        DashboardRepositoryImpl(
            d2,
            charts,
            teiUid,
            programUid,
            enrollmentUid,
            teiAttributesProvider,
            preferenceProvider,
            metadataIconProvider,
            programConfigurationRepository,
            featureConfigRepository,
        )

    @Provides
    @PerActivity
    fun ruleEngineRepository(d2: D2): RuleEngineHelper? {
        if (enrollmentUid.isNullOrEmpty()) return null
        return RuleEngineHelper(
            EvaluationType.Enrollment(enrollmentUid),
            org.dhis2.mobileProgramRules.RulesRepository(d2),
        )
    }

    @Provides
    @PerActivity
    fun pageConfigurator(dashboardRepository: DashboardRepository): NavigationPageConfigurator =
        TeiDashboardPageConfigurator(dashboardRepository, isPortrait)

    @Provides
    @PerActivity
    fun teiAttributesProvider(d2: D2): TeiAttributesProvider = TeiAttributesProvider(d2)

    @Provides
    @PerActivity
    fun providesViewModelFactory(
        repository: DashboardRepository,
        analyticsHelper: AnalyticsHelper,
        dispatcher: DispatcherProvider,
        pageConfigurator: NavigationPageConfigurator,
        resourcesManager: ResourceManager,
    ): DashboardViewModelFactory =
        DashboardViewModelFactory(
            repository,
            analyticsHelper,
            dispatcher,
            pageConfigurator,
            resourcesManager,
        )

    @Provides
    fun provideProgramConfigurationRepository(d2: D2): ProgramConfigurationRepository = ProgramConfigurationRepository(d2)
}
