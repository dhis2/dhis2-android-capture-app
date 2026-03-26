package org.dhis2.usescases.main

import android.content.Context
import androidx.fragment.app.FragmentManager
import androidx.work.WorkManager
import kotlinx.coroutines.Dispatchers
import org.dhis2.commons.filters.FilterManager
import org.dhis2.commons.resources.LocaleSelector
import org.dhis2.commons.resources.MetadataIconProvider
import org.dhis2.commons.resources.ResourceManager
import org.dhis2.commons.viewmodel.DispatcherProvider
import org.dhis2.data.service.VersionRepository
import org.dhis2.data.service.workManager.WorkManagerController
import org.dhis2.data.service.workManager.WorkManagerControllerImpl
import org.dhis2.usescases.main.data.HomeRepository
import org.dhis2.usescases.main.data.HomeRepositoryImpl
import org.dhis2.usescases.main.domain.CheckSingleNavigation
import org.dhis2.usescases.main.domain.ConfigureHomeNavigationBar
import org.dhis2.usescases.main.domain.DeleteAccount
import org.dhis2.usescases.main.domain.DownloadNewVersion
import org.dhis2.usescases.main.domain.GetHomeFilters
import org.dhis2.usescases.main.domain.GetLockAction
import org.dhis2.usescases.main.domain.GetUserName
import org.dhis2.usescases.main.domain.LaunchInitialSync
import org.dhis2.usescases.main.domain.LogoutUser
import org.dhis2.usescases.main.domain.ScheduleNewVersionAlert
import org.dhis2.usescases.main.domain.UpdateInitialSyncStatus
import org.dhis2.usescases.troubleshooting.TroubleshootingRepository
import org.dhis2.usescases.troubleshooting.TroubleshootingViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModel
import org.koin.core.parameter.parametersOf
import org.koin.dsl.module

val mainModule = module {

    val dispatcher = object : DispatcherProvider {
        override fun io() = Dispatchers.IO
        override fun computation() = Dispatchers.Unconfined
        override fun ui() = Dispatchers.Main
    }

    factory<WorkManagerController> {
        WorkManagerControllerImpl(WorkManager.getInstance(androidContext()))
    }

    factory<HomeRepository> {
        HomeRepositoryImpl(
            d2 = get(),
            charts = get(),
            preferences = get(),
            workManagerController = get(),
            syncStatusController = get(),
            domainErrorMapper = get(),
            dispatcher = get(),
        )
    }
    factory {
        FilterManager.getInstance()
    }
    singleOf(::VersionRepository)
    factoryOf(::ResourceManager)
    single { params ->
        MainNavigator(
            dispatcherProvider = dispatcher,
            fragmentManager = params.get(),
        )
    }
    factory { params ->
        GetUserName(
            homeRepository = get { parametersOf(params.get()) }
        )
    }
    factory { params ->
        ConfigureHomeNavigationBar(
            homeRepository = get { parametersOf(params.get()) },
            resourceManager = get()
        )
    }
    factoryOf(::GetHomeFilters)
    factoryOf(::DownloadNewVersion)
    factoryOf(::LogoutUser)
    factoryOf(::DeleteAccount)
    factoryOf(::GetLockAction)
    factoryOf(::UpdateInitialSyncStatus)
    factoryOf(::CheckSingleNavigation)
    factoryOf(::LaunchInitialSync)
    factory {
        ScheduleNewVersionAlert(
            workManagerController = get(),
            versionRepository = get()
        )
    }

    viewModel { params ->
        val context = params.get<Context>()
        val fragmentManager: FragmentManager = params.get()
        val skipInitialSync = params.get<Boolean>()
        val initialScreen = params.get<MainScreenType>()

        MainViewModel(
            preferences = get { parametersOf(context) },
            filterManager = get(),
            matomoAnalyticsController = get(),
            syncStatusController = get(),
            mainNavigator = get { parametersOf(fragmentManager) },
            getUserName = get { parametersOf(context) },
            configureHomeNavigationBar = get { parametersOf(context) },
            getHomeFilters = get(),
            downloadNewVersion = get(),
            logOutUser = get { parametersOf(context) },
            deleteAccount = get(),
            getLockAction = get(),
            updateInitialSyncStatus = get(),
            checkSingleNavigation = get { parametersOf(skipInitialSync) },
            launchInitialSync = get(),
            scheduleNewVersionAlert = get(),
            syncBackgroundJobAction = get(),
            initialScreen = initialScreen,
            dispatcher = dispatcher,
        )
    }

    factoryOf(::MetadataIconProvider)
    factoryOf(::TroubleshootingRepository)
    factory {
        LocaleSelector(androidContext(), get())
    }
    viewModel { params ->
        TroubleshootingViewModel(
            localeSelector = get(),
            repository = get(),
            openLanguageSection = params.get()
        )
    }
}
