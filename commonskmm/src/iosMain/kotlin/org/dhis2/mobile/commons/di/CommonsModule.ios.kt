package org.dhis2.mobile.commons.di

import kotlinx.coroutines.flow.flowOf
import org.dhis2.mobile.commons.coroutine.Dispatcher
import org.dhis2.mobile.commons.network.NetworkStatusProvider
import org.koin.core.module.Module
import org.koin.dsl.module

actual val commonsModule: Module
    get() =
        module {
            factory { Dispatcher() }
            // TODO: Observe the real connection status (NWPathMonitor)
            single<NetworkStatusProvider> {
                object : NetworkStatusProvider {
                    override val connectionStatus = flowOf(true)
                }
            }
        }
