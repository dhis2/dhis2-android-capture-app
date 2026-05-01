package org.dhis2.mobile.login.pin.ui.provider

import org.dhis2.mobile.login.pin.domain.model.PinMode
import org.dhis2.mobile.login.resources.Res
import org.dhis2.mobile.login.resources.create_pin
import org.dhis2.mobile.login.resources.create_pin_button
import org.dhis2.mobile.login.resources.create_pin_description
import org.dhis2.mobile.login.resources.enter_pin
import org.dhis2.mobile.login.resources.enter_pin_button
import org.dhis2.mobile.login.resources.enter_pin_description
import org.dhis2.mobile.login.resources.forgot_pin_button
import org.dhis2.mobile.login.resources.pin_error_incorrect
import org.dhis2.mobile.login.resources.pin_error_no_pin
import org.dhis2.mobile.login.resources.pin_error_remaining_attempts
import org.dhis2.mobile.login.resources.pin_error_reset_failed
import org.dhis2.mobile.login.resources.pin_error_save_failed
import org.jetbrains.compose.resources.getString

/**
 * Provides string resources for PIN feature.
 * Extracted as an interface to allow test-friendly mocking.
 */
interface PinResourceProvider {
    suspend fun getPinTitle(mode: PinMode): String

    suspend fun getPinSubtitle(mode: PinMode): String

    suspend fun getPrimaryButtonText(mode: PinMode): String

    suspend fun getSecondaryButtonText(mode: PinMode): String?

    suspend fun getPinErrorIncorrect(): String

    suspend fun getPinErrorWithAttempts(
        message: String,
        attempts: Int,
    ): String

    suspend fun getPinErrorNoPinStored(): String

    suspend fun getPinErrorSaveFailed(): String

    suspend fun getPinErrorResetFailed(): String
}

/**
 * Default implementation of [PinResourceProvider] using Compose Multiplatform string resources.
 */
class PinResourceProviderImpl : PinResourceProvider {
    override suspend fun getPinTitle(mode: PinMode): String =
        when (mode) {
            PinMode.SET -> getString(Res.string.create_pin)
            PinMode.ASK -> getString(Res.string.enter_pin)
        }

    override suspend fun getPinSubtitle(mode: PinMode): String =
        when (mode) {
            PinMode.SET -> getString(Res.string.create_pin_description)
            PinMode.ASK -> getString(Res.string.enter_pin_description)
        }

    override suspend fun getPrimaryButtonText(mode: PinMode): String =
        when (mode) {
            PinMode.SET -> getString(Res.string.create_pin_button)
            PinMode.ASK -> getString(Res.string.enter_pin_button)
        }

    override suspend fun getSecondaryButtonText(mode: PinMode): String? =
        when (mode) {
            PinMode.SET -> null
            PinMode.ASK -> getString(Res.string.forgot_pin_button)
        }

    override suspend fun getPinErrorIncorrect(): String = getString(Res.string.pin_error_incorrect)

    override suspend fun getPinErrorWithAttempts(
        message: String,
        attempts: Int,
    ): String = "$message. ${getString(Res.string.pin_error_remaining_attempts, attempts)}"

    override suspend fun getPinErrorNoPinStored(): String = getString(Res.string.pin_error_no_pin)

    override suspend fun getPinErrorSaveFailed(): String = getString(Res.string.pin_error_save_failed)

    override suspend fun getPinErrorResetFailed(): String = getString(Res.string.pin_error_reset_failed)
}
