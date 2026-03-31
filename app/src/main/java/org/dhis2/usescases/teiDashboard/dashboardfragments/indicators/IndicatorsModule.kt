package org.dhis2.usescases.teiDashboard.dashboardfragments.indicators

import dagger.Module
import dagger.Provides
import dhis2.org.analytics.charts.Charts
import org.dhis2.commons.di.dagger.PerFragment
import org.dhis2.commons.resources.ResourceManager
import org.dhis2.commons.viewmodel.DispatcherProvider
import org.dhis2.mobileProgramRules.RuleEngineHelper
import org.hisp.dhis.android.core.D2

@Module
class IndicatorsModule(
    val programUid: String,
    val recordUid: String,
    val view: IndicatorsView,
    private val visualizationType: VisualizationType,
) {
    @Provides
    @PerFragment
    fun providesPresenter(
        dispatcherProvider: DispatcherProvider,
        indicatorRepository: IndicatorRepository,
    ): IndicatorsPresenter = IndicatorsPresenter(dispatcherProvider, view, indicatorRepository)

    @Provides
    @PerFragment
    fun provideRepository(
        d2: D2,
        ruleEngineHelper: RuleEngineHelper?,
        charts: Charts?,
        resourceManager: ResourceManager,
    ): IndicatorRepository =
        if (visualizationType == VisualizationType.TRACKER) {
            TrackerAnalyticsRepository(
                d2,
                ruleEngineHelper,
                charts,
                programUid,
                recordUid,
                resourceManager,
            )
        } else {
            EventIndicatorRepository(
                d2,
                ruleEngineHelper,
                programUid,
                recordUid,
                resourceManager,
            )
        }
}
