package org.dhis2.mobile.login.pin.di

import org.dhis2.mobile.login.main.di.fakePinDataModule
import org.koin.core.module.Module

internal actual val pinDataModule: Module
    get() = fakePinDataModule
