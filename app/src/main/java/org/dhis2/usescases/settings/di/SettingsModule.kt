package org.dhis2.usescases.settings.di

import kotlinx.coroutines.Dispatchers
import org.dhis2.R
import org.dhis2.commons.resources.ResourceManager
import org.dhis2.commons.viewmodel.DispatcherProvider
import org.dhis2.mobile.commons.error.HttpStatusMessageProvider
import org.dhis2.usescases.settings.SettingsRepository
import org.dhis2.usescases.settings.domain.GetSharedData
import org.dhis2.usescases.settings.domain.GetSyncErrors
import org.dhis2.usescases.settings.models.ErrorModelMapper
import org.dhis2.usescases.settings.ui.viewmodels.SyncErrorLogViewModel
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val settingsModule =
    module {

        factory<DispatcherProvider> {
            object : DispatcherProvider {
                override fun io() = Dispatchers.IO

                override fun computation() = Dispatchers.Unconfined

                override fun ui() = Dispatchers.Main
            }
        }

        factoryOf(::SettingsRepository)
        factory<ErrorModelMapper> {
            val resourceManager: ResourceManager = get()
            ErrorModelMapper(
                fkMessage = resourceManager.getString(R.string.fk_message),
                httpStatusMessageProvider = HttpStatusMessageProvider(),
            )
        }
        factoryOf(::GetSharedData)
        factoryOf(::GetSyncErrors)
        viewModelOf(::SyncErrorLogViewModel)
    }
