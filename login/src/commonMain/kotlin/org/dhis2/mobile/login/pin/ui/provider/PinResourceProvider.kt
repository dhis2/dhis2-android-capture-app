package org.dhis2.mobile.login.pin.ui.provider

import org.dhis2.mobile.login.resources.Res
import org.dhis2.mobile.login.resources.pin_error_incorrect
import org.dhis2.mobile.login.resources.pin_error_no_pin
import org.dhis2.mobile.login.resources.pin_error_reset_failed
import org.dhis2.mobile.login.resources.pin_error_save_failed
import org.jetbrains.compose.resources.getString

/**
 * Provides string resources for PIN feature.
 * This class decouples resource access from ViewModels, making testing easier.
 */
class PinResourceProvider {
    /**
     * Returns the error message for incorrect PIN.
     */
    suspend fun getPinErrorIncorrect(): String = getString(Res.string.pin_error_incorrect)

    /**
     * Returns the error message when no PIN is stored.
     */
    suspend fun getPinErrorNoPinStored(): String = getString(Res.string.pin_error_no_pin)

    /**
     * Returns the error message when PIN save fails.
     */
    suspend fun getPinErrorSaveFailed(): String = getString(Res.string.pin_error_save_failed)

    /**
     * Returns the error message when PIN reset fails.
     */
    suspend fun getPinErrorResetFailed(): String = getString(Res.string.pin_error_reset_failed)
}
