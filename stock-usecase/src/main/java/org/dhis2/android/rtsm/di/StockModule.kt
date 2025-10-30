package org.dhis2.android.rtsm.di

import dhis2.org.analytics.charts.Charts
import dhis2.org.analytics.charts.DhisAnalyticCharts
import io.reactivex.disposables.CompositeDisposable
import org.dhis2.android.rtsm.coroutines.StockDispatcherProvider
import org.dhis2.android.rtsm.services.AnalyticsDependencies
import org.dhis2.android.rtsm.services.MetadataManager
import org.dhis2.android.rtsm.services.MetadataManagerImpl
import org.dhis2.android.rtsm.services.SpeechRecognitionManager
import org.dhis2.android.rtsm.services.SpeechRecognitionManagerImpl
import org.dhis2.android.rtsm.services.StockManager
import org.dhis2.android.rtsm.services.StockManagerImpl
import org.dhis2.android.rtsm.services.StockTableDimensionStore
import org.dhis2.android.rtsm.services.rules.RuleValidationHelper
import org.dhis2.android.rtsm.services.rules.RuleValidationHelperImpl
import org.dhis2.android.rtsm.services.scheduler.BaseSchedulerProvider
import org.dhis2.android.rtsm.services.scheduler.SchedulerProviderImpl
import org.dhis2.android.rtsm.ui.home.HomeViewModel
import org.dhis2.android.rtsm.ui.managestock.ManageStockViewModel
import org.dhis2.android.rtsm.ui.managestock.TableModelMapper
import org.dhis2.commons.featureconfig.data.FeatureConfigRepository
import org.dhis2.commons.featureconfig.data.FeatureConfigRepositoryImpl
import org.dhis2.commons.resources.ColorUtils
import org.dhis2.commons.resources.ResourceManager
import org.dhis2.commons.viewmodel.DispatcherProvider
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val stockModule =
    module {
        factory {
            CompositeDisposable()
        }

        factory<DispatcherProvider> {
            StockDispatcherProvider()
        }

        factory {
            ColorUtils()
        }

        factory<SpeechRecognitionManager> {
            SpeechRecognitionManagerImpl(get())
        }

        factory {
            ResourceManager(get(), get())
        }

        factory<FeatureConfigRepository> {
            FeatureConfigRepositoryImpl(get())
        }

        factory<Charts.Dependencies> {
            AnalyticsDependencies(get(), get(), get(), get(), get())
        }

        factory<Charts> {
            DhisAnalyticCharts.Provider.get(get())
        }

        factory<BaseSchedulerProvider> {
            SchedulerProviderImpl()
        }

        factory<RuleValidationHelper> {
            RuleValidationHelperImpl(get())
        }

        factory<MetadataManager> {
            MetadataManagerImpl(get(), get())
        }

        factory<StockManager> {
            StockManagerImpl(get(), get(), get(), get(), get())
        }

        viewModel { params ->
            HomeViewModel(get(), get(), get(), get(), get(), params.get())
        }

        factory {
            StockTableDimensionStore(get())
        }

        factory {
            StockDispatcherProvider()
        }

        factory {
            TableModelMapper(get())
        }

        viewModel {
            ManageStockViewModel(get(), get(), get(), get(), get(), get(), get(), get(), get(), get())
        }
    }
