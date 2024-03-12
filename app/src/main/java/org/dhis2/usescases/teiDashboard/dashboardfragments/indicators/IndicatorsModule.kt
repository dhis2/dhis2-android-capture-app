package org.dhis2.usescases.teiDashboard.dashboardfragments.indicators

import dagger.Module
import dagger.Provides
import dhis2.org.analytics.charts.Charts
import org.dhis2.commons.di.dagger.PerFragment
import org.dhis2.commons.resources.ResourceManager
import org.dhis2.commons.schedulers.SchedulerProvider
import org.dhis2.data.forms.dataentry.RuleEngineRepository
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
        schedulerProvider: SchedulerProvider,
        indicatorRepository: IndicatorRepository,
    ): IndicatorsPresenter {
        return IndicatorsPresenter(schedulerProvider, view, indicatorRepository)
    }

    @Provides
    @PerFragment
    fun provideRepository(
        d2: D2,
        ruleEngineRepository: RuleEngineRepository,
        charts: Charts?,
        resourceManager: ResourceManager,
    ): IndicatorRepository {
        return if (visualizationType == VisualizationType.TRACKER) {
            TrackerAnalyticsRepository(
                d2,
                ruleEngineRepository,
                charts,
                programUid,
                recordUid,
                resourceManager,
            )
        } else {
            EventIndicatorRepository(
                d2,
                ruleEngineRepository,
                programUid,
                recordUid,
                resourceManager,
            )
        }
    }
}
