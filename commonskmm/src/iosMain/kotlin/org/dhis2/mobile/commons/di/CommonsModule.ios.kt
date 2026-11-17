package org.dhis2.mobile.commons.di

import org.koin.core.module.Module
import org.koin.dsl.module

actual val commonsModule: Module
    get() = module { includes(sharedCommonsModule, sdklessCommonsModule) }
