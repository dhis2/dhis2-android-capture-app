package org.dhis2.mobile.login.pin.di

import org.dhis2.mobile.login.pin.domain.usecase.ForgotPinUseCase
import org.dhis2.mobile.login.pin.domain.usecase.GetIsSessionLockedUseCase
import org.dhis2.mobile.login.pin.domain.usecase.SavePinUseCase
import org.dhis2.mobile.login.pin.domain.usecase.ValidatePinUseCase
import org.dhis2.mobile.login.pin.ui.provider.PinResourceProvider
import org.dhis2.mobile.login.pin.ui.viewmodel.PinViewModel
import org.koin.core.module.Module
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

/**
 * Common PIN module containing use cases and ViewModel.
 */
internal val pinModule =
    module {
        // PIN use cases
        factoryOf(::SavePinUseCase)
        factoryOf(::ValidatePinUseCase)
        factoryOf(::ForgotPinUseCase)
        factoryOf(::GetIsSessionLockedUseCase)

        // PIN resource provider
        singleOf(::PinResourceProvider)

        // PIN ViewModel
        viewModelOf(::PinViewModel)
    }

/**
 * Platform-specific PIN module.
 * Expected to be implemented in androidMain, iosMain, etc.
 */
internal expect val pinDataModule: Module

/**
 * Complete PIN module including common and platform-specific parts.
 */
val completePinModule =
    module {
        includes(pinModule, pinDataModule)
    }
