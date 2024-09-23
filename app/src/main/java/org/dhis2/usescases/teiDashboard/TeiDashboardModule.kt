package org.dhis2.usescases.teiDashboard

import dagger.Module
import dagger.Provides
import dhis2.org.analytics.charts.Charts
import org.dhis2.commons.di.dagger.PerActivity
import org.dhis2.commons.matomo.MatomoAnalyticsController
import org.dhis2.commons.prefs.PreferenceProvider
import org.dhis2.commons.resources.MetadataIconProvider
import org.dhis2.commons.schedulers.SchedulerProvider
import org.dhis2.commons.viewmodel.DispatcherProvider
import org.dhis2.data.forms.EnrollmentFormRepository
import org.dhis2.data.forms.FormRepository
import org.dhis2.form.data.RulesRepository
import org.dhis2.mobileProgramRules.EvaluationType
import org.dhis2.mobileProgramRules.RuleEngineHelper
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
    fun provideView(mobileActivity: TeiDashboardMobileActivity): TeiDashboardContracts.View {
        return mobileActivity
    }

    @Provides
    @PerActivity
    fun providePresenter(
        dashboardRepository: DashboardRepository,
        schedulerProvider: SchedulerProvider,
        analyticsHelper: AnalyticsHelper,
        preferenceProvider: PreferenceProvider,
        matomoAnalyticsController: MatomoAnalyticsController,
    ): TeiDashboardContracts.Presenter {
        return TeiDashboardPresenter(
            view,
            programUid,
            dashboardRepository,
            schedulerProvider,
            analyticsHelper,
            preferenceProvider,
            matomoAnalyticsController,
        )
    }

    @Provides
    @PerActivity
    fun dashboardRepository(
        d2: D2,
        charts: Charts,
        preferenceProvider: PreferenceProvider,
        teiAttributesProvider: TeiAttributesProvider,
        metadataIconProvider: MetadataIconProvider,
    ): DashboardRepository {
        return DashboardRepositoryImpl(
            d2,
            charts,
            teiUid,
            programUid,
            enrollmentUid,
            teiAttributesProvider,
            preferenceProvider,
            metadataIconProvider,
        )
    }

    @Provides
    @PerActivity
    fun rulesRepository(d2: D2): RulesRepository {
        return RulesRepository(d2)
    }

    @Provides
    @PerActivity
    fun formRepository(
        rulesRepository: RulesRepository,
        d2: D2,
    ): FormRepository {
        val enrollmentUidToUse = enrollmentUid ?: ""
        return EnrollmentFormRepository(
            rulesRepository,
            enrollmentUidToUse,
            d2,
        )
    }

    @Provides
    @PerActivity
    fun ruleEngineRepository(
        d2: D2,
    ): RuleEngineHelper? {
        if (enrollmentUid.isNullOrEmpty()) return null
        return RuleEngineHelper(
            EvaluationType.Enrollment(enrollmentUid),
            org.dhis2.mobileProgramRules.RulesRepository(d2),
        )
    }

    @Provides
    @PerActivity
    fun pageConfigurator(
        dashboardRepository: DashboardRepository,
    ): NavigationPageConfigurator {
        return TeiDashboardPageConfigurator(dashboardRepository, isPortrait)
    }

    @Provides
    @PerActivity
    fun teiAttributesProvider(d2: D2): TeiAttributesProvider {
        return TeiAttributesProvider(d2)
    }

    @Provides
    @PerActivity
    fun providesViewModelFactory(
        repository: DashboardRepository,
        analyticsHelper: AnalyticsHelper,
        dispatcher: DispatcherProvider,
    ): DashboardViewModelFactory {
        return DashboardViewModelFactory(repository, analyticsHelper, dispatcher)
    }
}
