package org.dhis2.mobile.login.main.di

import org.koin.core.module.Module

internal actual val accountModule: Module
    get() = fakeAccountModule
