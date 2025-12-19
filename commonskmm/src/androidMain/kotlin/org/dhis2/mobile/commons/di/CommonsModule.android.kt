package org.dhis2.mobile.commons.di

import org.dhis2.mobile.commons.coroutine.Dispatcher
import org.dhis2.mobile.commons.customintents.CustomIntentRepository
import org.dhis2.mobile.commons.customintents.CustomIntentRepositoryImpl
import org.dhis2.mobile.commons.data.TableDimensionRepository
import org.dhis2.mobile.commons.data.TableDimensionRepositoryImpl
import org.dhis2.mobile.commons.data.ValueParser
import org.dhis2.mobile.commons.data.ValueParserImpl
import org.dhis2.mobile.commons.error.DomainErrorMapper
import org.dhis2.mobile.commons.files.FileController
import org.dhis2.mobile.commons.files.FileControllerImpl
import org.dhis2.mobile.commons.files.FileHandler
import org.dhis2.mobile.commons.files.FileHandlerImpl
import org.dhis2.mobile.commons.network.NetworkStatusProvider
import org.dhis2.mobile.commons.network.NetworkStatusProviderImpl
import org.dhis2.mobile.commons.providers.PreferenceProvider
import org.dhis2.mobile.commons.providers.PreferenceProviderImpl
import org.dhis2.mobile.commons.reporting.CrashReportController
import org.dhis2.mobile.commons.reporting.CrashReportControllerImpl
import org.dhis2.mobile.commons.resources.D2ErrorMessageProvider
import org.dhis2.mobile.commons.resources.D2ErrorMessageProviderImpl
import org.koin.core.module.Module
import org.koin.dsl.module

actual val commonsModule: Module
    get() =
        module {
            single<ValueParser> {
                ValueParserImpl(get())
            }
            single<CustomIntentRepository> {
                CustomIntentRepositoryImpl(get())
            }
            single<FileController> {
                FileControllerImpl()
            }
            single<FileHandler> {
                FileHandlerImpl()
            }
            single<CrashReportController> {
                CrashReportControllerImpl(get())
            }

            single<NetworkStatusProvider> {
                NetworkStatusProviderImpl(get())
            }

            single<PreferenceProvider> {
                PreferenceProviderImpl(get())
            }

            factory<D2ErrorMessageProvider> {
                D2ErrorMessageProviderImpl()
            }

            factory<TableDimensionRepository> { params ->
                TableDimensionRepositoryImpl(get(), params.get())
            }
            factory<Dispatcher> {
                Dispatcher()
            }

            factory<DomainErrorMapper> {
                DomainErrorMapper(
                    d2ErrorMessageProvider = get(),
                    networkStatusProvider = get(),
                )
            }
        }
