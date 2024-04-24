package dhis2.org.analytics.charts.di

import android.content.Context
import dagger.Component
import dagger.Module
import dagger.Provides
import dhis2.org.analytics.charts.Charts
import dhis2.org.analytics.charts.ChartsRepository
import dhis2.org.analytics.charts.ChartsRepositoryImpl
import dhis2.org.analytics.charts.DhisAnalyticCharts
import dhis2.org.analytics.charts.data.AnalyticResources
import dhis2.org.analytics.charts.mappers.AnalyticDataElementToDataElementData
import dhis2.org.analytics.charts.mappers.AnalyticIndicatorToIndicatorData
import dhis2.org.analytics.charts.mappers.AnalyticTeiSettingsToSettingsAnalyticsModel
import dhis2.org.analytics.charts.mappers.AnalyticsTeiSettingsToGraph
import dhis2.org.analytics.charts.mappers.DataElementToGraph
import dhis2.org.analytics.charts.mappers.ProgramIndicatorToGraph
import dhis2.org.analytics.charts.mappers.VisualizationToGraph
import dhis2.org.analytics.charts.providers.AnalyticsFilterProvider
import dhis2.org.analytics.charts.providers.ChartCoordinatesProvider
import dhis2.org.analytics.charts.providers.ChartCoordinatesProviderImpl
import dhis2.org.analytics.charts.providers.NutritionDataProvider
import dhis2.org.analytics.charts.providers.PeriodStepProvider
import dhis2.org.analytics.charts.providers.PeriodStepProviderImpl
import dhis2.org.analytics.charts.providers.RuleEngineNutritionDataProviderImpl
import org.dhis2.commons.resources.ColorUtils
import org.dhis2.commons.resources.ResourceManager
import org.hisp.dhis.android.core.D2
import javax.inject.Singleton

@Singleton
@Component(
    modules = [ChartsModule::class],
    dependencies = [Charts.Dependencies::class],
)
interface ChartsComponent {
    fun charts(): Charts
}

@Module
class ChartsModule {

    @Provides
    internal fun provideChartRepository(
        d2: D2,
        visualizationToGraph: VisualizationToGraph,
        analyticsTeiSettingsToGraph: AnalyticsTeiSettingsToGraph,
        dataElementToGraph: DataElementToGraph,
        indicatorToGraph: ProgramIndicatorToGraph,
        analyticsResources: AnalyticResources,
        analyticsFilterProvider: AnalyticsFilterProvider,
    ): ChartsRepository = ChartsRepositoryImpl(
        d2,
        visualizationToGraph,
        analyticsTeiSettingsToGraph,
        dataElementToGraph,
        indicatorToGraph,
        analyticsResources,
        analyticsFilterProvider,
    )

    @Provides
    internal fun provideAnalyticFilters(d2: D2): AnalyticsFilterProvider {
        return AnalyticsFilterProvider(d2)
    }

    @Provides
    internal fun provideVisualizationToGraph(
        periodStepProvider: PeriodStepProvider,
        chartCoordinatesProvider: ChartCoordinatesProvider,
    ): VisualizationToGraph {
        return VisualizationToGraph(periodStepProvider, chartCoordinatesProvider)
    }

    @Provides
    internal fun provideAnalyticsSettingsToGraph(
        analyticsSettingsMapper: AnalyticTeiSettingsToSettingsAnalyticsModel,
        nutritionDataProvider: NutritionDataProvider,
        periodStepProvider: PeriodStepProvider,
        chartCoordinatesProvider: ChartCoordinatesProvider,
    ): AnalyticsTeiSettingsToGraph {
        return AnalyticsTeiSettingsToGraph(
            analyticsSettingsMapper,
            nutritionDataProvider,
            periodStepProvider,
            chartCoordinatesProvider,
        )
    }

    @Provides
    internal fun nutritionDataProvider(): NutritionDataProvider {
        return RuleEngineNutritionDataProviderImpl()
    }

    @Provides
    internal fun provideAnalyticSettingsMapper(
        analyticDataElementMapper: AnalyticDataElementToDataElementData,
        analyticIndicatorMapper: AnalyticIndicatorToIndicatorData,
    ): AnalyticTeiSettingsToSettingsAnalyticsModel {
        return AnalyticTeiSettingsToSettingsAnalyticsModel(
            analyticDataElementMapper,
            analyticIndicatorMapper,
        )
    }

    @Provides
    internal fun provideDataElementToGraph(
        periodStepProvider: PeriodStepProvider,
        chartCoordinatesProvider: ChartCoordinatesProvider,
    ): DataElementToGraph {
        return DataElementToGraph(periodStepProvider, chartCoordinatesProvider)
    }

    @Provides
    internal fun provideIndicatorToGraph(
        periodStepProvider: PeriodStepProvider,
        chartCoordinatesProvider: ChartCoordinatesProvider,
    ): ProgramIndicatorToGraph {
        return ProgramIndicatorToGraph(periodStepProvider, chartCoordinatesProvider)
    }

    @Provides
    internal fun periodStepProvider(d2: D2): PeriodStepProvider {
        return PeriodStepProviderImpl(d2)
    }

    @Provides
    internal fun chartCoordinatesProvider(
        d2: D2,
        periodStepProvider: PeriodStepProvider,
        resourceManager: ResourceManager,
    ): ChartCoordinatesProvider {
        return ChartCoordinatesProviderImpl(d2, periodStepProvider, resourceManager)
    }

    @Provides
    internal fun provideResourceManager(context: Context, colorUtils: ColorUtils): ResourceManager {
        return ResourceManager(context, colorUtils)
    }

    @Provides
    internal fun analyticResources(context: Context): AnalyticResources {
        return AnalyticResources(context)
    }

    @Provides
    internal fun provideAnalyticDataElementMapper(): AnalyticDataElementToDataElementData {
        return AnalyticDataElementToDataElementData()
    }

    @Provides
    internal fun provideAnalyticIndicatorMapper(): AnalyticIndicatorToIndicatorData {
        return AnalyticIndicatorToIndicatorData()
    }

    @Provides
    internal fun bindStorageFeatureImpl(analyticsCharts: DhisAnalyticCharts): Charts =
        analyticsCharts
}
