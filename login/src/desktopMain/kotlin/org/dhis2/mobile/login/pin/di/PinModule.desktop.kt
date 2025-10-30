package org.dhis2.mobile.login.pin.di

import org.koin.dsl.module

/**
 * Desktop-specific PIN module providing repository implementation.
 * Currently empty as desktop implementation is not yet available.
 */
internal actual val pinDataModule =
    module {
        // TODO: Add desktop-specific SessionRepository implementation when available
    }
