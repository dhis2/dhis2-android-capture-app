package dhis2.org.analytics.charts.di

import dagger.Component
import dagger.Module
import dagger.Provides
import dhis2.org.analytics.charts.Charts
import dhis2.org.analytics.charts.ChartsRepository
import dhis2.org.analytics.charts.ChartsRepositoryImpl
import dhis2.org.analytics.charts.DhisAnalyticCharts
import dhis2.org.analytics.charts.providers.NutritionDataProvider
import dhis2.org.analytics.charts.providers.RuleEngineNutritionDataProviderImpl
import javax.inject.Singleton
import org.hisp.dhis.android.core.D2

@Singleton
@Component(
    modules = [ChartsModule::class],
    dependencies = [Charts.Dependencies::class]
)
interface ChartsComponent {
    fun charts(): Charts
}

@Module
class ChartsModule {

    @Provides
    internal fun provideChartRepository(
        d2: D2,
        nutritionDataProvider: NutritionDataProvider
    ): ChartsRepository =
        ChartsRepositoryImpl(d2, nutritionDataProvider)

    @Provides
    internal fun nutritionDataProvider(): NutritionDataProvider {
        return RuleEngineNutritionDataProviderImpl()
    }

    @Provides
    internal fun bindStorageFeatureImpl(analyticsCharts: DhisAnalyticCharts): Charts =
        analyticsCharts
}
