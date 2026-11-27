package org.dhis2.mobile.commons.di

import org.dhis2.mobile.commons.coroutine.Dispatcher
import org.dhis2.mobile.commons.session.SessionRenewalNotifier
import org.koin.core.module.Module
import org.koin.dsl.module

expect val commonsModule: Module

/** Bindings that need no platform API. Every platform's [commonsModule] includes them. */
internal val sharedCommonsModule =
    module {
        factory { Dispatcher() }
        single { SessionRenewalNotifier() }
    }
