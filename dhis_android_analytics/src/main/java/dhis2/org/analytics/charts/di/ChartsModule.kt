package dhis2.org.analytics.charts.di

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
import kotlinx.coroutines.Dispatchers
import org.dhis2.commons.viewmodel.DispatcherProvider
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val chartsModule = module {

    factory<PeriodStepProvider> {
        PeriodStepProviderImpl(
            d2 = get(),
            dispatcherProvider = object : DispatcherProvider {
                override fun io() = Dispatchers.IO

                override fun computation() = Dispatchers.Default

                override fun ui() = Dispatchers.Main

            }
        )
    }

    factory<ChartCoordinatesProvider> {
        ChartCoordinatesProviderImpl(
            d2 = get(),
            periodStepProvider = get(),
            resourceManager = get()
        )
    }

    factory {
        VisualizationToGraph(
            periodStepProvider = get(),
            chartCoordinatesProvider = get()
        )
    }

    factoryOf(::AnalyticDataElementToDataElementData)
    factoryOf(::AnalyticIndicatorToIndicatorData)
    factory {
        AnalyticTeiSettingsToSettingsAnalyticsModel(
            analyticDataElementMapper = get(),
            analyticIndicatorMapper = get()
        )
    }

    factory<NutritionDataProvider> {
        RuleEngineNutritionDataProviderImpl()
    }

    factory {
        AnalyticsTeiSettingsToGraph(
            analyticsSettingsMapper = get(),
            nutritionDataProvider = get(),
            periodStepProvider = get(),
            chartCoordinatesProvider = get()
        )
    }

    factory {
        DataElementToGraph(
            periodStepProvider = get(),
            chartCoordinatesProvider = get()
        )
    }

    factory {
        ProgramIndicatorToGraph(
            periodStepProvider = get(),
            chartCoordinatesProvider = get()
        )
    }

    factoryOf(::AnalyticResources)

    factoryOf(::AnalyticsFilterProvider)

    factory<ChartsRepository> {
        ChartsRepositoryImpl(
            d2 = get(),
            visualizationToGraph = get(),
            analyticsTeiSettingsToGraph = get(),
            dataElementToGraph = get(),
            programIndicatorToGraph = get(),
            analyticsResources = get(),
            analyticsFilterProvider = get()
        )
    }
    factory<Charts> {
        DhisAnalyticCharts(
            chartsRepository = get()
        )
    }
}