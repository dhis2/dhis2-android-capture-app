package org.dhis2.mobile.login.authentication.di

import org.dhis2.mobile.login.main.di.fakeTwoFARepositoryModule
import org.koin.core.module.Module

internal actual val twoFARepositoryModule: Module
    get() = fakeTwoFARepositoryModule
